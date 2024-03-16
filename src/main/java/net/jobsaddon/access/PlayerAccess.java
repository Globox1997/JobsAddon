package net.jobsaddon.access;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public interface PlayerAccess {

    public boolean setLastBlockId(BlockPos blockPos, boolean building, int id);

    public int getLastBlockId();

    public Identifier getLastRecipeId();

    public void setLastRecipeId(Identifier identifier);

    public boolean isQuickCrafted();

    public void setQuickCraftedRecipe(boolean quickCrafted);

}
