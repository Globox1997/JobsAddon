package net.jobsaddon.mixin.block;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "afterBreak", at = @At(value = "HEAD"))
    private void afterBreakMixin(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo info) {
        if (!world.isClient()) {
            JobHelper.blockBreakJobXp(player, state);
        }
    }

    @Inject(method = "onPlaced", at = @At(value = "HEAD"))
    private void onPlacedMixin(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo info) {
        if (placer instanceof ServerPlayerEntity serverPlayerEntity) {
            JobHelper.blockPlaceJobXp(serverPlayerEntity, pos, state);
        }
    }

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("TAIL"))
    private static void getDroppedStacksMixin(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> info) {
        if (entity instanceof ServerPlayerEntity serverPlayerEntity && !info.getReturnValue().isEmpty()) {
            JobHelper.itemDropJobXp(serverPlayerEntity, info.getReturnValue());
        }
    }

}
