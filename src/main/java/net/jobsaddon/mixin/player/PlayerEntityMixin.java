package net.jobsaddon.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements JobsManagerAccess, PlayerAccess {

    @Unique
    private final JobsManager jobsManager = new JobsManager((PlayerEntity) (Object) this);
    @Unique
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
        JobHelper.killEntityJobXp((PlayerEntity) (Object) this, other);
    }

    @Override
    public JobsManager getJobsManager() {
        return this.jobsManager;
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
