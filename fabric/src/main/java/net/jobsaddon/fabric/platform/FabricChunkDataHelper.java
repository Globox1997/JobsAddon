package net.jobsaddon.fabric.platform;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.jobsaddon.fabric.JobsAddonFabric;
import net.jobsaddon.platform.services.IChunkDataHelper;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class FabricChunkDataHelper implements IChunkDataHelper {

    @Override
    public @Nullable Long2IntOpenHashMap getPlacedBlocks(LevelChunk chunk) {
        return chunk.getAttached(JobsAddonFabric.PLACED_BLOCKS);
    }

    @Override
    public Long2IntOpenHashMap getOrCreatePlacedBlocks(LevelChunk chunk) {
        return chunk.getAttachedOrCreate(JobsAddonFabric.PLACED_BLOCKS);
    }
}
