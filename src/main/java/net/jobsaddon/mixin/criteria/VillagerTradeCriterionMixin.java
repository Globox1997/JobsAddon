package net.jobsaddon.mixin.criteria;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TradeTrigger.class)
public class VillagerTradeCriterionMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    private void triggerMixin(ServerPlayer player, AbstractVillager merchant, ItemStack stack, CallbackInfo info) {
        JobHelper.tradeJobXp(player, stack);
    }
}
