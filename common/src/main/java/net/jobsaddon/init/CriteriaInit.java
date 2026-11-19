package net.jobsaddon.init;

import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.criteria.JobUpCriterion;
import net.jobsaddon.mixin.misc.ObjectiveCriteriaInvoker;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.function.BiConsumer;

public class CriteriaInit {

    public static final ObjectiveCriteria JOBS = ObjectiveCriteriaInvoker.invokeRegisterCustom("jobsaddon", false, ObjectiveCriteria.RenderType.INTEGER);
    public static JobUpCriterion JOB_UP;

    public static void register(BiConsumer<Identifier, CriterionTrigger<?>> registrar) {
        JOB_UP = new JobUpCriterion();
        registrar.accept(JobsAddonMain.identifierOf("job_up"), JOB_UP);
    }
}
