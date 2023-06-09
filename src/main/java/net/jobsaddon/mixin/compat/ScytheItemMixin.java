package net.jobsaddon.mixin.compat;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.TagInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wraith.harvest_scythes.item.ScytheItem;

@Mixin(ScytheItem.class)
public class ScytheItemMixin {

    @Inject(method = "harvest", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void harvestMixin(int harvestRadius, int miningLevel, World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info, BlockPos blockPos,
            ItemStack stack, Item item, int lvl, boolean prematureHarvest, int radius, boolean circleHarvest, int totalBlocks, int totalDamage, int x, int y, int z, BlockPos cropPos,
            BlockState blockState, Block block, int damageTool, boolean canHarvest, CropBlock cropBlock, List<ItemStack> drops) {
        if (!world.isClient && ((JobsManagerAccess) user).getJobsManager().isEmployedJob("farmer")) {
            int xpCount = 0;
            for (int i = 0; i < drops.size(); i++)
                if (drops.get(i).isIn(TagInit.FARMER_BREAKING_ITEMS)) {
                    if (JobLists.farmerItemIdMap.containsKey(Registries.ITEM.getRawId(drops.get(i).getItem())))
                        xpCount += drops.get(i).getCount() * JobLists.farmerItemIdMap.get(Registries.ITEM.getRawId(drops.get(i).getItem()));
                    else
                        xpCount += drops.get(i).getCount() * ConfigInit.CONFIG.farmerXP;
                }

            if (xpCount > 0)
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) user, "farmer", xpCount);
        }
    }
}
