package net.jobsaddon.mixin.criteria;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.advancement.criterion.VillagerTradeCriterion;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerTradeCriterion.class)
public class VillagerTradeCriterionMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    private void triggerMixin(ServerPlayerEntity player, MerchantEntity merchant, ItemStack stack, CallbackInfo info) {
        JobHelper.tradeJobXp(player, stack);
    }
}
