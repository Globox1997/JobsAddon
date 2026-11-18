package net.jobsaddon.init;

import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.criteria.JobUpCriterion;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class CriteriaInit {

    public static final ObjectiveCriteria JOBS = ObjectiveCriteria.registerCustom("jobsaddon");
    public static final JobUpCriterion JOB_UP = Registry.register(BuiltInRegistries.TRIGGER_TYPES, JobsAddonMain.identifierOf("job_up"), new JobUpCriterion());

    public static void init() {
    }
}
