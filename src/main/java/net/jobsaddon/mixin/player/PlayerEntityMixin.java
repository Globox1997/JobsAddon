package net.jobsaddon.mixin.player;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements JobsManagerAccess, PlayerAccess {

    private final JobsManager jobsManager = new JobsManager();
    private BlockPos lastBlockPos;
    private int lastBlockId;
    private int blockCount;
    @Nullable
    private Identifier lastRecipeIdentifier;
    private boolean quickCraftedRecipe;

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound tag, CallbackInfo info) {
        this.jobsManager.readNbt(tag);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound tag, CallbackInfo info) {
        this.jobsManager.writeNbt(tag);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (jobsManager.getEmployedJobTime() > 0) {
            jobsManager.setEmployedJobTime(jobsManager.getEmployedJobTime() - 1);
        }
    }

    @Inject(method = "onKilledOther", at = @At("HEAD"))
    private void onKilledOtherMixin(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> info) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.getWorld().isClient()) {
            JobHelper.addFisherEntityXp(player, other);
            JobHelper.addWarriorXp(player, other);
        }
    }

    @Override
    public JobsManager getJobsManager() {
        return this.jobsManager;
    }

    @Override
    public boolean setLastBlockId(BlockPos blockPos, boolean building, int id) {
        if (building) {
            if (this.lastBlockId == id) {
                blockCount++;
            } else {
                blockCount = 0;
            }
            this.lastBlockId = id;
            if (blockCount > 200) {
                JobsAddonMain.LOGGER.warn("Player " + ((PlayerEntity) (Object) this).getName().getString() + " placed over 200 blocks of type "
                        + Registries.BLOCK.get(id).asItem().getName().getString() + " at last pos " + ((PlayerEntity) (Object) this).getBlockPos());
                try (FileWriter playerLogFile = new FileWriter("playerlog.txt", true)) {
                    playerLogFile
                            .append("Player " + ((PlayerEntity) (Object) this).getName().getString() + " placed over 200 blocks of type " + Registries.BLOCK.get(id).asItem().getName().getString()
                                    + " at last pos " + ((PlayerEntity) (Object) this).getBlockPos() + " at " + new Timestamp(System.currentTimeMillis()));
                    playerLogFile.append(System.lineSeparator());
                } catch (IOException e) {
                }
                blockCount = 0;
            }
            return blockCount < 200;
        } else {
            if (this.lastBlockPos != null && this.lastBlockPos.equals(blockPos)) {
                blockCount++;
            } else {
                blockCount = 0;
            }
            this.lastBlockPos = blockPos;

            return blockCount < 10;
        }

    }

    @Override
    public int getLastBlockId() {
        return this.lastBlockId;
    }

    @Nullable
    @Override
    public Identifier getLastRecipeId() {
        return this.lastRecipeIdentifier;
    }

    @Override
    public void setLastRecipeId(Identifier identifier) {
        this.lastRecipeIdentifier = identifier;
    }

    @Override
    public boolean isQuickCrafted() {
        return this.quickCraftedRecipe;
    }

    @Override
    public void setQuickCraftedRecipe(boolean quickCrafted) {
        this.quickCraftedRecipe = quickCrafted;
    }
}
