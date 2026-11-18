package net.jobsaddon.mixin.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.jobsaddon.access.BrewingStandAccess;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;doBrew(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;)V"))
    private static void tickMixin(Level world, BlockPos pos, NonNullList<ItemStack> slots, Operation<Void> original, @Local(argsOnly = true) BrewingStandBlockEntity blockEntity) {
        ItemStack[] oldPotions = new ItemStack[3];
        for (int i = 0; i < oldPotions.length; i++) {
            oldPotions[i] = slots.get(i).copy();
        }
        original.call(world, pos, slots);

        UUID brewer = ((BrewingStandAccess) blockEntity).getBrewer();
        if (!world.isClientSide() && brewer != null && world.getPlayerByUUID(brewer) instanceof ServerPlayer serverPlayerEntity
                && serverPlayerEntity.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_BREWER_DISTANCE * MAX_BREWER_DISTANCE) {
            for (int i = 0; i < oldPotions.length; i++) {
                if (!slots.get(i).isEmpty() && !ItemStack.matches(oldPotions[i], slots.get(i))) {
                    JobHelper.brewJobXp(serverPlayerEntity, slots.get(i));
                }
            }
        }
    }

    @Inject(method = "createMenu", at = @At("HEAD"))
    protected void createScreenHandlerMixin(int syncId, Inventory playerInventory, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        this.brewer = playerInventory.player.getUUID();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    protected void readNbtMixin(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo info) {
        if (nbt.contains("Brewer")) {
            this.brewer = nbt.getUUID("Brewer");
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    protected void writeNbtMixin(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo info) {
        if (this.brewer != null) {
            nbt.putUUID("Brewer", this.brewer);
        }
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
