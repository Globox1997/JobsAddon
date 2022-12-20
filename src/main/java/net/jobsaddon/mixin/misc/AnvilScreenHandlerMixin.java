package net.jobsaddon.mixin.misc;

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
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    protected void onTakeOutputMixin(PlayerEntity player, ItemStack stack, CallbackInfo info) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("smither")) {
            int xpCount = 0;
            if (JobLists.smitherItemIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
                xpCount = stack.getCount() * JobLists.smitherItemIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
            else
                xpCount = stack.getCount() * ConfigInit.CONFIG.smitherXP;

            if (xpCount > 0)
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "smither", xpCount);
        }
    }
}
