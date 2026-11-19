package net.jobsaddon.init;

import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.screen.JobScreen;
import net.jobsaddon.screen.widget.JobTab;
import net.libz.registry.TabRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class RenderInit {

    public static final Identifier JOB_TAB_ICON = JobsAddonMain.identifierOf("textures/gui/job_tab_icon.png");

    public static void init() {
        TabRegistry.registerInventoryTab(new JobTab(Component.translatable("screen.jobsaddon.jobs_screen"), JOB_TAB_ICON, 2, JobScreen.class));
    }
}
