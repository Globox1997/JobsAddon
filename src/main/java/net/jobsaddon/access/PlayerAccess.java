package net.jobsaddon.access;

import net.minecraft.util.math.BlockPos;

public interface PlayerAccess {

    public boolean setLastBlockId(BlockPos blockPos, boolean building, int id);

    public int getLastBlockId();

}
