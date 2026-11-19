package net.jobsaddon.platform.services;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public interface IChunkDataHelper {

    @Nullable
    Long2IntOpenHashMap getPlacedBlocks(LevelChunk chunk);

    Long2IntOpenHashMap getOrCreatePlacedBlocks(LevelChunk chunk);
}
