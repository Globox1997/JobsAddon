package net.jobsaddon.init;

import net.jobsaddon.criteria.JobUpCriterion;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class CriteriaInit {

    public static final ObjectiveCriteria JOBS = ObjectiveCriteria.registerCustom("jobsaddon");
    public static final JobUpCriterion JOB_UP = CriteriaTriggers.register("jobsaddon:job_up", new JobUpCriterion());

    public static void init() {
    }
}
