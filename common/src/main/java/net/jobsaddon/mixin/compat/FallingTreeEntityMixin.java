package net.jobsaddon.mixin.compat;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FallingTreeEntity.class)
public class FallingTreeEntityMixin {

    @Inject(method = "dropTree", at = @At("HEAD"), remap = false)
    private static void onDropTree(Level world, BranchDestructionData destroyData, List<ItemStack> woodDropList, FallingTreeEntity.DestroyType destroyType, CallbackInfoReturnable<FallingTreeEntity> info) {
        if (woodDropList == null || woodDropList.isEmpty()) {
            return;
        }
        if (world.isClientSide()) {
            return;
        }
        if (destroyType != FallingTreeEntity.DestroyType.HARVEST) return;

        BlockPos cutPos = destroyData.cutPos;
        Player player = world.getNearestPlayer(cutPos.getX(), cutPos.getY(), cutPos.getZ(), 8.0, false);
        if (player == null) {
            return;
        }

        JobHelper.itemDropJobXp(player, woodDropList);
    }
}
