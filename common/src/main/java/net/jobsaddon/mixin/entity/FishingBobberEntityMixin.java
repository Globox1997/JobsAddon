package net.jobsaddon.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = FishingHook.class, priority = 999)
public class FishingBobberEntityMixin {

    @WrapOperation(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean useMixin(Level world, Entity entity, Operation<Boolean> original) {
        if (entity instanceof ItemEntity itemEntity && getPlayerOwner() != null && !itemEntity.getItem().isEmpty()) {
            JobHelper.itemDropJobXp(getPlayerOwner(), List.of(itemEntity.getItem()));
        }
        return original.call(world, entity);
    }

    @Nullable
    @Shadow
    public Player getPlayerOwner() {
        return null;
    }
}
