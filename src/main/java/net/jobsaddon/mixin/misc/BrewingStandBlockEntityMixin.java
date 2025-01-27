package net.jobsaddon.mixin.misc;

import net.jobsaddon.access.BrewingStandAccess;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
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

    @Nullable
    @Unique
    private UUID brewer = null;

    public BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;craft(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V"))
    private static void tickMixin(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo info) {
        if (!world.isClient() && ((BrewingStandAccess) blockEntity).getBrewer() != null && world.getPlayerByUuid(((BrewingStandAccess) blockEntity).getBrewer()) instanceof ServerPlayerEntity serverPlayerEntity) {
            for (int i = 0; i < 3; i++) {
                if (!((BrewingStandBlockEntityAccess) blockEntity).getInventory().get(i).isEmpty()) {
                    JobHelper.brewJobXp(serverPlayerEntity, ((BrewingStandBlockEntityAccess) blockEntity).getInventory().get(i));
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
