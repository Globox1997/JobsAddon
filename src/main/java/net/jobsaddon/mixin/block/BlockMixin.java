package net.jobsaddon.mixin.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.TagInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onBreak", at = @At(value = "HEAD"))
    private void onBreakMixin(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient) {
            if (state.isIn(TagInit.MINER_BLOCKS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("miner") && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
                if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, player.getMainHandStack()) == 0) {
                    System.out.println(player.getMainHandStack());
                    int xpCount = 0;
                    if (JobLists.minerBlockIdMap.containsKey(Registry.BLOCK.getRawId(state.getBlock())))
                        xpCount = JobLists.minerBlockIdMap.get(Registry.BLOCK.getRawId(state.getBlock()));
                    else
                        xpCount = ConfigInit.CONFIG.minerXP;
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "miner", xpCount);
                }
            }

            if (state.isIn(BlockTags.LOGS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("lumberjack") && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
                int xpCount = 0;
                if (JobLists.lumberjackBlockIdMap.containsKey(Registry.BLOCK.getRawId(state.getBlock())))
                    xpCount = JobLists.lumberjackBlockIdMap.get(Registry.BLOCK.getRawId(state.getBlock()));
                else
                    xpCount = ConfigInit.CONFIG.lumberjackXP;
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "lumberjack", xpCount);
            }
            if (state.getBlock() instanceof PlantBlock && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer") && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
                int xpCount = 0;
                List<ItemStack> list = Block.getDroppedStacks(state, (ServerWorld) world, pos, null);
                for (int i = 0; i < list.size(); i++)
                    if (list.get(i).isIn(TagInit.FARMER_BREAKING_ITEMS)) {
                        if (JobLists.farmerItemIdMap.containsKey(Registry.ITEM.getRawId(list.get(i).getItem())))
                            xpCount += list.get(i).getCount() * JobLists.farmerItemIdMap.get(Registry.ITEM.getRawId(list.get(i).getItem()));
                        else
                            xpCount += list.get(i).getCount() * ConfigInit.CONFIG.farmerXP;
                    }

                if (xpCount > 0)
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
            }
        }
    }

    @Inject(method = "onPlaced", at = @At(value = "HEAD"))
    private void onPlacedMixin(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo info) {
        if (!world.isClient && placer != null && placer instanceof ServerPlayerEntity)
            if (state.isIn(TagInit.BUILDER_PLACING_BLOCKS) && ((JobsManagerAccess) placer).getJobsManager().isEmployedJob("builder")) {
                if (((PlayerAccess) placer).setLastBlockId(pos, true, Registry.BLOCK.getRawId(state.getBlock()))) {

                    int xpCount = 0;
                    if (JobLists.builderBlockIdMap.containsKey(Registry.BLOCK.getRawId(state.getBlock())))
                        xpCount = JobLists.builderBlockIdMap.get(Registry.BLOCK.getRawId(state.getBlock()));
                    else
                        xpCount = ConfigInit.CONFIG.builderXP;

                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) placer, "builder", xpCount);
                }
            }
    }
}
