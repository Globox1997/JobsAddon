package net.jobsaddon.mixin.misc;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @Nullable
    @Unique
    private ItemStack oldItemStack = null;

    @Inject(method = "onButtonClick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/inventory/Inventory;getStack(I)Lnet/minecraft/item/ItemStack;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onButtonClickMixin(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> info, ItemStack itemStack) {
        oldItemStack = itemStack.copy();
    }

    @Inject(method = "onButtonClick", at = @At(value = "RETURN", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onButtonClickMixin(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> info, ItemStack itemStack, ItemStack itemStack2) {
        if (info.getReturnValue()) {
            JobHelper.addBrewerEnchantmentXp(player, itemStack, oldItemStack);
        }
    }
}
