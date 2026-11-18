package net.jobsaddon.mixin.entity;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity {

    public MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ItemStack stack) {
        if (!stack.isEmpty() && this.isDeadOrDying() && this.getLastAttacker() instanceof ServerPlayer serverPlayerEntity) {
            JobHelper.itemDropJobXp(serverPlayerEntity, List.of(stack));
        }
        return super.spawnAtLocation(stack);
    }
}
