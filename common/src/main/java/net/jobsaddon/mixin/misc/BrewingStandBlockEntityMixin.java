package net.jobsaddon.mixin.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.jobsaddon.access.BrewingStandAccess;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends BaseContainerBlockEntity implements BrewingStandAccess {

    @Unique
    private static final double MAX_BREWER_DISTANCE = 32.0D;

    @Nullable
    @Unique
    private UUID brewer = null;

    public BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;doBrew(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;)V"))
    private static void tickMixin(ServerLevel world, BlockPos pos, BrewingStandBlockEntity blockEntity, Operation<Void> original) {
        ItemStack[] oldPotions = new ItemStack[3];
        for (int i = 0; i < oldPotions.length; i++) {
            oldPotions[i] = blockEntity.getItem(i).copy();
        }
        original.call(world, pos, blockEntity);

        UUID brewer = ((BrewingStandAccess) blockEntity).getBrewer();
        if (brewer != null && world.getPlayerByUUID(brewer) instanceof ServerPlayer serverPlayerEntity
                && serverPlayerEntity.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_BREWER_DISTANCE * MAX_BREWER_DISTANCE) {
            for (int i = 0; i < oldPotions.length; i++) {
                ItemStack potion = blockEntity.getItem(i);
                if (!potion.isEmpty() && !ItemStack.matches(oldPotions[i], potion)) {
                    JobHelper.brewJobXp(serverPlayerEntity, potion);
                }
            }
        }
    }

    @Inject(method = "createMenu", at = @At("HEAD"))
    protected void createScreenHandlerMixin(int syncId, Inventory playerInventory, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        this.brewer = playerInventory.player.getUUID();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    protected void readNbtMixin(ValueInput nbt, CallbackInfo info) {
        nbt.read("Brewer", UUIDUtil.CODEC).ifPresent(uuid -> this.brewer = uuid);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    protected void writeNbtMixin(ValueOutput nbt, CallbackInfo info) {
        nbt.storeNullable("Brewer", UUIDUtil.CODEC, this.brewer);
    }

    @Override
    public @Nullable UUID getBrewer() {
        return this.brewer;
    }

    @Override
    public void setBrewer(@Nullable UUID brewer) {
        this.brewer = brewer;
    }
}
