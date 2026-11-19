package net.jobsaddon.neoforge.platform;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.jobsaddon.neoforge.JobsAddonNeoForge;
import net.jobsaddon.platform.services.IChunkDataHelper;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class NeoForgeChunkDataHelper implements IChunkDataHelper {

    @Override
    public @Nullable Long2IntOpenHashMap getPlacedBlocks(LevelChunk chunk) {
        return chunk.getExistingDataOrNull(JobsAddonNeoForge.PLACED_BLOCKS);
    }

    @Override
    public Long2IntOpenHashMap getOrCreatePlacedBlocks(LevelChunk chunk) {
        return chunk.getData(JobsAddonNeoForge.PLACED_BLOCKS);
    }
}
