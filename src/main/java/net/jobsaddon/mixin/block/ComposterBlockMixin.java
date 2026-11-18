package net.jobsaddon.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.jobsaddon.jobs.JobExperienceType;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ComposterBlock;addLayer(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/component/Compostable;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState addToComposterMixin(@Nullable Entity user, BlockState state, ServerLevel world, BlockPos pos, Compostable compostable, Operation<BlockState> original, @Local(argsOnly = true) ItemStack stack) {
        BlockState newState = original.call(user, state, world, pos, compostable);
        if (newState != state && user instanceof Player playerEntity) {
            JobHelper.craftItemJobXp(playerEntity, stack, 1, JobExperienceType.CRAFTING);
        }
        return newState;
    }

}
