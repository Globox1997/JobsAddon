package net.jobsaddon.mixin.misc;

import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "onCraftByPlayer", at = @At("TAIL"))
    private void onCraftByPlayerMixin(ItemStack stack, World world, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient() && stack != null && !stack.isEmpty()) {
            if (!((PlayerAccess) player).isQuickCrafted()
                    && (((PlayerAccess) player).getLastRecipeId() == null || !JobsManager.RESTRICTED_RECIPES.contains(((PlayerAccess) player).getLastRecipeId()))) {
                JobHelper.craftItemJobXp(player, stack);
                ((PlayerAccess) player).setLastRecipeId(null);
            }
            ((PlayerAccess) player).setQuickCraftedRecipe(false);
        }
    }
}
