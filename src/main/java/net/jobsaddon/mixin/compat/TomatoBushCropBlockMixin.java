package net.jobsaddon.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;

@Mixin(TomatoVineBlock.class)
public class TomatoBushCropBlockMixin {

    @Inject(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onUseMixin(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir, int age, boolean isMature, int quantity) {
//        if (!level.isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer")) {
//            int xpCount = 0;
//            if (JobLists.farmerItemIdMap.containsKey(Registries.ITEM.getRawId(ItemsRegistry.TOMATO.get()))) {
//                xpCount += j * JobLists.farmerItemIdMap.get(Registries.ITEM.getRawId(ItemsRegistry.TOMATO.get()));
//                if (xpCount > 0) {
//                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
//                }
//            }
//        }
    }
}
