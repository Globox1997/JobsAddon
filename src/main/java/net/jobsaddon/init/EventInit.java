package net.jobsaddon.init;

import ht.treechop.api.TreeChopEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EventInit {

    public static final boolean isTreeChopLoaded = FabricLoader.getInstance().isModLoaded("treechop");

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            JobLists.builderBlockTagMap.forEach((tagKey, xp) -> {
                Registries.BLOCK.getOrCreateEntryList(tagKey).forEach(block -> {
                    JobLists.builderBlockIdMap.put(Registries.BLOCK.getRawId(block.value()), xp);
                });
            });
        });

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
                    }
                }
            });
        }
        if (FabricLoader.getInstance().isModLoaded("bakery")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "bakery_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("betterend")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "betterend_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("betternether")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "betternether_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("candlelight")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "candlelight_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("earlystage")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "earlystage_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("snuffles")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "snuffles_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("supplementaries")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "supplementaries_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("vinery")) {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("jobsaddon", "vinery_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
    }
}
