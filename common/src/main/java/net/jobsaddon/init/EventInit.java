package net.jobsaddon.init;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class EventInit {

    public static final List<String> COMPAT_PACKS = List.of("bakery", "betterend", "betternether", "candlelight", "earlystage", "snuffles", "supplementaries", "vinery");

    public static void onPlayerJoin(ServerPlayer player) {
        JobsServerPacket.writeS2CJobPacket(((JobsManagerAccess) player).getJobsManager(), player);
    }

    public static void onPlayerCopy(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        ((JobsManagerAccess) newPlayer).getJobsManager().copyFrom(((JobsManagerAccess) oldPlayer).getJobsManager(), !alive && ConfigInit.CONFIG.resetCurrentJobXP);
    }

    public static void onPlayerRespawn(ServerPlayer newPlayer, boolean alive) {
        JobsManager jobsManager = ((JobsManagerAccess) newPlayer).getJobsManager();
        if (!alive) {
            float retainPercentage = getJobLevelRetainPercentage();
            if (retainPercentage < 100.0F) {
                jobsManager.retainJobLevels(retainPercentage);
                // the score counts the job level ups
                int totalJobLevel = jobsManager.getTotalJobLevel();
                newPlayer.level().getScoreboard().forAllObjectives(CriteriaInit.JOBS, newPlayer, score -> score.set(totalJobLevel));
            }
            if (ConfigInit.CONFIG.quitJobsOnDeath && !jobsManager.getEmployedJobsList().isEmpty()) {
                for (int jobId : List.copyOf(jobsManager.getEmployedJobsList())) {
                    jobsManager.quitJob(jobId);
                }
                jobsManager.setEmployedJobTime(ConfigInit.CONFIG.jobChangeTime);
            }
        }
        JobsServerPacket.writeS2CJobPacket(jobsManager, newPlayer);
    }

    public static void onPlayerChangeLevel(ServerPlayer player) {
        JobsServerPacket.writeS2CJobPacket(((JobsManagerAccess) player).getJobsManager(), player);
    }

    private static float getJobLevelRetainPercentage() {
        if (ConfigInit.CONFIG.jobLevelRetainPercentage < 0.0F) {
            return net.levelz.init.ConfigInit.CONFIG.levelRetainPercentage;
        }
        return ConfigInit.CONFIG.jobLevelRetainPercentage;
    }
}
