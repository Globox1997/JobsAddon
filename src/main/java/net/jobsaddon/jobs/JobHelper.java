package net.jobsaddon.jobs;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.api.JobXpCallback;
import net.jobsaddon.mixin.misc.AbstractFurnaceBlockEntityAccess;
import net.jobsaddon.network.packet.JobXpPacket;
import net.levelz.registry.EnchantmentRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobHelper {

    private static void addJobXp(ServerPlayerEntity serverPlayerEntity, int jobId, int experience) {
        JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
        // set on server
        jobsManager.addJobXP(serverPlayerEntity, jobId, experience);
        // send to client
        ServerPlayNetworking.send(serverPlayerEntity, new JobXpPacket(jobId, jobsManager.getJobXP(jobId)));
    }

    @Nullable
    public static List<JobExperience> getJobExperiences(JobExperienceType type, int rawId) {
        List<JobExperience> jobExperiences = type.getJobExperiences(rawId);
        if (jobExperiences == null) {
            if (type == JobExperienceType.SMITHING || type == JobExperienceType.STONECUTTING) {
                return JobExperienceType.CRAFTING.getJobExperiences(rawId);
            } else if (type == JobExperienceType.HARVEST) {
                return JobExperienceType.BLOCK_BREAK.getJobExperiences(rawId);
            }
        }
        return jobExperiences;
    }

    public static void grantJobXp(PlayerEntity player, JobExperienceType type, int rawId, int count) {
        if (count <= 0 || !(player instanceof ServerPlayerEntity serverPlayerEntity)) {
            return;
        }
        List<JobExperience> jobExperiences = getJobExperiences(type, rawId);
        if (jobExperiences == null) {
            return;
        }
        JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();
        float multiplier = -1.0F;
        for (JobExperience jobExperience : jobExperiences) {
            Job job = jobsManager.getJob(jobExperience.getId());
            if (job == null || !jobsManager.isEmployedJob(jobExperience.getId())) {
                continue;
            }
            if (multiplier < 0.0F) {
                multiplier = jobsManager.getDiminishingReturns().getMultiplier(type, rawId, serverPlayerEntity.getServer().getTicks());
            }
            int xpCount = roundRandomly((float) jobExperience.getExperience() * count * multiplier, serverPlayerEntity.getRandom());
            if (xpCount > 0) {
                xpCount = JobXpCallback.EVENT.invoker().modifyExperience(serverPlayerEntity, job, type, xpCount);
            }
            if (xpCount > 0) {
                addJobXp(serverPlayerEntity, jobExperience.getId(), xpCount);
            }
        }
    }

    private static int roundRandomly(float experience, Random random) {
        int xpCount = (int) experience;
        if (random.nextFloat() < experience - xpCount) {
            xpCount++;
        }
        return xpCount;
    }

    public static void craftItemJobXp(PlayerEntity player, ItemStack stack) {
        craftItemJobXp(player, stack, stack.getCount(), getCraftingType(player));
    }

    public static void craftItemJobXp(PlayerEntity player, ItemStack stack, int count, JobExperienceType type) {
        if (!stack.isEmpty()) {
            grantJobXp(player, type, Registries.ITEM.getRawId(stack.getItem()), count);
        }
    }

    public static JobExperienceType getCraftingType(PlayerEntity player) {
        if (player.currentScreenHandler instanceof SmithingScreenHandler) {
            return JobExperienceType.SMITHING;
        } else if (player.currentScreenHandler instanceof StonecutterScreenHandler) {
            return JobExperienceType.STONECUTTING;
        }
        return JobExperienceType.CRAFTING;
    }

    public static boolean isRestrictedRecipe(PlayerEntity player, ItemStack stack) {
        if (JobsManager.RESTRICTED_RECIPES.isEmpty()) {
            return false;
        }
        World world = player.getWorld();
        for (Slot slot : player.currentScreenHandler.slots) {
            if (slot.inventory instanceof CraftingResultInventory craftingResultInventory) {
                RecipeEntry<?> recipeEntry = craftingResultInventory.getLastRecipe();
                return recipeEntry != null && JobsManager.RESTRICTED_RECIPES.contains(recipeEntry.id());
            }
            if (slot.inventory instanceof AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
                for (Identifier recipeId : ((AbstractFurnaceBlockEntityAccess) abstractFurnaceBlockEntity).getRecipesUsed().keySet()) {
                    if (JobsManager.RESTRICTED_RECIPES.contains(recipeId) && world.getRecipeManager().get(recipeId)
                            .map(recipeEntry -> recipeEntry.value().getResult(world.getRegistryManager()).isOf(stack.getItem())).orElse(false)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static void killEntityJobXp(PlayerEntity player, Entity entity) {
        grantJobXp(player, JobExperienceType.ENTITY_KILL, Registries.ENTITY_TYPE.getRawId(entity.getType()), 1);
    }

    public static void breedEntityJobXp(PlayerEntity player, Entity entity) {
        grantJobXp(player, JobExperienceType.BREEDING, Registries.ENTITY_TYPE.getRawId(entity.getType()), 1);
    }

    public static void tameEntityJobXp(PlayerEntity player, Entity entity) {
        grantJobXp(player, JobExperienceType.TAMING, Registries.ENTITY_TYPE.getRawId(entity.getType()), 1);
    }

    // One trade grants the xp once, the item is the bought item
    public static void tradeJobXp(PlayerEntity player, ItemStack soldStack) {
        if (!soldStack.isEmpty()) {
            grantJobXp(player, JobExperienceType.TRADING, Registries.ITEM.getRawId(soldStack.getItem()), 1);
        }
    }

    // Right click harvesting like sweet berries, falls back to blockbreak
    public static void harvestJobXp(PlayerEntity player, BlockState state) {
        if (!state.isAir()) {
            grantJobXp(player, JobExperienceType.HARVEST, Registries.BLOCK.getRawId(state.getBlock()), 1);
        }
    }

    // used for mining, wood cutting and farmer block drop
    public static void blockBreakJobXp(PlayerEntity player, BlockState state) {
        if (!state.isAir()) {
            grantJobXp(player, JobExperienceType.BLOCK_BREAK, Registries.BLOCK.getRawId(state.getBlock()), 1);
        }
    }

    public static void blockBreakJobXp(PlayerEntity player, World world, BlockPos pos, BlockState state) {
        if (!PlacedBlockTracker.isPlayerPlaced(world, pos, state)) {
            blockBreakJobXp(player, state);
        }
    }

    public static void blockPlaceJobXp(PlayerEntity player, BlockPos pos, BlockState state) {
        if (!state.isAir()) {
            grantJobXp(player, JobExperienceType.BLOCK_PLACE, Registries.BLOCK.getRawId(state.getBlock()), 1);
        }
    }

    public static void itemDropJobXp(PlayerEntity player, List<ItemStack> droppedStacks) {
        for (ItemStack stack : droppedStacks) {
            if (!stack.isEmpty()) {
                grantJobXp(player, JobExperienceType.ITEM_DROP, Registries.ITEM.getRawId(stack.getItem()), stack.getCount());
            }
        }
    }

    public static void brewJobXp(PlayerEntity player, ItemStack stack) {
        PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (!stack.isEmpty() && potionContentsComponent != null && potionContentsComponent.potion().isPresent()) {
            grantJobXp(player, JobExperienceType.BREWING, Registries.POTION.getRawId(potionContentsComponent.potion().get().value()), stack.getCount());
        }
    }

    // Grants xp for every new or upgraded enchantment
    public static void enchantingJobXp(PlayerEntity player, ItemStack stack, ItemStack oldItemStack) {
        if (player.getWorld().isClient() || stack.isEmpty() || oldItemStack.isEmpty()) {
            return;
        }
        ItemEnchantmentsComponent oldEnchantments = EnchantmentHelper.getEnchantments(oldItemStack);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(stack).getEnchantmentEntries()) {
            if (oldEnchantments.getLevel(entry.getKey()) < entry.getIntValue()) {
                grantJobXp(player, JobExperienceType.ENCHANTING, EnchantmentRegistry.getId(entry.getKey(), entry.getIntValue()), 1);
            }
        }
    }

    public static void multiBlockBreakJobXp(PlayerEntity player, List<BlockPos> blockPoses) {
        if (!(player instanceof ServerPlayerEntity serverPlayerEntity) || blockPoses.isEmpty()) {
            return;
        }
        JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();
        World world = player.getWorld();
        Map<Integer, Integer> jobXp = new HashMap<>();
        for (BlockPos pos : blockPoses) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || PlacedBlockTracker.isPlayerPlaced(world, pos, state)) {
                continue;
            }
            List<JobExperience> jobExperiences = JobExperienceType.BLOCK_BREAK.getJobExperiences(Registries.BLOCK.getRawId(state.getBlock()));
            if (jobExperiences != null) {
                for (JobExperience jobExperience : jobExperiences) {
                    if (jobsManager.isEmployedJob(jobExperience.getId())) {
                        jobXp.merge(jobExperience.getId(), jobExperience.getExperience(), Integer::sum);
                    }
                }
            }
        }
        jobXp.forEach((jobId, xpCount) -> {
            Job job = jobsManager.getJob(jobId);
            if (job != null && xpCount > 0) {
                xpCount = JobXpCallback.EVENT.invoker().modifyExperience(serverPlayerEntity, job, JobExperienceType.BLOCK_BREAK, xpCount);
            }
            if (job != null && xpCount > 0) {
                addJobXp(serverPlayerEntity, jobId, xpCount);
            }
        });
    }

    public static void addCustomJobXp(ServerPlayerEntity serverPlayerEntity, int jobId, int experience, boolean callEvent) {
        JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
        Job job = jobsManager.getJob(jobId);
        if (job == null || experience <= 0) {
            return;
        }
        if (callEvent) {
            experience = JobXpCallback.EVENT.invoker().modifyExperience(serverPlayerEntity, job, null, experience);
        }
        if (experience > 0) {
            addJobXp(serverPlayerEntity, jobId, experience);
        }
    }

}
