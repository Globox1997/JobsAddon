package net.jobsaddon.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jobsaddon.access.PlayerAccess;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(RecipeUnlocker.class)
public interface RecipeUnlockerMixin {

    @Inject(method = "shouldCraftRecipe", at = @At("HEAD"))
    default public void shouldCraftRecipeMixin(World world, ServerPlayerEntity player, Recipe<?> recipe, CallbackInfoReturnable<Boolean> info) {
        ((PlayerAccess) player).setLastRecipeId(recipe.getId());
    }
}
