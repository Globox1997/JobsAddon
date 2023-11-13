package net.jobsaddon.mixin.misc;

import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
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

        if (!world.isClient() && stack != null && !stack.isEmpty()) {
            boolean isQuickCrafted = ((PlayerAccess) player).isQuickCrafted();
            boolean isRestricted = JobLists.restrictedRecipeIds.contains(((PlayerAccess) player).getLastRecipeId());
            Identifier receipt = ((PlayerAccess) player).getLastRecipeId();


            // We need to rely on this check, because of a bug in 1.19 causing wrong setLastRecipeId
            boolean isFurnace = player.currentScreenHandler instanceof FurnaceScreenHandler || player.currentScreenHandler instanceof BlastFurnaceScreenHandler;

            if (!isQuickCrafted && ((receipt == null || !isRestricted) || isFurnace)) {
                JobHelper.addSmitherXp(player, stack);
                JobHelper.addFisherCraftingXp(player, stack);
                JobHelper.addFarmerCraftingXp(player, stack);
                ((PlayerAccess) player).setLastRecipeId(null);
            }
            ((PlayerAccess) player).setQuickCraftedRecipe(false);
        }
    }
}

