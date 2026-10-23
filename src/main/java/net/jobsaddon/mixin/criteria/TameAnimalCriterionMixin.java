package net.jobsaddon.mixin.criteria;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.advancement.criterion.TameAnimalCriterion;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameAnimalCriterion.class)
public class TameAnimalCriterionMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    private void triggerMixin(ServerPlayerEntity player, AnimalEntity entity, CallbackInfo info) {
        JobHelper.tameEntityJobXp(player, entity);
    }
}
