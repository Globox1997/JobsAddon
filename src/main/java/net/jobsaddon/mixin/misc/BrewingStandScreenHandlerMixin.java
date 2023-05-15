package net.jobsaddon.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(targets = "net/minecraft/screen/BrewingStandScreenHandler$PotionSlot")
public class BrewingStandScreenHandlerMixin {

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void onTakeItemMixin(PlayerEntity player, ItemStack stack, CallbackInfo info) {
        JobHelper.addBrewerXp(player, stack);
    }
}
