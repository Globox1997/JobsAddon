package net.jobsaddon.init;

import ht.treechop.api.FellData;
import ht.treechop.api.TreeChopEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EventInit {

    public static final boolean isTreeChopLoaded = FabricLoader.getInstance().isModLoaded("treechop");

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
//            JobLists.builderBlockTagMap.forEach((tagKey, xp) -> {
//                Registries.BLOCK.getOrCreateEntryList(tagKey).forEach(block -> {
//                    JobLists.builderBlockIdMap.put(Registries.BLOCK.getRawId(block.value()), xp);
//                });
//            });
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            JobsManager jobsManager = ((JobsManagerAccess) handler.getPlayer()).getJobsManager();
            JobsServerPacket.writeS2CJobPacket(jobsManager, handler.getPlayer());
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive || !net.levelz.init.ConfigInit.CONFIG.hardMode) {

                JobsManager jobsManager = ((JobsManagerAccess) oldPlayer).getJobsManager();
                JobsManager newJobsManager = ((JobsManagerAccess) newPlayer).getJobsManager();
                // Set on client
                JobsServerPacket.writeS2CJobPacket(jobsManager, newPlayer);
                // Set on server

                boolean resetCurrentJobXP = net.jobsaddon.init.ConfigInit.CONFIG.resetCurrentJobXP;
                for (Job job : jobsManager.getPlayerJobs().values()) {
                    newJobsManager.setJobLevel(job.getId(), job.getLevel());
                    if (!resetCurrentJobXP) {
                        newJobsManager.setJobXP(job.getId(), job.getExperience());
                    }
                }

                if (!jobsManager.getEmployedJobsList().isEmpty()) {
                    for (int jobId : jobsManager.getEmployedJobsList()) {
                        newJobsManager.employJob(jobId);
                    }
                }
                newJobsManager.setEmployedJobTime(jobsManager.getEmployedJobTime());
            }
            if (!alive && net.levelz.init.ConfigInit.CONFIG.hardMode) {
                newPlayer.getScoreboard().forEachScore(CriteriaInit.JOBS, newPlayer, ScoreAccess::resetScore);
                // set timer cause it is hard mode
                ((JobsManagerAccess) newPlayer).getJobsManager().setEmployedJobTime(net.jobsaddon.init.ConfigInit.CONFIG.jobChangeTime);
            }
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            JobsServerPacket.writeS2CJobPacket(((JobsManagerAccess) player).getJobsManager(), player);
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            JobsServerPacket.writeS2CJobPacket(((JobsManagerAccess) oldPlayer).getJobsManager(), newPlayer);
        });
        if (isTreeChopLoaded) {
            TreeChopEvents.BEFORE_FELL.register((world, player, pos, data) -> {
                if (!world.isClient() && player != null && data.getTree().getLogBlocks().isPresent()) {
                    JobHelper.multiBlockBreakJobXp(player, data.getTree().getLogBlocks().get().stream().toList());
                }
                return true;
            });
        }
        if (FabricLoader.getInstance().isModLoaded("bakery")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("bakery_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("betterend")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("betterend_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("betternether")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("betternether_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("candlelight")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("candlelight_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("earlystage")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("earlystage_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("snuffles")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("snuffles_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("supplementaries")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("supplementaries_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
        if (FabricLoader.getInstance().isModLoaded("vinery")) {
            ResourceManagerHelper.registerBuiltinResourcePack(JobsAddonMain.identifierOf("vinery_compat"), FabricLoader.getInstance().getModContainer("jobsaddon").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
    }
}
