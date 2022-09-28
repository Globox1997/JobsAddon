package net.jobsaddon.mixin.block;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(TntBlock.class)
public class TntBlockMixin {

    @Inject(method = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private static void primeTnt(World world, BlockPos pos, @Nullable LivingEntity igniter, CallbackInfo info) {
        if (igniter != null && igniter instanceof PlayerEntity && ((JobsManagerAccess) igniter).getJobsManager().isEmployedJob("miner"))
            JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) igniter, "miner", ConfigInit.CONFIG.minerXP);
    }
}
