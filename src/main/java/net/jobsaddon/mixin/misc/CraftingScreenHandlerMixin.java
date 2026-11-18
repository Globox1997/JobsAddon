package net.jobsaddon.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jobsaddon.access.PlayerAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(CraftingMenu.class)
public class CraftingScreenHandlerMixin {

    @Inject(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;onCraftedBy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V"))
    private void quickMoveMixin(Player player, int slot, CallbackInfoReturnable<ItemStack> info) {
        if (!player.level().isClientSide()) {
            ((PlayerAccess) player).setQuickCraftedRecipe(true);
        }
    }
}
