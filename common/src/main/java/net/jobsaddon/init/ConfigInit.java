package net.jobsaddon.init;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.jobsaddon.config.JobsAddonConfig;

public class ConfigInit {
    public static JobsAddonConfig CONFIG = new JobsAddonConfig();

    public static void init() {
        AutoConfig.register(JobsAddonConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(JobsAddonConfig.class).getConfig();
    }

}
