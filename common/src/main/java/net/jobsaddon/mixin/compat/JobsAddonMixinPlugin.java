package net.jobsaddon.mixin.compat;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.jobsaddon.platform.Services;

public class JobsAddonMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("BlockWrapperMixin") && !Services.PLATFORM.isModLoaded("fallingtree"))
            return false;
        if (mixinClassName.contains("MobEntityMixin") && Services.PLATFORM.isModLoaded("rpgz"))
            return false;
        if (mixinClassName.contains("TideFishingHookMixin") && !Services.PLATFORM.isModLoaded("tide"))
            return false;
        if (mixinClassName.contains("FallingTreeEntityMixin") && !Services.PLATFORM.isModLoaded("dynamictrees"))
            return false;

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

}