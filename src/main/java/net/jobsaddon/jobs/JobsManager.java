package net.jobsaddon.jobs;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import net.fabricmc.loader.api.FabricLoader;
import net.jobsaddon.api.JobEmploymentEvents;
import net.jobsaddon.api.JobLevelUpCallback;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.CriteriaInit;
import net.jobsaddon.network.JobsServerPacket;
import net.levelz.access.ServerPlayerSyncAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JobsManager {

    public static final Map<Integer, Job> JOBS = new HashMap<>();
    public static final Map<Integer, List<JobExperience>> BLOCK_BREAK_EXPERIENCE = JobExperienceType.BLOCK_BREAK.getExperienceMap();
    public static final Map<Integer, List<JobExperience>> BLOCK_PLACE_EXPERIENCE = JobExperienceType.BLOCK_PLACE.getExperienceMap();
    public static final Map<Integer, List<JobExperience>> ITEM_DROP_EXPERIENCE = JobExperienceType.ITEM_DROP.getExperienceMap();
    public static final Map<Integer, List<JobExperience>> ITEM_CRAFT_EXPERIENCE = JobExperienceType.CRAFTING.getExperienceMap();
    public static final Map<Integer, List<JobExperience>> ENCHANTMENT_EXPERIENCE = JobExperienceType.ENCHANTING.getExperienceMap();
    public static final Map<Integer, List<JobExperience>> BREWING_EXPERIENCE = JobExperienceType.BREWING.getExperienceMap();
    public static final Map<Integer, List<JobExperience>> ENTITY_KILL_EXPERIENCE = JobExperienceType.ENTITY_KILL.getExperienceMap();
    public static final Set<Identifier> RESTRICTED_RECIPES = new HashSet<>(); // used for crafting xp restriction

    public static final List<Map<Integer, List<JobExperience>>> EXPERIENCE_MAPS = Arrays.stream(JobExperienceType.values()).map(JobExperienceType::getExperienceMap).toList();

    private final PlayerEntity playerEntity;
    private Map<Integer, Job> playerJobs = new HashMap<>();
    // Employed Jobs
    private final ArrayList<Integer> employedJobsList = new ArrayList<>();
    private int employedJobTime;
    private final DiminishingReturns diminishingReturns = new DiminishingReturns();

    public JobsManager(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
        cleanupInvalidJobs();
    }

    public void readNbt(NbtCompound nbt) {
        NbtList jobsList = nbt.getList("Jobs", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < jobsList.size(); i++) {
            Job job = new Job(jobsList.getCompound(i));
            this.playerJobs.put(job.getId(), job);
        }
        // Employed Jobs
        this.employedJobsList.clear();
        int employedJobCount = nbt.getInt("EmployedJobCount");
        for (int i = 0; i < employedJobCount; i++) {
            int jobId = nbt.getInt("EmployedJob" + i);
            if (!this.employedJobsList.contains(jobId)) {
                this.employedJobsList.add(jobId);
            }
        }
        this.employedJobTime = nbt.getInt("EmployedJobTime");
        cleanupInvalidJobs();
    }

    public void writeNbt(NbtCompound nbt) {
        NbtList jobs = new NbtList();
        for (Job job : this.playerJobs.values()) {
            jobs.add(job.writeDataToNbt());
        }
        nbt.put("Jobs", jobs);
        // Employed Jobs
        for (int i = 0; i < this.employedJobsList.size(); i++) {
            nbt.putInt("EmployedJob" + i, this.employedJobsList.get(i));
        }
        nbt.putInt("EmployedJobCount", this.employedJobsList.size());
        nbt.putInt("EmployedJobTime", this.employedJobTime);
    }

    public void copyFrom(JobsManager jobsManager, boolean resetCurrentJobXp) {
        for (Job job : jobsManager.getPlayerJobs().values()) {
            setJobLevel(job.getId(), job.getLevel());
            setJobXP(job.getId(), resetCurrentJobXp ? 0 : job.getExperience());
        }
        this.employedJobsList.clear();
        for (int jobId : jobsManager.getEmployedJobsList()) {
            if (!this.employedJobsList.contains(jobId)) {
                this.employedJobsList.add(jobId);
            }
        }
        this.employedJobTime = jobsManager.getEmployedJobTime();
    }

    public void retainJobLevels(float retainPercentage) {
        float factor = Math.clamp(retainPercentage, 0.0F, 100.0F) / 100.0F;
        for (Job job : this.playerJobs.values()) {
            setJobLevel(job.getId(), (int) (job.getLevel() * factor));
            int levelCost = getNextJobLevelExperience(job.getId());
            if (levelCost > 0) {
                setJobXP(job.getId(), Math.min(job.getExperience(), levelCost - 1));
            }
        }
    }

    public int getTotalJobLevel() {
        return this.playerJobs.values().stream().mapToInt(Job::getLevel).sum();
    }

    public Map<Integer, Job> getPlayerJobs() {
        return playerJobs;
    }

    public void setPlayerJobs(Map<Integer, Job> playerJobs) {
        this.playerJobs = playerJobs;
    }

    @Nullable
    public Job getJob(int id) {
        return this.playerJobs.get(id);
    }

    public void setJobLevel(int id, int level) {
        Job job = this.playerJobs.get(id);
        if (job != null) {
            job.setLevel(MathHelper.clamp(level, 0, job.getMaxLevel()));
        }
    }

    public int getJobLevel(int id) {
        Job job = this.playerJobs.get(id);
        return job != null ? job.getLevel() : 0;
    }

    public void setJobXP(int id, int experience) {
        Job job = this.playerJobs.get(id);
        if (job != null) {
            job.setExperience(Math.max(experience, 0));
        }
    }

    public int getJobXP(int id) {
        Job job = this.playerJobs.get(id);
        return job != null ? job.getExperience() : 0;
    }

    // Should only get called on server
    public void addJobXP(PlayerEntity playerEntity, int id, int experience) {
        if (experience <= 0 || !this.playerJobs.containsKey(id) || isJobMaxLevel(id)) {
            return;
        }
        int currentJobXp = getJobXP(id) + experience;
        boolean leveledUp = false;
        int levelCost = getNextJobLevelExperience(id);

        while (levelCost > 0 && currentJobXp >= levelCost) {
            currentJobXp -= levelCost;
            int oldLevel = getJobLevel(id);
            addJobExperienceLevels(playerEntity, id, 1);
            playerEntity.getWorld().playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, playerEntity.getSoundCategory(), 1.0F, 1.0F);

            if (getJobLevel(id) > oldLevel && playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                playerEntity.getScoreboard().forEachScore(CriteriaInit.JOBS, playerEntity, ScoreAccess::incrementScore);
                CriteriaInit.JOB_UP.trigger(serverPlayerEntity, this.playerJobs.get(id).getKey(), getJobLevel(id));
                JobLevelUpCallback.EVENT.invoker().onLevelUp(serverPlayerEntity, this.playerJobs.get(id), getJobLevel(id));
                leveledUp = true;
            }
            levelCost = getNextJobLevelExperience(id);
        }
        setJobXP(id, levelCost > 0 ? currentJobXp : 0);

        if (leveledUp && playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
            JobsServerPacket.writeS2CJobPacket(this, serverPlayerEntity);
        }
    }

    public void addJobExperienceLevels(PlayerEntity playerEntity, int id, int levels) {
        if (!this.playerJobs.containsKey(id)) {
            return;
        }
        setJobLevel(id, getJobLevel(id) + levels);
        int jobLevel = getJobLevel(id);

        if (!playerEntity.getWorld().isClient() && jobLevel > 0) {
            // Add numismatic money
            if (FabricLoader.getInstance().isModLoaded("numismatic-overhaul") && ConfigInit.CONFIG.moneyMultiplicator > 0) {
                CurrencyComponent playerBalance = ModComponents.CURRENCY.get(playerEntity);
                playerBalance.silentModify((long) jobLevel * ConfigInit.CONFIG.moneyMultiplicator);
            }
            // Add levelz xp
            if (ConfigInit.CONFIG.levelZXPMultiplicator > 0) {
                ((ServerPlayerSyncAccess) playerEntity).addLevelExperience(ConfigInit.CONFIG.levelZXPMultiplicator * jobLevel);
            }
            // Add vanilla xp
            if (ConfigInit.CONFIG.xpMultiplicator > 0) {
                playerEntity.addExperience(ConfigInit.CONFIG.xpMultiplicator * jobLevel);
            }
        }
    }

    public void employJob(int id) {
        if (!isEmployedJob(id)) {
            employedJobsList.add(id);
            onEmploymentChanged(id, true);
        }
    }

    public void quitJob(int id) {
        if (employedJobsList.remove((Object) id)) {
            onEmploymentChanged(id, false);
        }
    }

    private void onEmploymentChanged(int id, boolean employed) {
        if (this.playerEntity instanceof ServerPlayerEntity serverPlayerEntity && this.playerJobs.containsKey(id)) {
            JobEmploymentEvents.EMPLOYMENT_CHANGED.invoker().onEmploymentChanged(serverPlayerEntity, this.playerJobs.get(id), employed);
        }
    }

    public boolean isEmployedJob(int id) {
        return employedJobsList.contains(id);
    }

    public boolean hasFreeJobSlot() {
        return employedJobsList.size() < ConfigInit.CONFIG.employedJobs;
    }

    public boolean canEmployJob(int id) {
        return this.playerJobs.containsKey(id) && !isEmployedJob(id) && !isJobMaxLevel(id) && hasFreeJobSlot() && getEmployedJobTime() == 0;
    }

    public ArrayList<Integer> getEmployedJobsList() {
        return employedJobsList;
    }

    public boolean hasReachedMaxLevel(int id) {
        Job job = this.playerJobs.get(id);
        return job != null && job.getLevel() >= job.getMaxLevel();
    }

    public boolean isJobMaxLevel(int id) {
        if (ConfigInit.CONFIG.allowMaxLvlProgress) {
            return false;
        }
        return hasReachedMaxLevel(id);
    }

    public DiminishingReturns getDiminishingReturns() {
        return diminishingReturns;
    }

    public int getEmployedJobTime() {
        return employedJobTime;
    }

    public void setEmployedJobTime(int time) {
        employedJobTime = Math.max(time, 0);
    }

    // Recommend to use https://www.geogebra.org/graphing
    public int getNextJobLevelExperience(int id) {
        if (!this.playerJobs.containsKey(id) || isJobMaxLevel(id)) {
            return 0;
        }
        int experienceCost = (int) (ConfigInit.CONFIG.jobXPBaseCost + ConfigInit.CONFIG.jobXPCostMultiplicator * Math.pow(getJobLevel(id), ConfigInit.CONFIG.jobXPExponent));
        if (ConfigInit.CONFIG.jobXPMaxCost > 0) {
            experienceCost = Math.min(experienceCost, ConfigInit.CONFIG.jobXPMaxCost);
        }
        return Math.max(experienceCost, 1);
    }

    public void cleanupInvalidJobs() {
        Map<Integer, Job> jobs = new HashMap<>();
        for (Job baseJob : JOBS.values()) {
            Job playerJob = this.playerJobs.get(baseJob.getId());
            int level = playerJob != null ? MathHelper.clamp(playerJob.getLevel(), 0, baseJob.getMaxLevel()) : 0;
            int experience = playerJob != null ? Math.max(playerJob.getExperience(), 0) : 0;
            jobs.put(baseJob.getId(), new Job(baseJob.getId(), baseJob.getKey(), baseJob.getMaxLevel(), level, experience));
        }
        this.playerJobs = jobs;
        this.employedJobsList.removeIf(jobId -> !JOBS.containsKey(jobId));
    }

    @Nullable
    public static Job getJobByKey(String key) {
        for (Job job : JOBS.values()) {
            if (job.getKey().equals(key)) {
                return job;
            }
        }
        return null;
    }

    public static void addJobExperience(Map<Integer, List<JobExperience>> map, int rawId, JobExperience jobExperience, boolean override) {
        List<JobExperience> jobExperiences = map.computeIfAbsent(rawId, id -> new ArrayList<>());
        for (int i = 0; i < jobExperiences.size(); i++) {
            if (jobExperiences.get(i).getId() == jobExperience.getId()) {
                if (override) {
                    jobExperiences.set(i, jobExperience);
                }
                return;
            }
        }
        jobExperiences.add(jobExperience);
    }

    public static void removeJobExperience(Map<Integer, List<JobExperience>> map, int jobId, int experience) {
        map.values().forEach(jobExperiences -> jobExperiences.removeIf(jobExperience -> jobExperience.getId() == jobId && jobExperience.getExperience() == experience));
        map.values().removeIf(List::isEmpty);
    }

    public static void removeJobExperience(int jobId) {
        for (Map<Integer, List<JobExperience>> map : EXPERIENCE_MAPS) {
            map.values().forEach(jobExperiences -> jobExperiences.removeIf(jobExperience -> jobExperience.getId() == jobId));
            map.values().removeIf(List::isEmpty);
        }
    }

}
