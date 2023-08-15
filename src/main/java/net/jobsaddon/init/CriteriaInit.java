package net.jobsaddon.init;

import net.jobsaddon.criteria.JobUpCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.scoreboard.ScoreboardCriterion;

public class CriteriaInit {

    public static final JobUpCriterion JOB_UP = Criteria.register(new JobUpCriterion());
    public static final ScoreboardCriterion JOBS = ScoreboardCriterion.create("jobsaddon");

    public static void init() {
    }
}
