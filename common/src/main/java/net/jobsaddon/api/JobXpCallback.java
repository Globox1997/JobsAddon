package net.jobsaddon.api;

import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobExperienceType;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface JobXpCallback {

    Event<JobXpCallback> EVENT = Event.createArrayBacked(JobXpCallback.class, listeners -> (player, job, type, experience) -> {
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
    int modifyExperience(ServerPlayer player, Job job, @Nullable JobExperienceType type, int experience);
}
