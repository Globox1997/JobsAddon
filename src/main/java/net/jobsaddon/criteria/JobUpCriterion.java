package net.jobsaddon.criteria;

import com.google.gson.JsonObject;

import net.levelz.criteria.NumberPredicate;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class JobUpCriterion extends AbstractCriterion<JobUpCriterion.Conditions> {
    static final Identifier ID = new Identifier("jobsaddon:job_up");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended extended, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
        JobPredicate jobPredicate = JobPredicate.fromJson(jsonObject.get("job_name"));
        NumberPredicate jobLevelPredicate = NumberPredicate.fromJson(jsonObject.get("job_level"));
        return new Conditions(extended, jobPredicate, jobLevelPredicate);
    }

    public void trigger(ServerPlayerEntity player, String jobName, int jobLevel) {
        this.trigger(player, conditions -> conditions.matches(player, jobName, jobLevel));
    }

    class Conditions extends AbstractCriterionConditions {
        private final JobPredicate jobPredicate;
        private final NumberPredicate jobLevelPredicate;

        public Conditions(EntityPredicate.Extended player, JobPredicate jobPredicate, NumberPredicate jobLevelPredicate) {
            super(ID, player);
            this.jobPredicate = jobPredicate;
            this.jobLevelPredicate = jobLevelPredicate;
        }

        public boolean matches(ServerPlayerEntity player, String jobName, int jobLevel) {
            System.out.println(this.jobPredicate.test(jobName) + " : " + jobLevelPredicate.test(jobLevel) + " : " + jobName + " : " + jobLevel);
            return this.jobPredicate.test(jobName) && jobLevelPredicate.test(jobLevel);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("job_name", this.jobPredicate.toJson());
            jsonObject.add("job_level", this.jobLevelPredicate.toJson());
            return jsonObject;
        }
    }

}
