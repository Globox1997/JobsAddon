package net.jobsaddon.mixin.misc;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public class EnchantmentScreenHandlerMixin {

    @Shadow
    @Final
    private Container enchantSlots;

    @Unique
    private ItemStack oldItemStack = ItemStack.EMPTY;

    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void onButtonClickHeadMixin(Player player, int id, CallbackInfoReturnable<Boolean> info) {
        this.oldItemStack = this.enchantSlots.getItem(0).copy();
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void onButtonClickReturnMixin(Player player, int id, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ()) {
            JobHelper.enchantingJobXp(player, this.enchantSlots.getItem(0), this.oldItemStack);
        }
        this.oldItemStack = ItemStack.EMPTY;
    }
}
