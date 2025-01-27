package net.jobsaddon.access;

import java.util.UUID;

public interface BrewingStandAccess {

    public UUID getBrewer();

    public void setBrewer(UUID brewer);
}
