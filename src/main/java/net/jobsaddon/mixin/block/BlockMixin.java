package net.jobsaddon.mixin.block;

import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.PlacedBlockTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "playerDestroy", at = @At(value = "HEAD"))
    private void afterBreakMixin(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo info) {
        if (!world.isClientSide()) {
            JobHelper.blockBreakJobXp(player, world, pos, state);
        }
    }

    @Inject(method = "setPlacedBy", at = @At(value = "HEAD"))
    private void onPlacedMixin(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo info) {
        if (placer instanceof ServerPlayer serverPlayerEntity && PlacedBlockTracker.onPlayerPlaced(world, pos, state)) {
            JobHelper.blockPlaceJobXp(serverPlayerEntity, pos, state);
        }
    }

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("TAIL"))
    private static void getDroppedStacksMixin(BlockState state, ServerLevel world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> info) {
        if (entity instanceof ServerPlayer serverPlayerEntity && !info.getReturnValue().isEmpty() && !PlacedBlockTracker.isPlayerPlaced(world, pos, state)) {
            JobHelper.itemDropJobXp(serverPlayerEntity, info.getReturnValue());
        }
    }

}
