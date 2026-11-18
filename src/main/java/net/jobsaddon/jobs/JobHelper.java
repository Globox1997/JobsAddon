package net.jobsaddon.jobs;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.api.JobXpCallback;
import net.jobsaddon.mixin.misc.AbstractFurnaceBlockEntityAccess;
import net.jobsaddon.network.packet.JobXpPacket;
import net.levelz.registry.EnchantmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobHelper {

    private static void addJobXp(ServerPlayer serverPlayerEntity, int jobId, int experience) {
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

    public static void grantJobXp(Player player, JobExperienceType type, int rawId, int count) {
        if (count <= 0 || !(player instanceof ServerPlayer serverPlayerEntity)) {
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
                multiplier = jobsManager.getDiminishingReturns().getMultiplier(type, rawId, serverPlayerEntity.level().getServer().getTickCount());
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

    private static int roundRandomly(float experience, RandomSource random) {
        int xpCount = (int) experience;
        if (random.nextFloat() < experience - xpCount) {
            xpCount++;
        }
        return xpCount;
    }

    public static void craftItemJobXp(Player player, ItemStack stack) {
        craftItemJobXp(player, stack, stack.getCount(), getCraftingType(player));
    }

    public static void craftItemJobXp(Player player, ItemStack stack, int count, JobExperienceType type) {
        if (!stack.isEmpty()) {
            grantJobXp(player, type, BuiltInRegistries.ITEM.getId(stack.getItem()), count);
        }
    }

    public static JobExperienceType getCraftingType(Player player) {
        if (player.containerMenu instanceof SmithingMenu) {
            return JobExperienceType.SMITHING;
        } else if (player.containerMenu instanceof StonecutterMenu) {
            return JobExperienceType.STONECUTTING;
        }
        return JobExperienceType.CRAFTING;
    }

    public static boolean isRestrictedRecipe(Player player, ItemStack stack) {
        if (JobsManager.RESTRICTED_RECIPES.isEmpty()) {
            return false;
        }
        Level world = player.level();
        for (Slot slot : player.containerMenu.slots) {
            if (slot.container instanceof ResultContainer craftingResultInventory) {
                RecipeHolder<?> recipeEntry = craftingResultInventory.getRecipeUsed();
                return recipeEntry != null && JobsManager.RESTRICTED_RECIPES.contains(recipeEntry.id().identifier());
            }
            if (slot.container instanceof AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
                if (world.getServer() == null) {
                    return false;
                }
                for (ResourceKey<Recipe<?>> recipeId : ((AbstractFurnaceBlockEntityAccess) abstractFurnaceBlockEntity).getRecipesUsed().keySet()) {
                    if (JobsManager.RESTRICTED_RECIPES.contains(recipeId.identifier()) && world.getServer().getRecipeManager().byKey(recipeId)
                            .map(recipeEntry -> recipeEntry.value() instanceof SingleItemRecipe singleItemRecipe && singleItemRecipe.assemble(new SingleRecipeInput(ItemStack.EMPTY)).is(stack.getItem())).orElse(false)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static void killEntityJobXp(Player player, Entity entity) {
        grantJobXp(player, JobExperienceType.ENTITY_KILL, BuiltInRegistries.ENTITY_TYPE.getId(entity.getType()), 1);
    }

    public static void breedEntityJobXp(Player player, Entity entity) {
        grantJobXp(player, JobExperienceType.BREEDING, BuiltInRegistries.ENTITY_TYPE.getId(entity.getType()), 1);
    }

    public static void tameEntityJobXp(Player player, Entity entity) {
        grantJobXp(player, JobExperienceType.TAMING, BuiltInRegistries.ENTITY_TYPE.getId(entity.getType()), 1);
    }

    // One trade grants the xp once, the item is the bought item
    public static void tradeJobXp(Player player, ItemStack soldStack) {
        if (!soldStack.isEmpty()) {
            grantJobXp(player, JobExperienceType.TRADING, BuiltInRegistries.ITEM.getId(soldStack.getItem()), 1);
        }
    }

    // Right click harvesting like sweet berries, falls back to blockbreak
    public static void harvestJobXp(Player player, BlockState state) {
        if (!state.isAir()) {
            grantJobXp(player, JobExperienceType.HARVEST, BuiltInRegistries.BLOCK.getId(state.getBlock()), 1);
        }
    }

    // used for mining, wood cutting and farmer block drop
    public static void blockBreakJobXp(Player player, BlockState state) {
        if (!state.isAir()) {
            grantJobXp(player, JobExperienceType.BLOCK_BREAK, BuiltInRegistries.BLOCK.getId(state.getBlock()), 1);
        }
    }

    public static void blockBreakJobXp(Player player, Level world, BlockPos pos, BlockState state) {
        if (!PlacedBlockTracker.isPlayerPlaced(world, pos, state)) {
            blockBreakJobXp(player, state);
        }
    }

    public static void blockPlaceJobXp(Player player, BlockPos pos, BlockState state) {
        if (!state.isAir()) {
            grantJobXp(player, JobExperienceType.BLOCK_PLACE, BuiltInRegistries.BLOCK.getId(state.getBlock()), 1);
        }
    }

    public static void itemDropJobXp(Player player, List<ItemStack> droppedStacks) {
        for (ItemStack stack : droppedStacks) {
            if (!stack.isEmpty()) {
                grantJobXp(player, JobExperienceType.ITEM_DROP, BuiltInRegistries.ITEM.getId(stack.getItem()), stack.getCount());
            }
        }
    }

    public static void brewJobXp(Player player, ItemStack stack) {
        PotionContents potionContentsComponent = stack.get(DataComponents.POTION_CONTENTS);
        if (!stack.isEmpty() && potionContentsComponent != null && potionContentsComponent.potion().isPresent()) {
            grantJobXp(player, JobExperienceType.BREWING, BuiltInRegistries.POTION.getId(potionContentsComponent.potion().get().value()), stack.getCount());
        }
    }

    // Grants xp for every new or upgraded enchantment
    public static void enchantingJobXp(Player player, ItemStack stack, ItemStack oldItemStack) {
        if (player.level().isClientSide() || stack.isEmpty() || oldItemStack.isEmpty()) {
            return;
        }
        ItemEnchantments oldEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(oldItemStack);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            if (oldEnchantments.getLevel(entry.getKey()) < entry.getIntValue()) {
                grantJobXp(player, JobExperienceType.ENCHANTING, EnchantmentRegistry.getId(entry.getKey(), entry.getIntValue()), 1);
            }
        }
    }

    public static void multiBlockBreakJobXp(Player player, List<BlockPos> blockPoses) {
        if (!(player instanceof ServerPlayer serverPlayerEntity) || blockPoses.isEmpty()) {
            return;
        }
        JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();
        Level world = player.level();
        Map<Integer, Integer> jobXp = new HashMap<>();
        for (BlockPos pos : blockPoses) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || PlacedBlockTracker.isPlayerPlaced(world, pos, state)) {
                continue;
            }
            List<JobExperience> jobExperiences = JobExperienceType.BLOCK_BREAK.getJobExperiences(BuiltInRegistries.BLOCK.getId(state.getBlock()));
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

    public static void addCustomJobXp(ServerPlayer serverPlayerEntity, int jobId, int experience, boolean callEvent) {
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
