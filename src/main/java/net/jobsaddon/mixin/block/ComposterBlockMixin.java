package net.jobsaddon.mixin.block;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jobsaddon.jobs.JobExperienceType;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {

    @Inject(method = "addItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static void addToComposterMixin(@Nullable Entity user, BlockState state, LevelAccessor world, BlockPos pos, ItemStack stack, CallbackInfoReturnable<BlockState> info) {
        if (user instanceof Player playerEntity) {
            JobHelper.craftItemJobXp(playerEntity, stack, 1, JobExperienceType.CRAFTING);
        }
    }

}
