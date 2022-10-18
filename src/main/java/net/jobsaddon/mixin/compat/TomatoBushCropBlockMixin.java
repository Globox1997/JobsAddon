package net.jobsaddon.mixin.compat;

import com.nhoryzon.mc.farmersdelight.block.TomatoBushCropBlock;
import com.nhoryzon.mc.farmersdelight.registry.ItemsRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(TomatoBushCropBlock.class)
public class TomatoBushCropBlockMixin {

    @Inject(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onUseMixin(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info, int i, boolean bl, int j) {
        if (!world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer")) {
            int xpCount = 0;
            if (JobLists.farmerItemIdMap.containsKey(Registry.ITEM.getRawId(ItemsRegistry.TOMATO.get()))) {
                xpCount += j * JobLists.farmerItemIdMap.get(Registry.ITEM.getRawId(ItemsRegistry.TOMATO.get()));
                if (xpCount > 0)
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
            }
        }
    }
}
