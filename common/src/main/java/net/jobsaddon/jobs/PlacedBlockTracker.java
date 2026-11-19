package net.jobsaddon.jobs;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import java.util.stream.LongStream;

public class PlacedBlockTracker {

    private static final int NO_STATE = -1;

    public static final Codec<Long2IntOpenHashMap> CODEC = Codec.LONG_STREAM.xmap(PlacedBlockTracker::decode, PlacedBlockTracker::encode);

    public static boolean onPlayerPlaced(Level world, BlockPos pos, BlockState state) {
        if (!ConfigInit.CONFIG.trackPlacedBlocks || world.isClientSide()) {
            return true;
        }
        LevelChunk chunk = world.getChunkAt(pos);
        int stateId = Block.getId(state);
        int previousStateId = Services.chunkData().getOrCreatePlacedBlocks(chunk).put(pos.asLong(), stateId);
        chunk.markUnsaved();
        return previousStateId == NO_STATE || !matches(previousStateId, state);
    }

    public static boolean isPlayerPlaced(Level world, BlockPos pos, BlockState state) {
        if (!ConfigInit.CONFIG.trackPlacedBlocks || world.isClientSide()) {
            return false;
        }
        Long2IntOpenHashMap placedBlocks = Services.chunkData().getPlacedBlocks(world.getChunkAt(pos));
        if (placedBlocks == null) {
            return false;
        }
        int placedStateId = placedBlocks.get(pos.asLong());
        return placedStateId != NO_STATE && matches(placedStateId, state);
    }

    private static boolean matches(int placedStateId, BlockState state) {
        if (!Block.stateById(placedStateId).is(state.getBlock())) {
            return false;
        }
        return !isGrowingBlock(state) || placedStateId == Block.getId(state);
    }

    private static boolean isGrowingBlock(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("age")) {
                return true;
            }
        }
        return false;
    }

    public static Long2IntOpenHashMap createMap() {
        Long2IntOpenHashMap map = new Long2IntOpenHashMap();
        map.defaultReturnValue(NO_STATE);
        return map;
    }

    private static Long2IntOpenHashMap decode(LongStream stream) {
        Long2IntOpenHashMap map = createMap();
        long[] values = stream.toArray();
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(values[i], (int) values[i + 1]);
        }
        return map;
    }

    private static LongStream encode(Long2IntOpenHashMap map) {
        long[] values = new long[map.size() * 2];
        int i = 0;
        for (Long2IntMap.Entry entry : map.long2IntEntrySet()) {
            values[i++] = entry.getLongKey();
            values[i++] = entry.getIntValue();
        }
        return LongStream.of(values);
    }
}
