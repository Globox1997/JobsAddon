package net.jobsaddon.criteria;

import com.google.gson.JsonObject;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class JobUpCriterion extends AbstractCriterion<JobUpCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return JobUpCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, String jobName, int jobLevel) {
        this.trigger(player, conditions -> conditions.matches(player, jobName, jobLevel));
    }

    public record Conditions(Optional<LootContextPredicate> player, String jobName, int jobLevel) implements AbstractCriterion.Conditions {

        public static final Codec<JobUpCriterion.Conditions> CODEC = RecordCodecBuilder
                .create(instance -> instance
                        .group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(JobUpCriterion.Conditions::player),
                                Codec.STRING.fieldOf("job_name").forGetter(JobUpCriterion.Conditions::jobName), Codec.INT.fieldOf("job_level").forGetter(JobUpCriterion.Conditions::jobLevel))
                        .apply(instance, JobUpCriterion.Conditions::new));

        public boolean matches(ServerPlayerEntity player, String jobName, int jobLevel) {
            if (!jobName.equals(this.jobName)) {
                return false;
            }
            return jobLevel == this.jobLevel;
        }
    }

//
//    @Override
//    protected Conditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
//        JobPredicate jobPredicate = JobPredicate.fromJson(jsonObject.get("job_name"));
//        NumberPredicate jobLevelPredicate = NumberPredicate.fromJson(jsonObject.get("job_level"));
//        return new Conditions(lootContextPredicate, jobPredicate, jobLevelPredicate);
//    }
//
//
//
//    class Conditions extends AbstractCriterionConditions {
//        private final JobPredicate jobPredicate;
//        private final NumberPredicate jobLevelPredicate;
//
//        public Conditions(LootContextPredicate lootContextPredicate, JobPredicate jobPredicate, NumberPredicate jobLevelPredicate) {
//            super(ID, lootContextPredicate);
//            this.jobPredicate = jobPredicate;
//            this.jobLevelPredicate = jobLevelPredicate;
//        }
//
//        public boolean matches(ServerPlayerEntity player, String jobName, int jobLevel) {
//            return this.jobPredicate.test(jobName) && jobLevelPredicate.test(jobLevel);
//        }
//
//        @Override
//        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
//            JsonObject jsonObject = super.toJson(predicateSerializer);
//            jsonObject.add("job_name", this.jobPredicate.toJson());
//            jsonObject.add("job_level", this.jobLevelPredicate.toJson());
//            return jsonObject;
//        }
//    }

}
