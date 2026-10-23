package net.jobsaddon.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.jobsaddon.init.ConfigInit;
import net.libz.api.ConfigSync;

import java.util.ArrayList;
import java.util.List;

@Config(name = "jobsaddon")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class JobsAddonConfig implements ConfigData, ConfigSync {

    @Comment("Count for how many jobs can a player have at a time")
    public int employedJobs = 2;
    @Comment("Delay to employ new job")
    public int jobChangeTime = 24000;
    @Comment("Reset current job lvl xp when died")
    public boolean resetCurrentJobXP = true;
    @Comment("Percentage of job levels kept on death, 100 keeps all, 0 resets all, -1 uses the LevelZ levelRetainPercentage")
    public float jobLevelRetainPercentage = 100.0F;
    @Comment("Quit all jobs on death and start the job change cooldown")
    public boolean quitJobsOnDeath = false;
    public boolean defaultJobs = true;
    @Comment("Blocks placed by players grant no xp when broken and placing them at the same spot again grants no xp")
    public boolean trackPlacedBlocks = true;

    @ConfigEntry.Category("experience_settings")
    @Comment("XP equation: lvl^exponent * multiplicator + base")
    public int jobXPBaseCost = 100;
    @ConfigEntry.Category("experience_settings")
    public float jobXPCostMultiplicator = 0.5F;
    @ConfigEntry.Category("experience_settings")
    public int jobXPExponent = 2;
    @ConfigEntry.Category("experience_settings")
    public int jobXPMaxCost = 0;
    @ConfigEntry.Category("experience_settings")
    @Comment("Repeating the same xp source in a short time grants less xp")
    public boolean diminishingReturns = true;
    @ConfigEntry.Category("experience_settings")
    @Comment("Recent repeats of the same source before the xp gets reduced")
    public int diminishingReturnsThreshold = 32;
    @ConfigEntry.Category("experience_settings")
    @Comment("Ticks until half of the repeats are forgotten")
    public int diminishingReturnsHalfLife = 1200;
    @ConfigEntry.Category("experience_settings")
    @Comment("Lowest xp multiplier")
    public float diminishingReturnsMinMultiplier = 0.1F;
    @ConfigEntry.Category("experience_settings")
    @Comment("Affected xp types: blockbreak, blockplace, itemdrop, crafting, enchanting, brewing, entitykill, smithing, stonecutting, breeding, taming, trading, harvest")
    public List<String> diminishingReturnsTypes = new ArrayList<>(List.of("entitykill", "itemdrop", "breeding", "taming", "trading", "harvest"));

    @ConfigEntry.Category("reward_settings")
    public int xpMultiplicator = 3;
    @ConfigEntry.Category("reward_settings")
    public int levelZXPMultiplicator = 3;
    @ConfigEntry.Category("reward_settings")
    @Comment("Numismatic Overhaul compat lvl * multiplicator")
    public int moneyMultiplicator = 20;
    @Comment("Max level jobs keep gaining xp and grant the level rewards again on every full xp bar")
    public boolean allowMaxLvlProgress = false;

    @Override
    public void updateConfig(ConfigData data) {
        ConfigInit.CONFIG = (JobsAddonConfig) data;
    }
}
