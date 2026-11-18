package net.jobsaddon.mixin.criteria;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BredAnimalsTrigger.class)
public class BredAnimalsCriterionMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    private void triggerMixin(ServerPlayer player, Animal parent, Animal partner, @Nullable AgeableMob child, CallbackInfo info) {
        JobHelper.breedEntityJobXp(player, parent);
    }
}
