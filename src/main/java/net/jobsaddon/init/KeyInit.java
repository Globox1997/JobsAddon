package net.jobsaddon.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.screen.JobScreen;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public class KeyInit {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(JobsAddonMain.identifierOf("keybind"));
    public static KeyMapping screenKey = new KeyMapping("key.jobsaddon.openjobsscreen", InputConstants.UNKNOWN.getValue(), CATEGORY);

    public static void init() {
        // Registering
        KeyMappingHelper.registerKeyMapping(screenKey);
        // Callback
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (screenKey.consumeClick()) {
                client.setScreenAndShow(new JobScreen());
                return;
            }
        });
    }

}
