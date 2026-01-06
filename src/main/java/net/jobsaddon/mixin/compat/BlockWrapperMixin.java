package net.jobsaddon.mixin.compat;

import fr.rakambda.fallingtree.common.wrapper.*;
import fr.rakambda.fallingtree.fabric.common.wrapper.BlockWrapper;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockWrapper.class)
public class BlockWrapperMixin {

    @Inject(method = "playerDestroy", at = @At("TAIL"), remap = false)
    private void playerDestroyMixin(ILevel level, IPlayer player, IBlockPos blockPos, IBlockState blockState, IBlockEntity blockEntity, IItemStack itemStack, boolean dropResources, CallbackInfo info) {
        BlockState state = (BlockState) blockState.getRaw();
        PlayerEntity playerEntity = (PlayerEntity) player.getRaw();

        if (((PlayerAccess) playerEntity).setLastBlockId((BlockPos) blockPos.getRaw(), false, 0)) {
            JobHelper.blockBreakJobXp(playerEntity, state);
        }
    }
}
