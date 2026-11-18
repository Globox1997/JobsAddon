package net.jobsaddon.mixin.compat;

import fr.rakambda.fallingtree.common.wrapper.*;
import fr.rakambda.fallingtree.fabric.common.wrapper.BlockWrapper;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockWrapper.class)
public class BlockWrapperMixin {

    @Inject(method = "playerDestroy", at = @At("TAIL"), remap = false)
    private void playerDestroyMixin(ILevel level, IPlayer player, IBlockPos blockPos, IBlockState blockState, IBlockEntity blockEntity, IItemStack itemStack, boolean dropResources, CallbackInfo info) {
        Level world = (Level) level.getRaw();
        if (!world.isClientSide()) {
            JobHelper.blockBreakJobXp((Player) player.getRaw(), world, (BlockPos) blockPos.getRaw(), (BlockState) blockState.getRaw());
        }
    }
}
