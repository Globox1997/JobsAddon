package net.jobsaddon.mixin.compat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.raksrinana.fallingtree.common.wrapper.IBlockEntity;
import fr.raksrinana.fallingtree.common.wrapper.IBlockPos;
import fr.raksrinana.fallingtree.common.wrapper.IBlockState;
import fr.raksrinana.fallingtree.common.wrapper.IItemStack;
import fr.raksrinana.fallingtree.common.wrapper.ILevel;
import fr.raksrinana.fallingtree.common.wrapper.IPlayer;
import fr.raksrinana.fallingtree.fabric.common.wrapper.BlockWrapper;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.registry.Registry;

@Mixin(BlockWrapper.class)
public class BlockWrapperMixin {

    @Inject(method = "playerDestroy", at = @At("TAIL"), remap = false)
    private void playerDestroyMixin(@NotNull ILevel level, @NotNull IPlayer player, @NotNull IBlockPos blockPos, @NotNull IBlockState blockState, @Nullable IBlockEntity blockEntity,
            @NotNull IItemStack itemStack, CallbackInfo info) {
        BlockState state = (BlockState) blockState.getRaw();
        if (state.isIn(BlockTags.LOGS) && ((JobsManagerAccess) player.getRaw()).getJobsManager().isEmployedJob("lumberjack")) {
            int xpCount = 0;
            if (JobLists.lumberjackBlockIdMap.containsKey(Registry.BLOCK.getRawId(state.getBlock())))
                xpCount = JobLists.lumberjackBlockIdMap.get(Registry.BLOCK.getRawId(state.getBlock()));
            else
                xpCount = ConfigInit.CONFIG.lumberjackXP;
            JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player.getRaw(), "lumberjack", xpCount);
        }
    }
}
