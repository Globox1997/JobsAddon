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
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class JobHelper {

    public static void addSmitherXp(PlayerEntity player, ItemStack stack) {
        if (!player.getWorld().isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("smither")) {
            int xpCount = 0;
            if (JobLists.smitherItemIdMap.containsKey(Registries.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.smitherItemIdMap.get(Registries.ITEM.getRawId(stack.getItem()));
            } else if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof ArmorItem || stack.getItem() instanceof ToolItem || stack.getItem() instanceof BowItem
                    || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof HorseArmorItem) {
                xpCount = stack.getCount() * ConfigInit.CONFIG.smitherXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "smither", xpCount);
            }
        }
    }

    public static void addFisherXp(PlayerEntity player, ItemStack stack) {
        if (!player.getWorld().isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher")) {
            int xpCount = 0;
            if (JobLists.farmerItemIdMap.containsKey(Registries.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.farmerItemIdMap.get(Registries.ITEM.getRawId(stack.getItem()));
            } else {
                xpCount = stack.getCount() * ConfigInit.CONFIG.fisherXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "fisher", xpCount);
            }
        }
    }

    public static void addFisherCraftingXp(PlayerEntity player, ItemStack stack) {
        if (!player.getWorld().isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher") && stack.isIn(TagInit.FISHER_CRAFTING_ITEMS)) {
            int xpCount = 0;
            if (JobLists.fisherCraftingIdMap.containsKey(Registries.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.fisherCraftingIdMap.get(Registries.ITEM.getRawId(stack.getItem()));
            } else {
                xpCount = stack.getCount() * ConfigInit.CONFIG.fisherXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "fisher", xpCount);
            }
        }
    }

    public static void addFisherEntityXp(PlayerEntity player, LivingEntity entity) {
        if (!player.getWorld().isClient() && entity instanceof WaterCreatureEntity) {
            if (((JobsManagerAccess) player).getJobsManager().isEmployedJob("fisher")) {
                int xpCount = 0;
                if (JobLists.fisherEntityIdMap.containsKey(Registries.ENTITY_TYPE.getRawId(entity.getType()))) {
                    xpCount = JobLists.fisherEntityIdMap.get(Registries.ENTITY_TYPE.getRawId(entity.getType()));
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
        if (!player.getWorld().isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer") && (stack.isFood() || stack.isIn(TagInit.FARMER_CRAFTING_ITEMS))) {
            int xpCount = 0;
            if (JobLists.farmerCraftingIdMap.containsKey(Registries.ITEM.getRawId(stack.getItem()))) {
                xpCount = stack.getCount() * JobLists.farmerCraftingIdMap.get(Registries.ITEM.getRawId(stack.getItem()));
            } else {
                xpCount = stack.getCount() * ConfigInit.CONFIG.farmerXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "farmer", xpCount);
            }
        }
    }

    public static void addFarmerBlockDropXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (!player.getWorld().isClient() && state.getBlock() instanceof PlantBlock && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("farmer")
                && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
            int xpCount = 0;
            List<ItemStack> list = Block.getDroppedStacks(state, (ServerWorld) player.getWorld(), pos, null);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isIn(TagInit.FARMER_BREAKING_ITEMS)) {
                    if (JobLists.farmerItemIdMap.containsKey(Registries.ITEM.getRawId(list.get(i).getItem()))) {
                        xpCount += list.get(i).getCount() * JobLists.farmerItemIdMap.get(Registries.ITEM.getRawId(list.get(i).getItem()));
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
        if (!player.getWorld().isClient() && state.isIn(BlockTags.LOGS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("lumberjack")
                && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
            int xpCount = 0;
            if (JobLists.lumberjackBlockIdMap.containsKey(Registries.BLOCK.getRawId(state.getBlock()))) {
                xpCount = JobLists.lumberjackBlockIdMap.get(Registries.BLOCK.getRawId(state.getBlock()));
            } else {
                xpCount = ConfigInit.CONFIG.lumberjackXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "lumberjack", xpCount);
            }
        }
    }

    public static void addMinerXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (!player.getWorld().isClient() && state.isIn(TagInit.MINER_BLOCKS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("miner")
                && ((PlayerAccess) player).setLastBlockId(pos, false, 0)) {
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, player.getMainHandStack()) == 0) {
                int xpCount = 0;
                if (JobLists.minerBlockIdMap.containsKey(Registries.BLOCK.getRawId(state.getBlock()))) {
                    xpCount = JobLists.minerBlockIdMap.get(Registries.BLOCK.getRawId(state.getBlock()));
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
        if (player != null && !player.getWorld().isClient() && state.isIn(TagInit.BUILDER_PLACING_BLOCKS) && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("builder")) {
            if (((PlayerAccess) player).setLastBlockId(pos, true, Registries.BLOCK.getRawId(state.getBlock()))) {
                int xpCount = 0;
                if (JobLists.builderBlockIdMap.containsKey(Registries.BLOCK.getRawId(state.getBlock()))) {
                    xpCount = JobLists.builderBlockIdMap.get(Registries.BLOCK.getRawId(state.getBlock()));
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
        if (!player.getWorld().isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("brewer")) {
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
        if (!player.getWorld().isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("brewer")) {

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
        if (!player.getWorld().isClient() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("warrior")) {
            int xpCount = 0;
            if (JobLists.warriorEntityIdMap.containsKey(Registries.ENTITY_TYPE.getRawId(entity.getType()))) {
                xpCount = JobLists.warriorEntityIdMap.get(Registries.ENTITY_TYPE.getRawId(entity.getType()));
            } else {
                xpCount = ConfigInit.CONFIG.warriorXP;
            }
            if (xpCount > 0) {
                JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "warrior", xpCount);
            }
        }
    }
}
