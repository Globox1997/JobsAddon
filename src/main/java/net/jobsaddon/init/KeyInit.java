package net.jobsaddon.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.jobsaddon.screen.JobScreen;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class KeyInit {
    public static KeyMapping screenKey = new KeyMapping("key.jobsaddon.openjobsscreen", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.jobsaddon.keybind");

    public static void init() {
        // Registering
        KeyBindingHelper.registerKeyBinding(screenKey);
        // Callback
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (screenKey.consumeClick()) {
                client.setScreen(new JobScreen());
                return;
            }
        });
    }

}
