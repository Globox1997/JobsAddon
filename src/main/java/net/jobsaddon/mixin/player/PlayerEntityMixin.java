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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class PlayerEntityMixin implements JobsManagerAccess, PlayerAccess {

    @Unique
    private final JobsManager jobsManager = new JobsManager((Player) (Object) this);
    @Unique
    private boolean quickCraftedRecipe;

    @Inject(method = "readAdditionalSaveData", at = @At(value = "TAIL"))
    private void readCustomDataFromNbtMixin(CompoundTag tag, CallbackInfo info) {
        this.jobsManager.readNbt(tag);
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "TAIL"))
    private void writeCustomDataToNbtMixin(CompoundTag tag, CallbackInfo info) {
        this.jobsManager.writeNbt(tag);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (jobsManager.getEmployedJobTime() > 0) {
            jobsManager.setEmployedJobTime(jobsManager.getEmployedJobTime() - 1);
        }
    }

    @Inject(method = "killedEntity", at = @At("HEAD"))
    private void onKilledOtherMixin(ServerLevel world, LivingEntity other, CallbackInfoReturnable<Boolean> info) {
        JobHelper.killEntityJobXp((Player) (Object) this, other);
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
