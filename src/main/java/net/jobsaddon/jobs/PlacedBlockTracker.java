package net.jobsaddon.jobs;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.init.ConfigInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import java.util.stream.LongStream;

public class PlacedBlockTracker {

    private static final int NO_STATE = -1;

    private static final Codec<Long2IntOpenHashMap> CODEC = Codec.LONG_STREAM.xmap(PlacedBlockTracker::decode, PlacedBlockTracker::encode);

    public static final AttachmentType<Long2IntOpenHashMap> PLACED_BLOCKS = AttachmentRegistry.<Long2IntOpenHashMap>builder().persistent(CODEC).initializer(PlacedBlockTracker::createMap)
            .buildAndRegister(JobsAddonMain.identifierOf("placed_blocks"));

    public static void init() {
    }

    public static boolean onPlayerPlaced(Level world, BlockPos pos, BlockState state) {
        if (!ConfigInit.CONFIG.trackPlacedBlocks || world.isClientSide()) {
            return true;
        }
        LevelChunk chunk = world.getChunkAt(pos);
        int stateId = Block.getId(state);
        int previousStateId = chunk.getAttachedOrCreate(PLACED_BLOCKS).put(pos.asLong(), stateId);
        chunk.setUnsaved(true);
        return previousStateId == NO_STATE || !matches(previousStateId, state);
    }

    public static boolean isPlayerPlaced(Level world, BlockPos pos, BlockState state) {
        if (!ConfigInit.CONFIG.trackPlacedBlocks || world.isClientSide()) {
            return false;
        }
        Long2IntOpenHashMap placedBlocks = world.getChunkAt(pos).getAttached(PLACED_BLOCKS);
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

    private static Long2IntOpenHashMap createMap() {
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
