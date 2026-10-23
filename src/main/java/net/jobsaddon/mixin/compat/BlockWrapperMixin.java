package net.jobsaddon.mixin.compat;

import fr.rakambda.fallingtree.common.wrapper.*;
import fr.rakambda.fallingtree.fabric.common.wrapper.BlockWrapper;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockWrapper.class)
public class BlockWrapperMixin {

    @Inject(method = "playerDestroy", at = @At("TAIL"), remap = false)
    private void playerDestroyMixin(ILevel level, IPlayer player, IBlockPos blockPos, IBlockState blockState, IBlockEntity blockEntity, IItemStack itemStack, boolean dropResources, CallbackInfo info) {
        World world = (World) level.getRaw();
        if (!world.isClient()) {
            JobHelper.blockBreakJobXp((PlayerEntity) player.getRaw(), world, (BlockPos) blockPos.getRaw(), (BlockState) blockState.getRaw());
        }
    }
}
