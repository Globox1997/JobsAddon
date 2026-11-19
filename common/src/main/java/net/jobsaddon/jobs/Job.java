package net.jobsaddon.jobs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Job {

    private final int id;
    private final String key;
    private final int maxLevel;
    private int level;
    private int experience;

    public Job(int id, String key, int maxLevel, int level, int experience) {
        this.id = id;
        this.key = key;
        this.maxLevel = maxLevel;
        this.level = level;
        this.experience = experience;
    }

    public Job(ValueInput input) {
        this.id = input.getIntOr("Id", 0);
        this.key = input.getStringOr("Key", "");
        this.maxLevel = input.getIntOr("MaxLevel", 0);
        this.level = input.getIntOr("Level", 0);
        this.experience = input.getIntOr("Experience", 0);
    }

    public void writeData(ValueOutput output) {
        output.putInt("Id", this.id);
        output.putString("Key", this.key);
        output.putInt("MaxLevel", this.maxLevel);
        output.putInt("Level", this.level);
        output.putInt("Experience", this.experience);
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getExperience() {
        return experience;
    }

    public Component getJobName() {
        return Component.translatable("text.jobsaddon." + this.key);
    }

    public Component getJobTooltip() {
        return Component.translatable("text.jobsaddon." + this.key + ".tooltip");
    }
}
