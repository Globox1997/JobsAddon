package net.jobsaddon.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = FishingBobberEntity.class, priority = 999)
public class FishingBobberEntityMixin {

    @WrapOperation(method = "use(Lnet/minecraft/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean useMixin(World world, Entity entity, Operation<Boolean> original) {
        if (entity instanceof ItemEntity itemEntity && getPlayerOwner() != null && !itemEntity.getStack().isEmpty()) {
            JobHelper.itemDropJobXp(getPlayerOwner(), List.of(itemEntity.getStack()));
        }
        return original.call(world, entity);
    }

    @Nullable
    @Shadow
    public PlayerEntity getPlayerOwner() {
        return null;
    }
}
