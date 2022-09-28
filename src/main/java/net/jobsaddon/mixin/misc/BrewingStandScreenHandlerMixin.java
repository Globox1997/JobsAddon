package net.jobsaddon.mixin.misc;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(targets = "net/minecraft/screen/BrewingStandScreenHandler$PotionSlot")
public class BrewingStandScreenHandlerMixin {

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void onTakeItemMixin(PlayerEntity player, ItemStack stack, CallbackInfo info) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("brewer")) {
            Optional<ItemStack> optional = JobLists.brewerItemBrewingMap.keySet().stream().filter(potion -> ItemStack.canCombine(potion, stack)).findFirst();

            int xpCount = 0;
            if (optional.isPresent())
                xpCount += stack.getCount() * JobLists.brewerItemBrewingMap.get(optional.get());
            else
                xpCount += stack.getCount() * ConfigInit.CONFIG.brewerXP;

            JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "brewer", xpCount);
        }
    }
}
