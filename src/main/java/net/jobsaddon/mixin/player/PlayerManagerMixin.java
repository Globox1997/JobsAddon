package net.jobsaddon.mixin.player;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.CriteriaInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.levelz.init.ConfigInit;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerConnected(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();
        JobsServerPacket.writeS2CJobPacket(jobsManager, player);
    }

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerRespawned(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void respawnPlayerMixin(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld,
            Optional<Object> optional, ServerWorld serverWorld2, ServerPlayerEntity serverPlayerEntity) {
        if (alive || !ConfigInit.CONFIG.hardMode) {

            JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();
            JobsManager newJobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            // Set on client
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            // Set on server

            boolean resetCurrentJobXP = net.jobsaddon.init.ConfigInit.CONFIG.resetCurrentJobXP;
            boolean keepInventory = serverWorld.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);

            // Job Level
            newJobsManager.setJobLevel("brewer", jobsManager.getJobLevel("brewer"));
            newJobsManager.setJobLevel("builder", jobsManager.getJobLevel("builder"));
            newJobsManager.setJobLevel("farmer", jobsManager.getJobLevel("farmer"));
            newJobsManager.setJobLevel("fisher", jobsManager.getJobLevel("fisher"));
            newJobsManager.setJobLevel("lumberjack", jobsManager.getJobLevel("lumberjack"));
            newJobsManager.setJobLevel("miner", jobsManager.getJobLevel("miner"));
            newJobsManager.setJobLevel("smither", jobsManager.getJobLevel("smither"));
            newJobsManager.setJobLevel("warrior", jobsManager.getJobLevel("warrior"));
            // Job XP
            newJobsManager.setJobXP("brewer", keepInventory ? jobsManager.getJobXP("brewer") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("brewer"));
            newJobsManager.setJobXP("builder", keepInventory ? jobsManager.getJobXP("builder") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("builder"));
            newJobsManager.setJobXP("farmer", keepInventory ? jobsManager.getJobXP("farmer") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("farmer"));
            newJobsManager.setJobXP("fisher", keepInventory ? jobsManager.getJobXP("fisher") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("fisher"));
            newJobsManager.setJobXP("lumberjack", keepInventory ? jobsManager.getJobXP("lumberjack") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("lumberjack"));
            newJobsManager.setJobXP("miner", keepInventory ? jobsManager.getJobXP("miner") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("miner"));
            newJobsManager.setJobXP("smither", keepInventory ? jobsManager.getJobXP("smither") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("smither"));
            newJobsManager.setJobXP("warrior", keepInventory ? jobsManager.getJobXP("warrior") : resetCurrentJobXP ? 0 : jobsManager.getJobXP("warrior"));
        }
        if (ConfigInit.CONFIG.hardMode) {
            serverPlayerEntity.getScoreboard().forEachScore(CriteriaInit.JOBS, serverPlayerEntity.getEntityName(), ScoreboardPlayerScore::clearScore);
        }
    }
}
