package net.jobsaddon.mixin.compat;

import com.nhoryzon.mc.farmersdelight.entity.block.inventory.slot.CookingPotResultSlot;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
@Mixin(CookingPotResultSlot.class)
public class CookingPotResultSlotMixin {

    // @Shadow
    // @Final
    // @Mutable
    // private PlayerEntity player;

    // @Inject(method = "onCrafted", at = @At("HEAD"))
    // protected void onCrafted(ItemStack stack) {
        // if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher") && stack.isIn(TagInit.FISHER_CRAFTING_ITEMS)) {
        //     int xpCount = 0;
        //     if (JobLists.fisherCraftingIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
        //         xpCount = stack.getCount() * JobLists.fisherCraftingIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
        //     else
        //         xpCount = stack.getCount() * ConfigInit.CONFIG.fisherXP;
        //     JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "fisher", xpCount);

        // }
        // if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer") && (stack.isFood() || stack.isIn(TagInit.FARMER_CRAFTING_ITEMS))) {
        //     int xpCount = 0;
        //     if (JobLists.farmerCraftingIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem())))
        //         xpCount = stack.getCount() * JobLists.farmerCraftingIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
        //     else
        //         xpCount = stack.getCount() * ConfigInit.CONFIG.farmerXP;
        //     JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
        // }
   // }

}
