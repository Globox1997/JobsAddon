package net.jobsaddon.jobs;

import java.util.ArrayList;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.CriteriaInit;
import net.jobsaddon.network.JobsServerPacket;
import net.levelz.access.PlayerSyncAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

public class JobsManager {

    // Jobs
    private int brewerLevel;
    private int builderLevel;
    private int farmerLevel;
    private int fisherLevel;
    private int lumberjackLevel;
    private int minerLevel;
    private int smitherLevel;
    private int warriorLevel;
    // XP
    private int brewerXP;
    private int builderXP;
    private int farmerXP;
    private int fisherXP;
    private int lumberjackXP;
    private int minerXP;
    private int smitherXP;
    private int warriorXP;
    // Employed Jobs
    private ArrayList<String> employedJobsList = new ArrayList<String>();
    private int employedJobTime;

    public void readNbt(NbtCompound tag) {
        if (tag.contains("BrewerLevel", 99)) {
            // Job Level
            this.brewerLevel = tag.getInt("BrewerLevel");
            this.builderLevel = tag.getInt("BuilderLevel");
            this.farmerLevel = tag.getInt("FarmerLevel");
            this.fisherLevel = tag.getInt("FisherLevel");
            this.lumberjackLevel = tag.getInt("LumberjackLevel");
            this.minerLevel = tag.getInt("MinerLevel");
            this.smitherLevel = tag.getInt("SmitherLevel");
            this.warriorLevel = tag.getInt("WarriorLevel");
            // Job XP
            this.brewerXP = tag.getInt("BrewerXP");
            this.builderXP = tag.getInt("BuilderXP");
            this.farmerXP = tag.getInt("FarmerXP");
            this.fisherXP = tag.getInt("FisherXP");
            this.lumberjackXP = tag.getInt("LumberjackXP");
            this.minerXP = tag.getInt("MinerXP");
            this.smitherXP = tag.getInt("SmitherXP");
            this.warriorXP = tag.getInt("WarriorXP");
            // Employed Jobs
            int employedJobCount = tag.getInt("EmployedJobCount");
            if (employedJobCount > 0) {
                employedJobsList.clear();
                for (int i = 0; i < employedJobCount; i++)
                    employedJobsList.add(tag.getString("EmployedJob" + i));
            }
            this.employedJobTime = tag.getInt("EmployedJobTime");
        }
    }

    public void writeNbt(NbtCompound tag) {
        // Job Level
        tag.putInt("BrewerLevel", this.brewerLevel);
        tag.putInt("BuilderLevel", this.builderLevel);
        tag.putInt("FarmerLevel", this.farmerLevel);
        tag.putInt("FisherLevel", this.fisherLevel);
        tag.putInt("LumberjackLevel", this.lumberjackLevel);
        tag.putInt("MinerLevel", this.minerLevel);
        tag.putInt("SmitherLevel", this.smitherLevel);
        tag.putInt("WarriorLevel", this.warriorLevel);
        // Job XP
        tag.putInt("BrewerXP", this.brewerXP);
        tag.putInt("BuilderXP", this.builderXP);
        tag.putInt("FarmerXP", this.farmerXP);
        tag.putInt("FisherXP", this.fisherXP);
        tag.putInt("LumberjackXP", this.lumberjackXP);
        tag.putInt("MinerXP", this.minerXP);
        tag.putInt("SmitherXP", this.smitherXP);
        tag.putInt("WarriorXP", this.warriorXP);
        // Employed Jobs
        if (!employedJobsList.isEmpty())
            for (int i = 0; i < employedJobsList.size(); i++)
                tag.putString("EmployedJob" + i, employedJobsList.get(i));
        tag.putInt("EmployedJobCount", employedJobsList.size());
        tag.putInt("EmployedJobTime", this.employedJobTime);
    }

    public void setJobLevel(String string, int level) {
        switch (string) {
        case "brewer":
            this.brewerLevel = level;
            break;
        case "builder":
            this.builderLevel = level;
            break;
        case "farmer":
            this.farmerLevel = level;
            break;
        case "fisher":
            this.fisherLevel = level;
            break;
        case "lumberjack":
            this.lumberjackLevel = level;
            break;
        case "miner":
            this.minerLevel = level;
            break;
        case "smither":
            this.smitherLevel = level;
            break;
        case "warrior":
            this.warriorLevel = level;
            break;
        default:
            break;
        }
    }

    public int getJobLevel(String string) {
        switch (string) {
        case "brewer":
            return this.brewerLevel;
        case "builder":
            return this.builderLevel;
        case "farmer":
            return this.farmerLevel;
        case "fisher":
            return this.fisherLevel;
        case "lumberjack":
            return this.lumberjackLevel;
        case "miner":
            return this.minerLevel;
        case "smither":
            return this.smitherLevel;
        case "warrior":
            return this.warriorLevel;
        default:
            return 0;
        }
    }

    public void setJobXP(String string, int amount) {
        switch (string) {
        case "brewer":
            this.brewerXP = amount;
            break;
        case "builder":
            this.builderXP = amount;
            break;
        case "farmer":
            this.farmerXP = amount;
            break;
        case "fisher":
            this.fisherXP = amount;
            break;
        case "lumberjack":
            this.lumberjackXP = amount;
            break;
        case "miner":
            this.minerXP = amount;
            break;
        case "smither":
            this.smitherXP = amount;
            break;
        case "warrior":
            this.warriorXP = amount;
            break;
        default:
            break;
        }
    }

    public int getJobXP(String string) {
        switch (string) {
        case "brewer":
            return this.brewerXP;
        case "builder":
            return this.builderXP;
        case "farmer":
            return this.farmerXP;
        case "fisher":
            return this.fisherXP;
        case "lumberjack":
            return this.lumberjackXP;
        case "miner":
            return this.minerXP;
        case "smither":
            return this.smitherXP;
        case "warrior":
            return this.warriorXP;
        default:
            return 0;
        }
    }

    public void addJobXP(PlayerEntity playerEntity, String string, int XP) {
        if (!isJobMaxLevel(string)) {
            int currentJobXp = getJobXP(string) + XP;

            while (getNextJobLevelExperience(string) < currentJobXp && !isJobMaxLevel(string)) {
                addJobExperienceLevels(playerEntity, string, 1);
                playerEntity.world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, playerEntity.getSoundCategory(), 1.0F, 1.0F);
                if (!playerEntity.world.isClient) {
                    playerEntity.getScoreboard().forEachScore(CriteriaInit.JOBS, playerEntity.getEntityName(), ScoreboardPlayerScore::incrementScore);
                    CriteriaInit.JOB_UP.trigger((ServerPlayerEntity) playerEntity, string, getJobLevel(string));
                    JobsServerPacket.writeS2CJobPacket(this, (ServerPlayerEntity) playerEntity);
                }
                currentJobXp -= getNextJobLevelExperience(string);
            }
            setJobXP(string, currentJobXp);
        }
    }

    public void addJobExperienceLevels(PlayerEntity playerEntity, String string, int levels) {
        int jobLevel = getJobLevel(string);

        setJobLevel(string, jobLevel + levels);
        if (jobLevel < 0)
            setJobLevel(string, 0);
        if (!playerEntity.world.isClient) {
            // Add numismatic money
            if (FabricLoader.getInstance().isModLoaded("numismatic-overhaul") && ConfigInit.CONFIG.moneyMultiplicator > 0) {
                CurrencyComponent playerBalance = ModComponents.CURRENCY.get(playerEntity);
                playerBalance.silentModify(playerBalance.getValue() + (getJobLevel(string) * ConfigInit.CONFIG.moneyMultiplicator));
            }
            // Add levelz xp
            if (ConfigInit.CONFIG.levelZXPMultiplicator > 0)
                ((PlayerSyncAccess) playerEntity).addLevelExperience(ConfigInit.CONFIG.levelZXPMultiplicator * getJobLevel(string));
        }

    }

    public void employJob(String jobName) {
        if (!isEmployedJob(jobName))
            employedJobsList.add(jobName);
    }

    public void quitJob(String jobName) {
        if (isEmployedJob(jobName))
            employedJobsList.remove(jobName);
    }

    public boolean isEmployedJob(String jobName) {
        return employedJobsList.contains(jobName);
    }

    public boolean canEmployJob(String string) {
        return !isEmployedJob(string) && employedJobsList.size() < ConfigInit.CONFIG.employedJobs && getEmployedJobTime() == 0;
    }

    public boolean hasMaxEmployedJobs() {
        return employedJobsList != null && employedJobsList.size() == ConfigInit.CONFIG.employedJobs;
    }

    @Nullable
    public ArrayList<String> getEmployedJobs() {
        return employedJobsList.isEmpty() ? null : employedJobsList;
    }

    public boolean isJobMaxLevel(String string) {
        return getJobLevel(string) >= ConfigInit.CONFIG.jobMaxLevel;
    }

    public int getEmployedJobTime() {
        return employedJobTime;
    }

    public void setEmployedJobTime(int time) {
        employedJobTime = time;
    }

    // Recommend to use https://www.geogebra.org/graphing
    public int getNextJobLevelExperience(String string) {
        if (isJobMaxLevel(string))
            return 0;
        int experienceCost = (int) (ConfigInit.CONFIG.jobXPBaseCost + ConfigInit.CONFIG.jobXPCostMultiplicator * Math.pow(getJobLevel(string), ConfigInit.CONFIG.jobXPExponent));
        if (ConfigInit.CONFIG.jobXPMaxCost != 0)
            return experienceCost >= ConfigInit.CONFIG.jobXPMaxCost ? ConfigInit.CONFIG.jobXPMaxCost : experienceCost;
        else
            return experienceCost;
    }

}
