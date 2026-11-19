package net.jobsaddon.api;

import net.jobsaddon.jobs.Job;
import net.minecraft.server.level.ServerPlayer;

public final class JobEmploymentEvents {

    public static final Event<AllowEmploy> ALLOW_EMPLOY = Event.createArrayBacked(AllowEmploy.class, listeners -> (player, job) -> {
        for (AllowEmploy listener : listeners) {
            if (!listener.allowEmploy(player, job)) {
                return false;
            }
        }
        return true;
    });

    public static final Event<EmploymentChanged> EMPLOYMENT_CHANGED = Event.createArrayBacked(EmploymentChanged.class, listeners -> (player, job, employed) -> {
        for (EmploymentChanged listener : listeners) {
            listener.onEmploymentChanged(player, job, employed);
        }
    });

    private JobEmploymentEvents() {
    }

    @FunctionalInterface
    public interface AllowEmploy {
        boolean allowEmploy(ServerPlayer player, Job job);
    }

    @FunctionalInterface
    public interface EmploymentChanged {
        void onEmploymentChanged(ServerPlayer player, Job job, boolean employed);
    }
}
