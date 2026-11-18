package net.jobsaddon.mixin.block;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CaveVines.class)
public interface CaveVinesMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static void pickBerriesMixin(@Nullable Entity picker, BlockState state, Level world, BlockPos pos, CallbackInfoReturnable<InteractionResult> info) {
        if (!world.isClientSide() && picker instanceof Player player) {
            JobHelper.harvestJobXp(player, state);
        }
    }
}
