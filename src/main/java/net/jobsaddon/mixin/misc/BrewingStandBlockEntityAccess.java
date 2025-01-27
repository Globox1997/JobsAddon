package net.jobsaddon.mixin.misc;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BrewingStandBlockEntity.class)
public interface BrewingStandBlockEntityAccess {

    @Accessor("inventory")
    DefaultedList<ItemStack> getInventory();

}
