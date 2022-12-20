package net.jobsaddon.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wraith.harvest_scythes.api.event.HarvestEvent;
import wraith.harvest_scythes.item.MacheteItem;

@SuppressWarnings("unused")
@Mixin(MacheteItem.class)
public class MacheteItemMixin {

    // @Inject(method = "tryHarvest", at = @At(value = "INVOKE", target = ""))
    // private static void tryHarvestMixin(World world, PlayerEntity player, BlockPos pos, BlockState blockState, BlockEntity blockEntity, CallbackInfo info) {
    // }

}
