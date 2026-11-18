package net.jobsaddon.jobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

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

    public Job(CompoundTag nbt) {
        this.id = nbt.getInt("Id");
        this.key = nbt.getString("Key");
        this.maxLevel = nbt.getInt("MaxLevel");
        this.level = nbt.getInt("Level");
        this.experience = nbt.getInt("Experience");
    }

    public CompoundTag writeDataToNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Id", this.id);
        nbt.putString("Key", this.key);
        nbt.putInt("MaxLevel", this.maxLevel);
        nbt.putInt("Level", this.level);
        nbt.putInt("Experience", this.experience);
        return nbt;
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
