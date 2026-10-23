package net.jobsaddon.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
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
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(JobLoader.ID, JobLoader::new);

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) {
                JobLoader.resolveTags();
            }
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
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
