package net.jobsaddon.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class JobUpCriterion extends SimpleCriterionTrigger<JobUpCriterion.Conditions> {

    @Override
    public Codec<Conditions> codec() {
        return JobUpCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, String jobName, int jobLevel) {
        this.trigger(player, conditions -> conditions.matches(player, jobName, jobLevel));
    }

    public record Conditions(Optional<Holder<LootItemCondition>> player, String jobName, int jobLevel) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<JobUpCriterion.Conditions> CODEC = RecordCodecBuilder
                .create(instance -> instance
                        .group(LootItemCondition.CODEC.optionalFieldOf("player").forGetter(JobUpCriterion.Conditions::player),
                                Codec.STRING.fieldOf("job_name").forGetter(JobUpCriterion.Conditions::jobName), Codec.INT.fieldOf("job_level").forGetter(JobUpCriterion.Conditions::jobLevel))
                        .apply(instance, JobUpCriterion.Conditions::new));

        public boolean matches(ServerPlayer player, String jobName, int jobLevel) {
            if (!jobName.equals(this.jobName)) {
                return false;
            }
            return jobLevel == this.jobLevel;
        }
    }

}
