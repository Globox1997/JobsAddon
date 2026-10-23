package net.jobsaddon.mixin.criteria;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.advancement.criterion.BredAnimalsCriterion;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BredAnimalsCriterion.class)
public class BredAnimalsCriterionMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    private void triggerMixin(ServerPlayerEntity player, AnimalEntity parent, AnimalEntity partner, @Nullable PassiveEntity child, CallbackInfo info) {
        JobHelper.breedEntityJobXp(player, parent);
    }
}
