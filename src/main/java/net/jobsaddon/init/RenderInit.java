package net.jobsaddon.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jobsaddon.screen.JobScreen;
import net.jobsaddon.screen.widget.JobTab;
import net.libz.registry.TabRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static final Identifier JOB_GUI_ICONS = new Identifier("jobsaddon:textures/gui/icons.png");
    public static final Identifier JOB_TAB_ICON = new Identifier("jobsaddon:textures/gui/job_tab_icon.png");

    public static void init() {
        TabRegistry.registerInventoryTab(new JobTab(Text.translatable("screen.jobsaddon.jobs_screen"), JOB_TAB_ICON, 2, JobScreen.class));
    }
}
