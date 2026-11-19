package net.jobsaddon.api;

import net.jobsaddon.jobs.Job;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface JobLevelUpCallback {

    Event<JobLevelUpCallback> EVENT = Event.createArrayBacked(JobLevelUpCallback.class, listeners -> (player, job, level) -> {
        for (JobLevelUpCallback listener : listeners) {
            listener.onLevelUp(player, job, level);
        }
    });

    /**
     * @param job   the job of the player, read only
     * @param level the new level
     */
    void onLevelUp(ServerPlayer player, Job job, int level);
}
