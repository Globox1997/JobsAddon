package net.jobsaddon.jobs;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.jobsaddon.init.ConfigInit;

public class DiminishingReturns {

    private static final int CLEANUP_SIZE = 256;

    private final Long2ObjectOpenHashMap<Heat> heatMap = new Long2ObjectOpenHashMap<>();

    public float getMultiplier(JobExperienceType type, int rawId, long tick) {
        if (!ConfigInit.CONFIG.diminishingReturns || !ConfigInit.CONFIG.diminishingReturnsTypes.contains(type.getKey())) {
            return 1.0F;
        }
        Heat heat = this.heatMap.computeIfAbsent(((long) type.ordinal() << 32) | (rawId & 0xFFFFFFFFL), key -> new Heat());
        heat.value = getDecayedHeat(heat, tick) + 1.0D;
        heat.tick = tick;

        if (this.heatMap.size() > CLEANUP_SIZE) {
            this.heatMap.values().removeIf(entry -> getDecayedHeat(entry, tick) < 1.0D);
        }

        int threshold = Math.max(ConfigInit.CONFIG.diminishingReturnsThreshold, 1);
        if (heat.value <= threshold) {
            return 1.0F;
        }
        return (float) Math.max(ConfigInit.CONFIG.diminishingReturnsMinMultiplier, threshold / heat.value);
    }

    public void clear() {
        this.heatMap.clear();
    }

    private static double getDecayedHeat(Heat heat, long tick) {
        int halfLife = Math.max(ConfigInit.CONFIG.diminishingReturnsHalfLife, 1);
        return heat.value * Math.pow(0.5D, Math.max(tick - heat.tick, 0L) / (double) halfLife);
    }

    private static class Heat {
        private double value;
        private long tick;
    }
}
