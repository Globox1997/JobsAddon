package net.jobsaddon.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLoader;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;

public class LoaderInit {

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JobLoader());

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
                    ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayerList().get(i);
                    JobsManager manager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
                    manager.cleanupInvalidJobs();
                    JobsServerPacket.writeS2CJobPacket(manager, serverPlayerEntity);
                }
                JobsAddonMain.LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else {
                JobsAddonMain.LOGGER.error("Failed to reload on {}", Thread.currentThread());
            }
        });
    }

}
