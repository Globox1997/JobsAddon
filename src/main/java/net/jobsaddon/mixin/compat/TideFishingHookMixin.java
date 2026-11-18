package net.jobsaddon.mixin.compat;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TideFishingHook.class)
public class TideFishingHookMixin {

    @Shadow
    protected List<ItemStack> hookedItems;

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;)I", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"))
    private void retrieveMixin(ItemStack rod, ServerLevel level, Player player, CallbackInfoReturnable<Integer> info) {
        if (player != null && !hookedItems.isEmpty()) {
            JobHelper.itemDropJobXp(player, hookedItems);
        }
    }

}
