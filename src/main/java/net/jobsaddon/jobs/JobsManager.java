package net.jobsaddon.jobs;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import net.fabricmc.loader.api.FabricLoader;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobsManager {

    public static final Map<Integer, Job> JOBS = new HashMap<>();
    public static final Map<Integer, JobExperience> BLOCK_BREAK_EXPERIENCE = new HashMap<>();
    public static final Map<Integer, JobExperience> BLOCK_PLACE_EXPERIENCE = new HashMap<>();
    public static final Map<Integer, JobExperience> ITEM_DROP_EXPERIENCE = new HashMap<>(); // used for entity drops, block drops and fish drops
    public static final Map<Integer, JobExperience> ITEM_CRAFT_EXPERIENCE = new HashMap<>();
    public static final Map<Integer, JobExperience> ENCHANTMENT_EXPERIENCE = new HashMap<>();
    public static final Map<Integer, JobExperience> BREWING_EXPERIENCE = new HashMap<>(); // used for brewing
    public static final Map<Integer, JobExperience> ENTITY_KILL_EXPERIENCE = new HashMap<>(); // used for killing entities
    public static final List<Identifier> RESTRICTED_RECIPES = new ArrayList<>(); // used for crafting xp restriction

    private final PlayerEntity playerEntity;
    private Map<Integer, Job> playerJobs = new HashMap<>();
    // Employed Jobs
    private ArrayList<Integer> employedJobsList = new ArrayList<>();
    private int employedJobTime;


    public JobsManager(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;

        for (Job job : JOBS.values()) {
            if (!this.playerJobs.containsKey(job.getId())) {
                this.playerJobs.put(job.getId(), new Job(job.getId(), job.getKey(), job.getMaxLevel(), 0, 0));
            } else if (this.playerJobs.get(job.getId()).getLevel() > job.getMaxLevel()) {
                this.playerJobs.get(job.getId()).setLevel(job.getMaxLevel());
            }
        }
    }

    public void readNbt(NbtCompound nbt) {
        NbtList jobsList = nbt.getList("Jobs", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < jobsList.size(); i++) {
            Job job = new Job(jobsList.getCompound(i));
            this.playerJobs.put(job.getId(), job);
        }
        // Employed Jobs
        int employedJobCount = nbt.getInt("EmployedJobCount");
        if (employedJobCount > 0) {
            this.employedJobsList.clear();
            for (int i = 0; i < employedJobCount; i++) {
                this.employedJobsList.add(nbt.getInt("EmployedJob" + i));
            }
        }
        this.employedJobTime = nbt.getInt("EmployedJobTime");
    }

    public void writeNbt(NbtCompound nbt) {
        NbtList jobs = new NbtList();
        for (Job job : this.playerJobs.values()) {
            jobs.add(job.writeDataToNbt());
        }
        nbt.put("Jobs", jobs);
        // Employed Jobs
        if (!this.employedJobsList.isEmpty()) {
            for (int i = 0; i < this.employedJobsList.size(); i++) {
                nbt.putInt("EmployedJob" + i, this.employedJobsList.get(i));
            }
        }
        nbt.putInt("EmployedJobCount", this.employedJobsList.size());
        nbt.putInt("EmployedJobTime", this.employedJobTime);
    }

    public Map<Integer, Job> getPlayerJobs() {
        return playerJobs;
    }

    public void setPlayerJobs(Map<Integer, Job> playerJobs) {
        this.playerJobs = playerJobs;
    }

    public void setJobLevel(int id, int level) {
        this.playerJobs.get(id).setLevel(level);
    }

    public int getJobLevel(int id) {
        return this.playerJobs.get(id).getLevel();
    }

    public void setJobXP(int id, int experience) {
        this.playerJobs.get(id).setExperience(experience);
    }

    public int getJobXP(int id) {
        return this.playerJobs.get(id).getExperience();
    }

    // Should only get called on server
    public void addJobXP(PlayerEntity playerEntity, int id, int experience) {
        if (!isJobMaxLevel(id)) {
            int currentJobXp = getJobXP(id) + experience;

            while (getNextJobLevelExperience(id) < currentJobXp && !isJobMaxLevel(id)) {
                addJobExperienceLevels(playerEntity, id, 1);
                playerEntity.getWorld().playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, playerEntity.getSoundCategory(), 1.0F, 1.0F);
                if (!playerEntity.getWorld().isClient()) {
                    playerEntity.getScoreboard().forEachScore(CriteriaInit.JOBS, playerEntity, ScoreAccess::incrementScore);
                    CriteriaInit.JOB_UP.trigger((ServerPlayerEntity) playerEntity, this.playerJobs.get(id).getKey(), getJobLevel(id));
                    JobsServerPacket.writeS2CJobPacket(this, (ServerPlayerEntity) playerEntity);
                }
                currentJobXp -= getNextJobLevelExperience(id);
            }
            setJobXP(id, currentJobXp);
        }
    }

    public void addJobExperienceLevels(PlayerEntity playerEntity, int id, int levels) {
        int jobLevel = getJobLevel(id);
        if (jobLevel < this.playerJobs.get(id).getMaxLevel()) {
            jobLevel += levels;
            setJobLevel(id, jobLevel);
        }
        if (jobLevel < 0) {
            jobLevel = 0;
            setJobLevel(id, jobLevel);
        }
        if (!playerEntity.getWorld().isClient()) {
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
        }
    }

    public void quitJob(int id) {
        if (isEmployedJob(id)) {
            employedJobsList.remove((Object) id);
        }
    }

    public boolean isEmployedJob(int id) {
        return employedJobsList.contains(id);
    }

    public boolean canEmployJob(int id) {
        return !isEmployedJob(id) && employedJobsList.size() < ConfigInit.CONFIG.employedJobs && getEmployedJobTime() == 0;
    }

    public boolean hasMaxEmployedJobs() {
        return employedJobsList != null && employedJobsList.size() == ConfigInit.CONFIG.employedJobs;
    }

    public ArrayList<Integer> getEmployedJobsList() {
        return employedJobsList;
    }

    public boolean isJobMaxLevel(int id) {
        if (ConfigInit.CONFIG.allowMaxLvlProgress) {
            return false;
        }
        return getJobLevel(id) >= this.playerJobs.get(id).getMaxLevel();
    }

    public int getEmployedJobTime() {
        return employedJobTime;
    }

    public void setEmployedJobTime(int time) {
        employedJobTime = time;
    }

    // Recommend to use https://www.geogebra.org/graphing
    public int getNextJobLevelExperience(int id) {
        if (isJobMaxLevel(id)) {
            return 0;
        }
        int experienceCost = (int) (ConfigInit.CONFIG.jobXPBaseCost + ConfigInit.CONFIG.jobXPCostMultiplicator * Math.pow(getJobLevel(id), ConfigInit.CONFIG.jobXPExponent));
        if (ConfigInit.CONFIG.jobXPMaxCost != 0) {
            return experienceCost >= ConfigInit.CONFIG.jobXPMaxCost ? ConfigInit.CONFIG.jobXPMaxCost : experienceCost;
        } else {
            return experienceCost;
        }
    }

}
