package net.jobsaddon.mixin.block;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onBreak", at = @At(value = "HEAD"))
    private void onBreakMixin(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient) {
            JobHelper.addMinerXp(player, pos, state);
            JobHelper.addLumberjackXp(player, pos, state);
            JobHelper.addFarmerBlockDropXp(player, pos, state);
        }
    }

    @Inject(method = "onPlaced", at = @At(value = "HEAD"))
    private void onPlacedMixin(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo info) {
        if (placer != null && placer instanceof ServerPlayerEntity) {
            JobHelper.addBuilderXp((ServerPlayerEntity) placer, pos, state);
        }
    }
}
