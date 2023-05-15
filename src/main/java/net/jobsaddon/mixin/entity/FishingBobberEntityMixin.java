package net.jobsaddon.mixin.entity;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void useMixin(ItemStack usedItem, CallbackInfoReturnable<Integer> info, PlayerEntity playerEntity, int i, LootContext.Builder builder, LootTable lootTable, List<ItemStack> list,
            Iterator<ItemStack> var7, ItemStack itemStack) {
        if (getPlayerOwner() != null) {
            JobHelper.addFisherXp(getPlayerOwner(), itemStack);
        }

    }

    @Nullable
    @Shadow
    public PlayerEntity getPlayerOwner() {
        return null;
    }
}
