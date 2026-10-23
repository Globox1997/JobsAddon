package net.jobsaddon.mixin.block;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CaveVines.class)
public interface CaveVinesMixin {

    @Inject(method = "pickBerries", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private static void pickBerriesMixin(@Nullable Entity picker, BlockState state, World world, BlockPos pos, CallbackInfoReturnable<ActionResult> info) {
        if (!world.isClient() && picker instanceof PlayerEntity player) {
            JobHelper.harvestJobXp(player, state);
        }
    }
}
