package net.jobsaddon.init;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.jobsaddon.data.JobLoader;
import net.minecraft.resource.ResourceType;

public class LoaderInit {

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JobLoader());
    }

}
