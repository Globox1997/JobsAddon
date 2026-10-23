package net.jobsaddon.init;

import ht.treechop.api.TreeChopEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;

import java.util.List;

public class EventInit {

    public static final boolean isTreeChopLoaded = FabricLoader.getInstance().isModLoaded("treechop");

    private static final List<String> COMPAT_PACKS = List.of("bakery", "betterend", "betternether", "candlelight", "earlystage", "snuffles", "supplementaries", "vinery");

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            JobsManager jobsManager = ((JobsManagerAccess) handler.getPlayer()).getJobsManager();
            JobsServerPacket.writeS2CJobPacket(jobsManager, handler.getPlayer());
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            ((JobsManagerAccess) newPlayer).getJobsManager().copyFrom(((JobsManagerAccess) oldPlayer).getJobsManager(), !alive && ConfigInit.CONFIG.resetCurrentJobXP);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            JobsManager jobsManager = ((JobsManagerAccess) newPlayer).getJobsManager();
            if (!alive) {
                float retainPercentage = getJobLevelRetainPercentage();
                if (retainPercentage < 100.0F) {
                    jobsManager.retainJobLevels(retainPercentage);
                    // the score counts the job level ups
                    int totalJobLevel = jobsManager.getTotalJobLevel();
                    newPlayer.getScoreboard().forEachScore(CriteriaInit.JOBS, newPlayer, score -> score.setScore(totalJobLevel));
                }
                if (ConfigInit.CONFIG.quitJobsOnDeath && !jobsManager.getEmployedJobsList().isEmpty()) {
                    for (int jobId : List.copyOf(jobsManager.getEmployedJobsList())) {
                        jobsManager.quitJob(jobId);
                    }
                    jobsManager.setEmployedJobTime(ConfigInit.CONFIG.jobChangeTime);
                }
            }
            JobsServerPacket.writeS2CJobPacket(jobsManager, newPlayer);
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            JobsServerPacket.writeS2CJobPacket(((JobsManagerAccess) player).getJobsManager(), player);
        });
        if (isTreeChopLoaded) {
            TreeChopEvents.BEFORE_FELL.register((world, player, pos, data) -> {
                if (!world.isClient() && player != null && data.getTree().getLogBlocks().isPresent()) {
                    JobHelper.multiBlockBreakJobXp(player, data.getTree().getLogBlocks().get().stream().toList());
                }
                return true;
            });
        }
        for (String modId : COMPAT_PACKS) {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf(modId + "_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                        ResourcePackActivationType.DEFAULT_ENABLED);
            }
        }
    }

    private static float getJobLevelRetainPercentage() {
        if (ConfigInit.CONFIG.jobLevelRetainPercentage < 0.0F) {
            return net.levelz.init.ConfigInit.CONFIG.levelRetainPercentage;
        }
        return ConfigInit.CONFIG.jobLevelRetainPercentage;
    }
}
