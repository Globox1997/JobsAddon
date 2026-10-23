package net.jobsaddon.mixin.misc;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @Shadow
    @Final
    private Inventory inventory;

    @Unique
    private ItemStack oldItemStack = ItemStack.EMPTY;

    @Inject(method = "onButtonClick", at = @At("HEAD"))
    private void onButtonClickHeadMixin(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> info) {
        this.oldItemStack = this.inventory.getStack(0).copy();
    }

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void onButtonClickReturnMixin(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ()) {
            JobHelper.enchantingJobXp(player, this.inventory.getStack(0), this.oldItemStack);
        }
        this.oldItemStack = ItemStack.EMPTY;
    }
}
