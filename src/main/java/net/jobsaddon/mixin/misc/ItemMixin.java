package net.jobsaddon.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "onCraft", at = @At("TAIL"))
    private void onCraftMixin(ItemStack stack, World world, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient && stack != null && !stack.isEmpty()) {
            if (((PlayerAccess) player).getLastRecipeId() == null || !JobLists.restrictedRecipeIds.contains(((PlayerAccess) player).getLastRecipeId())) {
                JobHelper.addSmitherXp(player, stack);
                JobHelper.addFisherCraftingXp(player, stack);
                JobHelper.addFarmerCraftingXp(player, stack);
            }
            ((PlayerAccess) player).setLastRecipeId(null);
        }
    }
}
