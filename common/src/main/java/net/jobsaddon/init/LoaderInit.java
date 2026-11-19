package net.jobsaddon.init;

import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLoader;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class LoaderInit {

    public static void onTagsLoaded() {
        JobLoader.resolveTags();
    }

    public static void onDataPackReload(MinecraftServer server, boolean success) {
        if (success) {
            for (ServerPlayer serverPlayerEntity : server.getPlayerList().getPlayers()) {
                JobsManager manager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
                manager.cleanupInvalidJobs();
                JobsServerPacket.writeS2CJobPacket(manager, serverPlayerEntity);
            }
            JobsAddonMain.LOGGER.info("Finished reload on {}", Thread.currentThread());
        } else {
            JobsAddonMain.LOGGER.error("Failed to reload on {}", Thread.currentThread());
        }
    }

}
