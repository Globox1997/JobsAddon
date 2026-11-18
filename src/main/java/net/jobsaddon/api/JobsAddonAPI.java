package net.jobsaddon.api;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobExperienceType;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.jobs.PlacedBlockTracker;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class JobsAddonAPI {

    private JobsAddonAPI() {
    }

    public static Collection<Job> getJobs() {
        return Collections.unmodifiableCollection(JobsManager.JOBS.values());
    }

    @Nullable
    public static Job getJob(String key) {
        return JobsManager.getJobByKey(key);
    }

    @Nullable
    public static Job getPlayerJob(Player player, String key) {
        return getJobsManager(player).getPlayerJobs().values().stream().filter(playerJob -> playerJob.getKey().equals(key)).findFirst().orElse(null);
    }

    public static int getJobLevel(Player player, String key) {
        Job job = getPlayerJob(player, key);
        return job != null ? job.getLevel() : 0;
    }

    public static int getJobExperience(Player player, String key) {
        Job job = getPlayerJob(player, key);
        return job != null ? job.getExperience() : 0;
    }

    public static int getNextLevelExperience(Player player, String key) {
        Job job = getPlayerJob(player, key);
        return job != null ? getJobsManager(player).getNextJobLevelExperience(job.getId()) : 0;
    }

    public static boolean isEmployed(Player player, String key) {
        Job job = getPlayerJob(player, key);
        return job != null && getJobsManager(player).isEmployedJob(job.getId());
    }

    public static List<Job> getEmployedJobs(Player player) {
        JobsManager jobsManager = getJobsManager(player);
        return jobsManager.getEmployedJobsList().stream().map(jobsManager::getJob).filter(job -> job != null).toList();
    }

    public static int getJobCooldown(Player player) {
        return getJobsManager(player).getEmployedJobTime();
    }

    /**
     * Adds experience to the job even if it isn't employed, can level up the job and grants the level rewards.
     *
     * @param callEvent if JobXpCallback should be able to modify the experience
     * @return false if the job doesn't exist
     */
    public static boolean addJobExperience(ServerPlayer player, String key, int experience, boolean callEvent) {
        Job job = getPlayerJob(player, key);
        if (job == null) {
            return false;
        }
        JobHelper.addCustomJobXp(player, job.getId(), experience, callEvent);
        return true;
    }

    public static boolean setJobLevel(ServerPlayer player, String key, int level) {
        Job job = getPlayerJob(player, key);
        if (job == null) {
            return false;
        }
        getJobsManager(player).setJobLevel(job.getId(), level);
        sync(player);
        return true;
    }

    /**
     * @param force ignores the cooldown, max level, free job slots and JobEmploymentEvents.ALLOW_EMPLOY
     * @return true if the job got employed
     */
    public static boolean employJob(ServerPlayer player, String key, boolean force) {
        Job job = getPlayerJob(player, key);
        JobsManager jobsManager = getJobsManager(player);
        if (job == null || jobsManager.isEmployedJob(job.getId())) {
            return false;
        }
        if (!force && (!jobsManager.canEmployJob(job.getId()) || !JobEmploymentEvents.ALLOW_EMPLOY.invoker().allowEmploy(player, job))) {
            return false;
        }
        jobsManager.employJob(job.getId());
        if (!force) {
            jobsManager.setEmployedJobTime(ConfigInit.CONFIG.jobChangeTime);
        }
        sync(player);
        return true;
    }

    public static boolean quitJob(ServerPlayer player, String key) {
        Job job = getPlayerJob(player, key);
        if (job == null || !getJobsManager(player).isEmployedJob(job.getId())) {
            return false;
        }
        getJobsManager(player).quitJob(job.getId());
        sync(player);
        return true;
    }

    public static void setJobCooldown(ServerPlayer player, int ticks) {
        getJobsManager(player).setEmployedJobTime(ticks);
        sync(player);
    }

    /**
     * Grants the experience of a built-in type like it happened in game, e.g. for custom harvesting or breeding.
     * Only employed jobs get xp, diminishing returns and JobXpCallback apply.
     *
     * @param id    the id of the block, item, entity type or potion, depending on the type
     * @param count multiplies the experience
     */
    public static void triggerExperience(ServerPlayer player, JobExperienceType type, ResourceLocation id, int count) {
        Registry<?> registry = switch (type) {
            case BLOCK_BREAK, BLOCK_PLACE, HARVEST -> BuiltInRegistries.BLOCK;
            case ITEM_DROP, CRAFTING, SMITHING, STONECUTTING, TRADING, CASTING -> BuiltInRegistries.ITEM;
            case ENTITY_KILL, BREEDING, TAMING -> BuiltInRegistries.ENTITY_TYPE;
            case BREWING -> BuiltInRegistries.POTION;
            // levelz enchantment ids depend on the level, use the enchanting screen instead
            case ENCHANTING -> null;
        };
        if (registry != null && registry.containsKey(id)) {
            JobHelper.grantJobXp(player, type, getRawId(registry, id), count);
        }
    }

    private static <T> int getRawId(Registry<T> registry, ResourceLocation id) {
        return registry.getId(registry.get(id));
    }

    // Right click harvesting for other mods
    public static void triggerHarvest(ServerPlayer player, BlockState state) {
        JobHelper.harvestJobXp(player, state);
    }

    // Blocks placed by players grant no xp when broken
    public static boolean isPlayerPlaced(Level world, BlockPos pos, BlockState state) {
        return PlacedBlockTracker.isPlayerPlaced(world, pos, state);
    }

    private static JobsManager getJobsManager(Player player) {
        return ((JobsManagerAccess) player).getJobsManager();
    }

    private static void sync(ServerPlayer player) {
        JobsServerPacket.writeS2CJobPacket(getJobsManager(player), player);
    }
}
