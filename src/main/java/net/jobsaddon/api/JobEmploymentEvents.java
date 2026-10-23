package net.jobsaddon.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.jobsaddon.jobs.Job;
import net.minecraft.server.network.ServerPlayerEntity;

public final class JobEmploymentEvents {

    public static final Event<AllowEmploy> ALLOW_EMPLOY = EventFactory.createArrayBacked(AllowEmploy.class, listeners -> (player, job) -> {
        for (AllowEmploy listener : listeners) {
            if (!listener.allowEmploy(player, job)) {
                return false;
            }
        }
        return true;
    });

    public static final Event<EmploymentChanged> EMPLOYMENT_CHANGED = EventFactory.createArrayBacked(EmploymentChanged.class, listeners -> (player, job, employed) -> {
        for (EmploymentChanged listener : listeners) {
            listener.onEmploymentChanged(player, job, employed);
        }
    });

    private JobEmploymentEvents() {
    }

    @FunctionalInterface
    public interface AllowEmploy {
        boolean allowEmploy(ServerPlayerEntity player, Job job);
    }

    @FunctionalInterface
    public interface EmploymentChanged {
        void onEmploymentChanged(ServerPlayerEntity player, Job job, boolean employed);
    }
}
