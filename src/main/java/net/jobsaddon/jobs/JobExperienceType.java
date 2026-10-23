package net.jobsaddon.jobs;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum JobExperienceType {
    BLOCK_BREAK("blockbreak"),
    BLOCK_PLACE("blockplace"),
    ITEM_DROP("itemdrop"), // used for entity drops, block drops and fish drops
    CRAFTING("crafting"),
    ENCHANTING("enchanting"),
    BREWING("brewing"),
    ENTITY_KILL("entitykill"),
    SMITHING("smithing"), // falls back to crafting
    STONECUTTING("stonecutting"), // falls back to crafting
    BREEDING("breeding"),
    TAMING("taming"),
    TRADING("trading"),
    HARVEST("harvest"); // falls back to blockbreak

    private final String key;
    private final Map<Integer, List<JobExperience>> experienceMap = new HashMap<>();

    JobExperienceType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Map<Integer, List<JobExperience>> getExperienceMap() {
        return experienceMap;
    }

    @Nullable
    public List<JobExperience> getJobExperiences(int rawId) {
        return experienceMap.get(rawId);
    }

    @Nullable
    public static JobExperienceType byKey(String key) {
        for (JobExperienceType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }
}
