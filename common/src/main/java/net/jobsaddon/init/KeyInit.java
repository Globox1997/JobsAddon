package net.jobsaddon.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.screen.JobScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeyInit {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(JobsAddonMain.identifierOf("keybind"));
    public static KeyMapping screenKey = new KeyMapping("key.jobsaddon.openjobsscreen", InputConstants.UNKNOWN.getValue(), CATEGORY);

    public static void onClientTick(Minecraft client) {
        if (screenKey.consumeClick()) {
            client.setScreenAndShow(new JobScreen());
        }
    }

}
