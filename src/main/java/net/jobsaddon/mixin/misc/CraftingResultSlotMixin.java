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
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    @Shadow
    @Mutable
    @Final
    private PlayerEntity player;

    @Inject(method = "Lnet/minecraft/screen/slot/CraftingResultSlot;onCrafted(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    protected void onCraftedMixin(ItemStack stack, CallbackInfo info) {
        if (!player.world.isClient && !stack.isEmpty()) {
            if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("smither") && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof ArmorItem
                    || stack.getItem() instanceof ToolItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || stack.isIn(TagInit.SMITHER_CRAFTING_ITEMS))) {
                int xpCount = 0;
                if (JobLists.smitherCraftingIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
                    xpCount = stack.getCount() * JobLists.smitherCraftingIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
                else
                    xpCount = stack.getCount() * ConfigInit.CONFIG.smitherXP;
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "smither", xpCount);
            }

            if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher") && stack.isIn(TagInit.FISHER_CRAFTING_ITEMS)) {
                int xpCount = 0;
                if (JobLists.fisherCraftingIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
                    xpCount = stack.getCount() * JobLists.fisherCraftingIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
                else
                    xpCount = stack.getCount() * ConfigInit.CONFIG.fisherXP;
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "fisher", xpCount);

            }
            if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer") && (stack.isFood() || stack.isIn(TagInit.FARMER_CRAFTING_ITEMS))) {

                int xpCount = 0;
                if (JobLists.farmerCraftingIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
                    xpCount = stack.getCount() * JobLists.farmerCraftingIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
                else
                    xpCount = stack.getCount() * ConfigInit.CONFIG.farmerXP;
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
            }

        }
    }
}
