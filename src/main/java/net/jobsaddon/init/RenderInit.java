package net.jobsaddon.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static final Identifier JOB_GUI_ICONS = new Identifier("jobsaddon:textures/gui/icons.png");

    public static void init() {
    }
}
