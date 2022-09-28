package net.jobsaddon.mixin.entity;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void useMixin(ItemStack usedItem, CallbackInfoReturnable<Integer> info, PlayerEntity playerEntity, int i, LootContext.Builder builder, LootTable lootTable, List<ItemStack> list,
            Iterator<ItemStack> var7, ItemStack itemStack) {
        if (((JobsManagerAccess) getPlayerOwner()).getJobsManager().isEmployedJob("fisher")) {
            int xpCount = 0;
            if (JobLists.fisherItemIdMap.containsKey(Registry.ITEM.getRawId(itemStack.getItem())))
                xpCount = itemStack.getCount() * JobLists.fisherItemIdMap.get(Registry.ITEM.getRawId(itemStack.getItem()));
            else
                xpCount = itemStack.getCount() * ConfigInit.CONFIG.fisherXP;
            JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) getPlayerOwner(), "fisher", xpCount);
        }

    }

    @Shadow
    public PlayerEntity getPlayerOwner() {
        return null;
    }
}
