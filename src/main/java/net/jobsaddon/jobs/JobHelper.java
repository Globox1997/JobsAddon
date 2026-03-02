package net.jobsaddon.jobs;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.network.packet.JobXpPacket;
import net.levelz.registry.EnchantmentRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class JobHelper {

    private static void addJobXp(ServerPlayerEntity serverPlayerEntity, int jobId, int experience) {
        JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
        // set on server
        jobsManager.addJobXP(serverPlayerEntity, jobId, experience);
        // send to client
        ServerPlayNetworking.send(serverPlayerEntity, new JobXpPacket(jobId, jobsManager.getJobXP(jobId)));
    }

    // also used for composter
    public static void craftItemJobXp(PlayerEntity player, ItemStack stack) {
        if (!player.getWorld().isClient() && !stack.isEmpty()) {
            if (JobsManager.ITEM_CRAFT_EXPERIENCE.containsKey(Registries.ITEM.getRawId(stack.getItem()))) {
                JobExperience jobExperience = JobsManager.ITEM_CRAFT_EXPERIENCE.get(Registries.ITEM.getRawId(stack.getItem()));

                if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                    int xpCount = stack.getCount() * jobExperience.getExperience();
                    if (xpCount > 0) {
                        addJobXp((ServerPlayerEntity) player, jobExperience.getId(), xpCount);
                    }
                }
            }
        }
    }

    public static void killEntityJobXp(PlayerEntity player, LivingEntity entity) {
        if (!player.getWorld().isClient()) {
            if (JobsManager.ENTITY_KILL_EXPERIENCE.containsKey(Registries.ENTITY_TYPE.getRawId(entity.getType()))) {
                JobExperience jobExperience = JobsManager.ENTITY_KILL_EXPERIENCE.get(Registries.ENTITY_TYPE.getRawId(entity.getType()));

                if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                    int xpCount = jobExperience.getExperience();
                    if (xpCount > 0) {
                        addJobXp((ServerPlayerEntity) player, jobExperience.getId(), xpCount);
                    }
                }
            }
        }
    }

    // used for mining, wood cutting and farmer block drop
    public static void blockBreakJobXp(PlayerEntity player, BlockState state) {
        if (!player.getWorld().isClient() && !state.isAir()) {
            if (JobsManager.BLOCK_BREAK_EXPERIENCE.containsKey(Registries.BLOCK.getRawId(state.getBlock()))) {
                JobExperience jobExperience = JobsManager.BLOCK_BREAK_EXPERIENCE.get(Registries.BLOCK.getRawId(state.getBlock()));

                if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                    int xpCount = jobExperience.getExperience();
                    if (xpCount > 0) {
                        addJobXp((ServerPlayerEntity) player, jobExperience.getId(), xpCount);
                    }
                }
            }
        }
    }

    public static void blockPlaceJobXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (!player.getWorld().isClient() && !state.isAir()) {
            if (JobsManager.BLOCK_PLACE_EXPERIENCE.containsKey(Registries.BLOCK.getRawId(state.getBlock()))) {
                JobExperience jobExperience = JobsManager.BLOCK_PLACE_EXPERIENCE.get(Registries.BLOCK.getRawId(state.getBlock()));

                if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                    int xpCount = jobExperience.getExperience();
                    if (xpCount > 0) {
                        addJobXp((ServerPlayerEntity) player, jobExperience.getId(), xpCount);
                    }
                }
            }
        }
    }

    public static void itemDropJobXp(PlayerEntity player, List<ItemStack> droppedStacks) {
        if (!player.getWorld().isClient() && !droppedStacks.isEmpty()) {
            for (ItemStack stack : droppedStacks) {
                if (JobsManager.ITEM_DROP_EXPERIENCE.containsKey(Registries.ITEM.getRawId(stack.getItem()))) {
                    JobExperience jobExperience = JobsManager.ITEM_DROP_EXPERIENCE.get(Registries.ITEM.getRawId(stack.getItem()));

                    if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                        int xpCount = jobExperience.getExperience() * stack.getCount();
                        if (xpCount > 0) {
                            addJobXp((ServerPlayerEntity) player, jobExperience.getId(), xpCount);
                        }
                    }
                }
            }
        }
    }

    public static void brewJobXp(PlayerEntity player, ItemStack stack) {
        if (!player.getWorld().isClient() && !stack.isEmpty() && stack.get(DataComponentTypes.POTION_CONTENTS) != null) {
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContentsComponent.potion().isPresent() && JobsManager.BREWING_EXPERIENCE.containsKey(Registries.POTION.getRawId(potionContentsComponent.potion().get().value()))) {
                JobExperience jobExperience = JobsManager.BREWING_EXPERIENCE.get(Registries.POTION.getRawId(potionContentsComponent.potion().get().value()));
                if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                    int xpCount = stack.getCount() * jobExperience.getExperience();
                    if (xpCount > 0) {
                        addJobXp((ServerPlayerEntity) player, jobExperience.getId(), xpCount);
                    }
                }
            }
        }
    }

    public static void enchantingJobXp(PlayerEntity player, ItemStack stack, ItemStack oldItemStack) {
        if (!player.getWorld().isClient() && !stack.isEmpty() && !oldItemStack.isEmpty()) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : stack.getEnchantments().getEnchantmentEntries()) {
                if (!oldItemStack.getEnchantments().getEnchantments().contains(entry.getKey())) {
                    int enchantmentId = EnchantmentRegistry.getId(entry.getKey(), entry.getIntValue());
                    if (JobsManager.ENCHANTMENT_EXPERIENCE.containsKey(enchantmentId)) {
                        JobExperience jobExperience = JobsManager.ENCHANTMENT_EXPERIENCE.get(enchantmentId);
                        if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                            int xpCount = jobExperience.getExperience();
                            if (xpCount > 0) {
                                addJobXp((ServerPlayerEntity) player, jobExperience.getId(), xpCount);
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom
    public static void multiBlockBreakJobXp(PlayerEntity player, List<BlockPos> blockPoses) {
        if (!player.getWorld().isClient() && !blockPoses.isEmpty()) {
            int xpCount = 0;
            int jobId = -1;
            for (BlockPos pos : blockPoses) {
                if (!player.getWorld().isAir(pos)) {
                    Block block = player.getWorld().getBlockState(pos).getBlock();
                    if (JobsManager.BLOCK_BREAK_EXPERIENCE.containsKey(Registries.BLOCK.getRawId(block))) {
                        JobExperience jobExperience = JobsManager.BLOCK_BREAK_EXPERIENCE.get(Registries.BLOCK.getRawId(block));
                        if (((JobsManagerAccess) player).getJobsManager().isEmployedJob(jobExperience.getId())) {
                            xpCount += jobExperience.getExperience();
                            jobId = jobExperience.getId();
                        }
                    }
                }
            }

            if (xpCount > 0 && jobId != -1) {
                addJobXp((ServerPlayerEntity) player, jobId, xpCount);
            }
        }
    }

}
