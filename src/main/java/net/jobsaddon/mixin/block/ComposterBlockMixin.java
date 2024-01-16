package net.jobsaddon.mixin.block;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {

    @Inject(method = "addToComposter", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private static void addToComposterMixin(@Nullable Entity user, BlockState state, WorldAccess world, BlockPos pos, ItemStack stack, CallbackInfoReturnable<BlockState> info) {
        if (user instanceof PlayerEntity playerEntity) {
            JobHelper.addFarmerCraftingXp(playerEntity, stack);
        }
    }

}
