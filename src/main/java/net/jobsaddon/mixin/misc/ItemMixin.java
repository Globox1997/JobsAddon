package net.jobsaddon.mixin.misc;

import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "onCraftedBy", at = @At("TAIL"))
    private void onCraftByPlayerMixin(ItemStack stack, Player player, CallbackInfo info) {
        if (!player.level().isClientSide() && stack != null && !stack.isEmpty()) {
            if (((PlayerAccess) player).isQuickCrafted()) {
                ((PlayerAccess) player).setQuickCraftedRecipe(false);
                return;
            }
            if (!JobHelper.isRestrictedRecipe(player, stack)) {
                JobHelper.craftItemJobXp(player, stack);
            }
        }
    }
}
