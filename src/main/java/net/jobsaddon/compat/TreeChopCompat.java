package net.jobsaddon.compat;

import ht.treechop.api.TreeChopEvents;
import net.jobsaddon.jobs.JobHelper;

public class TreeChopCompat {

    public static void init() {
        TreeChopEvents.BEFORE_FELL.register((world, player, pos, data) -> {
            if (!world.isClientSide() && player != null && data.getTree().getLogBlocks().isPresent()) {
                JobHelper.multiBlockBreakJobXp(player, data.getTree().getLogBlocks().get().stream().toList());
            }
            return true;
        });
    }
}
