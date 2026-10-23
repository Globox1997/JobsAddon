package net.jobsaddon.mixin.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.jobsaddon.access.BrewingStandAccess;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity implements BrewingStandAccess {

    @Unique
    private static final double MAX_BREWER_DISTANCE = 32.0D;

    @Nullable
    @Unique
    private UUID brewer = null;

    public BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;craft(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V"))
    private static void tickMixin(World world, BlockPos pos, DefaultedList<ItemStack> slots, Operation<Void> original, @Local(argsOnly = true) BrewingStandBlockEntity blockEntity) {
        ItemStack[] oldPotions = new ItemStack[3];
        for (int i = 0; i < oldPotions.length; i++) {
            oldPotions[i] = slots.get(i).copy();
        }
        original.call(world, pos, slots);

        UUID brewer = ((BrewingStandAccess) blockEntity).getBrewer();
        if (!world.isClient() && brewer != null && world.getPlayerByUuid(brewer) instanceof ServerPlayerEntity serverPlayerEntity
                && serverPlayerEntity.squaredDistanceTo(Vec3d.ofCenter(pos)) <= MAX_BREWER_DISTANCE * MAX_BREWER_DISTANCE) {
            for (int i = 0; i < oldPotions.length; i++) {
                if (!slots.get(i).isEmpty() && !ItemStack.areEqual(oldPotions[i], slots.get(i))) {
                    JobHelper.brewJobXp(serverPlayerEntity, slots.get(i));
                }
            }
        }
    }

    @Inject(method = "createScreenHandler", at = @At("HEAD"))
    protected void createScreenHandlerMixin(int syncId, PlayerInventory playerInventory, CallbackInfoReturnable<ScreenHandler> cir) {
        this.brewer = playerInventory.player.getUuid();
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    protected void readNbtMixin(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo info) {
        if (nbt.contains("Brewer")) {
            this.brewer = nbt.getUuid("Brewer");
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    protected void writeNbtMixin(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo info) {
        if (this.brewer != null) {
            nbt.putUuid("Brewer", this.brewer);
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
