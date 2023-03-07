package net.jobsaddon.mixin.compat;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import ht.treechop.common.chop.ChopResult;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;

@Mixin(ChopResult.class)
public class ChopResultMixin {

    @Inject(method = "Lht/treechop/common/chop/ChopResult;apply(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/item/ItemStack;Z)Z", at = @At(value = "INVOKE", target = "Lht/treechop/common/chop/ChopResult;fellBlocks(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;Ljava/util/stream/Stream;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void applyMixin(BlockPos targetPos, ServerPlayerEntity player, ItemStack tool, boolean breakLeaves, CallbackInfoReturnable<Boolean> info, ServerWorld level, GameMode gameType,
            AtomicBoolean somethingChanged, List<BlockPos> logs) {
        if (player != null && !level.isClient) {
            if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("lumberjack") && ((PlayerAccess) player).setLastBlockId(targetPos, false, 0)) {
                int xpCount = 0;
                for (int i = 0; i < logs.size(); i++) {
                    if (JobLists.lumberjackBlockIdMap.containsKey(Registry.BLOCK.getRawId(level.getBlockState(logs.get(i)).getBlock())))
                        xpCount += JobLists.lumberjackBlockIdMap.get(Registry.BLOCK.getRawId(level.getBlockState(logs.get(i)).getBlock()));
                    else
                        xpCount += ConfigInit.CONFIG.lumberjackXP;
                }
                if (xpCount > 0)
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "lumberjack", xpCount);
            }
        }
    }
}
