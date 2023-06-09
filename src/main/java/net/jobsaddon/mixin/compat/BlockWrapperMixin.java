package net.jobsaddon.mixin.compat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.rakambda.fallingtree.common.wrapper.IBlockEntity;
import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.IBlockState;
import fr.rakambda.fallingtree.common.wrapper.IItemStack;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import fr.rakambda.fallingtree.fabric.common.wrapper.BlockWrapper;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockWrapper.class)
public class BlockWrapperMixin {

    @Inject(method = "playerDestroy", at = @At("TAIL"), remap = false)
    private void playerDestroyMixin(@NotNull ILevel level, @NotNull IPlayer player, @NotNull IBlockPos blockPos, @NotNull IBlockState blockState, @Nullable IBlockEntity blockEntity,
            @NotNull IItemStack itemStack, CallbackInfo info) {
        BlockState state = (BlockState) blockState.getRaw();
        PlayerEntity playerEntity = (PlayerEntity) player.getRaw();

        if (((PlayerAccess) playerEntity).setLastBlockId((BlockPos) blockPos.getRaw(), false, 0)) {
            JobHelper.addLumberjackXp(playerEntity, (BlockPos) blockPos.getRaw(), state);
        }
    }
}
