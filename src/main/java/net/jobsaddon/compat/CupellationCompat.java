package net.jobsaddon.compat;

import net.cupellation.api.CastingResultCallback;
import net.jobsaddon.jobs.JobExperienceType;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.core.registries.BuiltInRegistries;

public class CupellationCompat {

    public static void init() {
        CastingResultCallback.EVENT.register((player, stack, metalTypeId, castingEntity) -> {
            if (!stack.isEmpty()) {
                JobHelper.grantJobXp(player, JobExperienceType.CASTING, BuiltInRegistries.ITEM.getId(stack.getItem()), stack.getCount());
            }
        });
    }
}
