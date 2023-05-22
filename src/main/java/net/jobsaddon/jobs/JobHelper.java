package net.jobsaddon.jobs;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.TagInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class JobHelper {

    public static void addSmitherXp(PlayerEntity player, ItemStack stack) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("smither")) {
            int xpCount = 0;
            if (JobLists.smitherItemIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.smitherItemIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
            } else {
                xpCount = stack.getCount() * ConfigInit.CONFIG.smitherXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "smither", xpCount);
            }
        }
    }

    public static void addFisherXp(PlayerEntity player, ItemStack stack) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher")) {
            int xpCount = 0;
            if (JobLists.farmerItemIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.farmerItemIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
            } else {
                xpCount = stack.getCount() * ConfigInit.CONFIG.fisherXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "fisher", xpCount);
            }
        }
    }

    public static void addFisherCraftingXp(PlayerEntity player, ItemStack stack) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher") && stack.isIn(TagInit.FISHER_CRAFTING_ITEMS)) {
            int xpCount = 0;
            if (JobLists.fisherCraftingIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.fisherCraftingIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
            } else {
                xpCount = stack.getCount() * ConfigInit.CONFIG.fisherXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "fisher", xpCount);
            }
        }
    }

    public static void addFisherEntityXp(PlayerEntity player, LivingEntity entity) {
        if (!player.world.isClient && entity instanceof WaterCreatureEntity) {
            if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher")) {
                int xpCount = 0;
                if (JobLists.fisherEntityIdMap.containsKey(Registry.ENTITY_TYPE.getRawId(entity.getType()))) {
                    xpCount = JobLists.fisherEntityIdMap.get(Registry.ENTITY_TYPE.getRawId(entity.getType()));
                } else {
                    xpCount = ConfigInit.CONFIG.fisherXP;
                }
                if (xpCount > 0) {
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "fisher", xpCount);
                }
            }
        }
    }

    public static void addFarmerCraftingXp(PlayerEntity player, ItemStack stack) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer") && (stack.isFood() || stack.isIn(TagInit.FARMER_CRAFTING_ITEMS))) {
            int xpCount = 0;
            if (JobLists.farmerCraftingIdMap.containsKey(Registry.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.farmerCraftingIdMap.get(Registry.ITEM.getRawId(stack.getItem()));
            } else {
                xpCount = stack.getCount() * ConfigInit.CONFIG.farmerXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
            }
        }
    }

    public static void addFarmerBlockDropXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (!player.world.isClient && state.getBlock() instanceof PlantBlock && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer")
                && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
            int xpCount = 0;
            List<ItemStack> list = Block.getDroppedStacks(state, (ServerWorld) player.getWorld(), pos, null);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isIn(TagInit.FARMER_BREAKING_ITEMS)) {
                    if (JobLists.farmerItemIdMap.containsKey(Registry.ITEM.getRawId(list.get(i).getItem()))) {
                        xpCount += list.get(i).getCount() * JobLists.farmerItemIdMap.get(Registry.ITEM.getRawId(list.get(i).getItem()));
                    } else {
                        xpCount += list.get(i).getCount() * ConfigInit.CONFIG.farmerXP;
                    }
                }
            }

            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
            }
        }
    }

    public static void addLumberjackXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (!player.world.isClient && state.isIn(BlockTags.LOGS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("lumberjack")
                && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
            int xpCount = 0;
            if (JobLists.lumberjackBlockIdMap.containsKey(Registry.BLOCK.getRawId(state.getBlock()))) {
                xpCount = JobLists.lumberjackBlockIdMap.get(Registry.BLOCK.getRawId(state.getBlock()));
            } else {
                xpCount = ConfigInit.CONFIG.lumberjackXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "lumberjack", xpCount);
            }
        }
    }

    public static void addMinerXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (!player.world.isClient && state.isIn(TagInit.MINER_BLOCKS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("miner")
                && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, player.getMainHandStack()) == 0) {
                int xpCount = 0;
                if (JobLists.minerBlockIdMap.containsKey(Registry.BLOCK.getRawId(state.getBlock()))) {
                    xpCount = JobLists.minerBlockIdMap.get(Registry.BLOCK.getRawId(state.getBlock()));
                } else {
                    xpCount = ConfigInit.CONFIG.minerXP;
                }
                if (xpCount > 0) {
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "miner", xpCount);
                }
            }
        }
    }

    public static void addBuilderXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (player != null && !player.world.isClient && state.isIn(TagInit.BUILDER_PLACING_BLOCKS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("builder")) {
            if (((PlayerAccess) player).setLastBlockId(pos, true, Registry.BLOCK.getRawId(state.getBlock()))) {
                int xpCount = 0;
                if (JobLists.builderBlockIdMap.containsKey(Registry.BLOCK.getRawId(state.getBlock()))) {
                    xpCount = JobLists.builderBlockIdMap.get(Registry.BLOCK.getRawId(state.getBlock()));
                } else {
                    xpCount = ConfigInit.CONFIG.builderXP;
                }
                if (xpCount > 0) {
                    JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "builder", xpCount);
                }
            }
        }
    }

    public static void addBrewerXp(PlayerEntity player, ItemStack stack) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("brewer")) {
            Optional<ItemStack> optional = JobLists.brewerItemBrewingMap.keySet().stream().filter(potion -> ItemStack.canCombine(potion, stack)).findFirst();
            int xpCount = 0;
            if (optional.isPresent()) {
                xpCount += stack.getCount() * JobLists.brewerItemBrewingMap.get(optional.get());
            } else {
                xpCount += stack.getCount() * ConfigInit.CONFIG.brewerXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "brewer", xpCount);
            }
        }
    }

    public static void addBrewerEnchantmentXp(PlayerEntity player, ItemStack stack, ItemStack oldItemStack) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("brewer")) {

            NbtList list = stack.getEnchantments();
            list.removeAll(oldItemStack.getEnchantments());

            Map<Enchantment, Integer> enchantmentMap = EnchantmentHelper.fromNbt(list);
            Iterator<Enchantment> iterator = enchantmentMap.keySet().iterator();

            int xpCount = 0;
            while (iterator.hasNext()) {
                Enchantment enchantment = iterator.next();
                if (JobLists.brewerEnchantmentMap.get(enchantment) != null) {
                    xpCount += JobLists.brewerEnchantmentMap.get(enchantment) * enchantmentMap.get(enchantment);
                } else {
                    xpCount += ConfigInit.CONFIG.brewerXP * enchantmentMap.get(enchantment);
                }
            }

            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "brewer", xpCount);
            }
        }
    }

    public static void addWarriorXp(PlayerEntity player, LivingEntity entity) {
        if (!player.world.isClient && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("warrior")) {
            int xpCount = 0;
            if (JobLists.warriorEntityIdMap.containsKey(Registry.ENTITY_TYPE.getRawId(entity.getType()))) {
                xpCount = JobLists.warriorEntityIdMap.get(Registry.ENTITY_TYPE.getRawId(entity.getType()));
            } else {
                xpCount = ConfigInit.CONFIG.warriorXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "warrior", xpCount);
            }
        }
    }
}
