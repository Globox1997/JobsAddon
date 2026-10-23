package net.jobsaddon.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

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

}
