package net.jobsaddon.mixin.entity;

import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    public MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public ItemEntity dropStack(ItemStack stack) {
        if (!stack.isEmpty() && this.isDead() && this.getLastAttacker() instanceof ServerPlayerEntity serverPlayerEntity) {
            JobHelper.itemDropJobXp(serverPlayerEntity, List.of(stack));
        }
        return super.dropStack(stack);
    }
}
