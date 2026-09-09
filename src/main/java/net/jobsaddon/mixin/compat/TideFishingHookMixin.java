package net.jobsaddon.mixin.compat;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(TideFishingHook.class)
public class TideFishingHookMixin {

    @Shadow
    protected List<ItemStack> hookedItems;

//    @Inject(method = "retrieve(Lnet/minecraft/item/ItemStack;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/player/PlayerEntity;)I", at = @At(value = "INVOKE", target = "Lcom/li64/tide/loaders/LoaderPlatform;isModLoaded(Ljava/lang/String;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void retrieveMixin(ItemStack rod, ServerWorld level, PlayerEntity player, CallbackInfoReturnable<Integer> cir, ServerPlayerEntity serverPlayer, int i, List hookedItemsCopy, Iterator var7, ItemStack stack) {
//        if (player != null && !stack.isEmpty()) {
//            JobHelper.itemDropJobXp(player, List.of(stack));
//        }
//    }

    @Inject(method = "retrieve(Lnet/minecraft/item/ItemStack;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/player/PlayerEntity;)I", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"))
    private void retrieveMixin(ItemStack rod, ServerWorld level, PlayerEntity player, CallbackInfoReturnable<Integer> info) {
        System.out.println("YOYOYO "+hookedItems);
        if (player != null && !hookedItems.isEmpty()) {
            JobHelper.itemDropJobXp(player, hookedItems);
        }
    }

}
