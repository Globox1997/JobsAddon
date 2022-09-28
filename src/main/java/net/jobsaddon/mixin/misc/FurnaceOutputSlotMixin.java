package net.jobsaddon.mixin.misc;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.TagInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

@Mixin(FurnaceOutputSlot.class)
public class FurnaceOutputSlotMixin {

    @Shadow
    @Mutable
    @Final
    private PlayerEntity player;

    @Inject(method = "Lnet/minecraft/screen/slot/FurnaceOutputSlot;onCrafted(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    protected void onCraftedMixin(ItemStack stack, CallbackInfo info) {
        if (!player.world.isClient && !stack.isEmpty()) {
            if (player.currentScreenHandler instanceof BlastFurnaceScreenHandler || stack.isIn(TagInit.SMITHER_FURNACE_ITEMS)) {
                if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("smither")) {
                    int xpCount = 0;
                    if (JobLists.smitherBlastFurnaceIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
                        xpCount = stack.getCount() * JobLists.smitherBlastFurnaceIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
                    else
                        xpCount = stack.getCount() * ConfigInit.CONFIG.smitherXP;
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "smither", xpCount);
                }
            } else if (player.currentScreenHandler instanceof SmokerScreenHandler || stack.isIn(TagInit.FARMER_FURNACE_ITEMS))
                if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer")) {
                    int xpCount = 0;
                    if (JobLists.farmerSmokerIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
                        xpCount = stack.getCount() * JobLists.farmerSmokerIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
                    else
                        xpCount = stack.getCount() * ConfigInit.CONFIG.farmerXP;
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
                }
        }
    }
}
