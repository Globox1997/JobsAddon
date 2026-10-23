package net.jobsaddon.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobExperienceType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface JobXpCallback {

    Event<JobXpCallback> EVENT = EventFactory.createArrayBacked(JobXpCallback.class, listeners -> (player, job, type, experience) -> {
        for (JobXpCallback listener : listeners) {
            experience = listener.modifyExperience(player, job, type, experience);
            if (experience <= 0) {
                return 0;
            }
        }
        return experience;
    });

    /**
     * @param job        the job of the player, read only
     * @param type       the xp source, null for custom experience
     * @param experience the experience which will get added
     * @return the new experience, 0 or less cancels it
     */
    int modifyExperience(ServerPlayerEntity player, Job job, @Nullable JobExperienceType type, int experience);
}
