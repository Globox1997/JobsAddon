package net.jobsaddon.init;

import ht.treechop.api.TreeChopEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventInit {

    public static final boolean isTreeChopLoaded = FabricLoader.getInstance().isModLoaded("treechop");

    public static void init() {
        if (isTreeChopLoaded) {
            TreeChopEvents.AFTER_CHOP.register((world, player, pos, state, data, felled) -> {
                if (player != null && !world.isClient() && felled) {
                    if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("lumberjack") && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
                        int xpCount = 0;
                        if (JobLists.lumberjackBlockIdMap.containsKey(Registries.BLOCK.getRawId(state.getBlock()))) {
                            xpCount += JobLists.lumberjackBlockIdMap.get(Registries.BLOCK.getRawId(state.getBlock())) * data.getNumChops();
                        } else {
                            xpCount += ConfigInit.CONFIG.lumberjackXP * data.getNumChops();
                        }
                        if (xpCount > 0) {
                            JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "lumberjack", xpCount);
                        }
                        // for (int i = 0; i < logs.size(); i++) {
                        // if (JobLists.lumberjackBlockIdMap.containsKey(Registries.BLOCK.getRawId(level.getBlockState(logs.get(i)).getBlock())))
                        // xpCount += JobLists.lumberjackBlockIdMap.get(Registries.BLOCK.getRawId(level.getBlockState(logs.get(i)).getBlock()));
                        // else
                        // xpCount += ConfigInit.CONFIG.lumberjackXP;
                        // }
                        // if (xpCount > 0) {
                        // JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "lumberjack", xpCount);
                        // }
                    }
                }
            });
        }
    }
}
