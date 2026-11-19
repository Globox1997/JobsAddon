package net.jobsaddon.mixin.misc;

import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ObjectiveCriteria.class)
public interface ObjectiveCriteriaInvoker {

    @Invoker("registerCustom")
    static ObjectiveCriteria invokeRegisterCustom(String name, boolean readOnly, ObjectiveCriteria.RenderType renderType) {
        throw new AssertionError();
    }
}
