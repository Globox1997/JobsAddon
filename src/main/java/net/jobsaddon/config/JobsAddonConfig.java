package net.jobsaddon.config;

import java.util.ArrayList;
import java.util.Arrays;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "jobsaddon")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class JobsAddonConfig implements ConfigData {

    // public int test = 0;
    // public int test1 = 0;
    // public int test2 = 0;
    // public int test3 = 70;
    // public int test4 = 10;
    // public int test5 = 10;

    @Comment("Count for how many jobs can a player have at a time")
    public int employedJobs = 2;
    @Comment("Delay to employ new job")
    public int jobChangeTime = 72000;
    @Comment("Reset current job lvl xp when died")
    public boolean resetCurrentJobXP = true;
    @Comment("Only for Devs")
    public boolean devMode = false;

    @ConfigEntry.Category("experience_settings")
    @Comment("Max level of each job")
    public int jobMaxLevel = 100;
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
    @Comment("Default xp per job execution")
    public int brewerXP = 10;
    @ConfigEntry.Category("experience_settings")
    public int builderXP = 1;
    @ConfigEntry.Category("experience_settings")
    public int farmerXP = 3;
    @ConfigEntry.Category("experience_settings")
    public int fisherXP = 10;
    @ConfigEntry.Category("experience_settings")
    public int lumberjackXP = 3;
    @ConfigEntry.Category("experience_settings")
    public int minerXP = 7;
    @ConfigEntry.Category("experience_settings")
    public int smitherXP = 8;
    @ConfigEntry.Category("experience_settings")
    public int warriorXP = 5;

    @ConfigEntry.Category("reward_settings")
    public int levelZXPMultiplicator = 1;
    @ConfigEntry.Category("reward_settings")
    @Comment("Numismatic Overhaul compat lvl * multiplicator")
    public int moneyMultiplicator = 20;

    @Comment("Show the job gui button in the inventory")
    public boolean inventoryButton = true;

    public ArrayList<Object> getJobConfigList() {
        return new ArrayList<Object>(Arrays.asList(employedJobs, jobChangeTime, resetCurrentJobXP, jobMaxLevel, jobXPBaseCost, jobXPCostMultiplicator, jobXPExponent, jobXPMaxCost, brewerXP,
                builderXP, farmerXP, fisherXP, lumberjackXP, minerXP, smitherXP, warriorXP, levelZXPMultiplicator, moneyMultiplicator));
    }
}