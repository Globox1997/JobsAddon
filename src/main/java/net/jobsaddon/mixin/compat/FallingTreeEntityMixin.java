package net.jobsaddon.mixin.compat;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FallingTreeEntity.class)
public class FallingTreeEntityMixin {

    @Inject(method = "dropTree", at = @At("HEAD"), remap = false)
    private static void onDropTree(World world, BranchDestructionData destroyData, List<ItemStack> woodDropList, FallingTreeEntity.DestroyType destroyType, CallbackInfoReturnable<FallingTreeEntity> info) {
        if (woodDropList == null || woodDropList.isEmpty()) {
            return;
        }
        if (world.isClient()) {
            return;
        }
        if (destroyType != FallingTreeEntity.DestroyType.HARVEST) return;

        try {
            int x = (int) destroyData.cutPos.getClass().getMethod("getX").invoke(destroyData.cutPos);
            int y = (int) destroyData.cutPos.getClass().getMethod("getY").invoke(destroyData.cutPos);
            int z = (int) destroyData.cutPos.getClass().getMethod("getZ").invoke(destroyData.cutPos);

            PlayerEntity player = world.getClosestPlayer(x, y, z, 8.0, false);
            if (player == null) {
                return;
            }

            JobHelper.itemDropJobXp(player, woodDropList);
        } catch (Exception ignored) {
        }
    }
}
