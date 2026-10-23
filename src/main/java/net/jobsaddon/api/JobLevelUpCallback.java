package net.jobsaddon.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.jobsaddon.jobs.Job;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface JobLevelUpCallback {

    Event<JobLevelUpCallback> EVENT = EventFactory.createArrayBacked(JobLevelUpCallback.class, listeners -> (player, job, level) -> {
        for (JobLevelUpCallback listener : listeners) {
            listener.onLevelUp(player, job, level);
        }
    });

    /**
     * @param job   the job of the player, read only
     * @param level the new level
     */
    void onLevelUp(ServerPlayerEntity player, Job job, int level);
}
