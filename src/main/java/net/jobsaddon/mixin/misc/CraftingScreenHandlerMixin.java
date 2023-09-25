package net.jobsaddon.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jobsaddon.access.PlayerAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"))
    private void quickMoveMixin(PlayerEntity player, int slot, CallbackInfoReturnable<ItemStack> info) {
        if (!player.getWorld().isClient()) {
            ((PlayerAccess) player).setQuickCraftedRecipe(true);
        }
    }
}
