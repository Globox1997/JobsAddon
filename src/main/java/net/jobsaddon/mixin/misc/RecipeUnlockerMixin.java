package net.jobsaddon.mixin.misc;

import net.jobsaddon.access.PlayerAccess;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeUnlocker.class)
public interface RecipeUnlockerMixin {

    @Inject(method = "shouldCraftRecipe", at = @At("HEAD"))
    default public void shouldCraftRecipeMixin(World world, ServerPlayerEntity player, RecipeEntry<?> recipe, CallbackInfoReturnable<Boolean> info) {
        ((PlayerAccess) player).setLastRecipeId(recipe.id());
    }
}
