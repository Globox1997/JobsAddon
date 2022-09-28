package net.jobsaddon.init;

import net.fabricmc.fabric.mixin.object.builder.CriteriaAccessor;
import net.jobsaddon.criteria.JobUpCriterion;
import net.levelz.mixin.misc.ScoreboardCriterionAccessor;
import net.minecraft.scoreboard.ScoreboardCriterion;

public class CriteriaInit {

    public static final JobUpCriterion JOB_UP = CriteriaAccessor.callRegister(new JobUpCriterion());
    public static final ScoreboardCriterion JOBS = ScoreboardCriterionAccessor.callCreate("jobsaddon");

    public static void init() {
    }
}
