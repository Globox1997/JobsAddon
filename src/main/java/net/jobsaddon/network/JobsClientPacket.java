package net.jobsaddon.network;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

public class JobsClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(JobsServerPacket.JOB_PACKET, (client, handler, buf, sender) -> {
            List<Integer> list = new ArrayList<Integer>();

            for (int i = 0; i < 16; i++)
                list.add(buf.readInt());

            List<String> employedJobList = new ArrayList<String>();
            int jobCount = buf.readInt();
            if (jobCount > 0)
                for (int i = 0; i < jobCount; i++)
                    employedJobList.add(buf.readString());
            int employedJobTime = buf.readInt();

            client.execute(() -> {
                JobsManager jobsManager = ((JobsManagerAccess) client.player).getJobsManager();
                // Job Level
                jobsManager.setJobLevel("brewer", list.get(0));
                jobsManager.setJobLevel("builder", list.get(1));
                jobsManager.setJobLevel("farmer", list.get(2));
                jobsManager.setJobLevel("fisher", list.get(3));
                jobsManager.setJobLevel("lumberjack", list.get(4));
                jobsManager.setJobLevel("miner", list.get(5));
                jobsManager.setJobLevel("smither", list.get(6));
                jobsManager.setJobLevel("warrior", list.get(7));
                // Job XP
                jobsManager.setJobXP("brewer", list.get(8));
                jobsManager.setJobXP("builder", list.get(9));
                jobsManager.setJobXP("farmer", list.get(10));
                jobsManager.setJobXP("fisher", list.get(11));
                jobsManager.setJobXP("lumberjack", list.get(12));
                jobsManager.setJobXP("miner", list.get(13));
                jobsManager.setJobXP("smither", list.get(14));
                jobsManager.setJobXP("warrior", list.get(15));
                // Employed Jobs
                if (jobCount > 0)
                    for (int i = 0; i < jobCount; i++)
                        jobsManager.employJob(employedJobList.get(i));
                // Employed Time
                jobsManager.setEmployedJobTime(employedJobTime);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(JobsServerPacket.JOB_XP_PACKET, (client, handler, buf, sender) -> {
            String jobName = buf.readString();
            int currentXP = buf.readInt();
            client.execute(() -> {
                ((JobsManagerAccess) client.player).getJobsManager().setJobXP(jobName, currentXP);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(JobsServerPacket.JOB_CONFIG_SYNC_PACKET, (client, handler, buf, sender) -> {
            int employedJobs = buf.readInt();
            int jobChangeTime = buf.readInt();
            boolean resetCurrentJobXP = buf.readBoolean();
            int jobMaxLevel = buf.readInt();
            int jobXPBaseCost = buf.readInt();
            float jobXPCostMultiplicator = buf.readFloat();
            int jobXPExponent = buf.readInt();
            int jobXPMaxCost = buf.readInt();
            int brewerXP = buf.readInt();
            int builderXP = buf.readInt();
            int farmerXP = buf.readInt();
            int fisherXP = buf.readInt();
            int lumberjackXP = buf.readInt();
            int minerXP = buf.readInt();
            int smitherXP = buf.readInt();
            int warriorXP = buf.readInt();
            int xpMultiplicator = buf.readInt();
            int levelZXPMultiplicator = buf.readInt();
            int moneyMultiplicator = buf.readInt();

            client.execute(() -> {
                ConfigInit.CONFIG.employedJobs = employedJobs;
                ConfigInit.CONFIG.jobChangeTime = jobChangeTime;
                ConfigInit.CONFIG.resetCurrentJobXP = resetCurrentJobXP;
                ConfigInit.CONFIG.jobMaxLevel = jobMaxLevel;
                ConfigInit.CONFIG.jobXPBaseCost = jobXPBaseCost;
                ConfigInit.CONFIG.jobXPCostMultiplicator = jobXPCostMultiplicator;

                ConfigInit.CONFIG.jobXPExponent = jobXPExponent;
                ConfigInit.CONFIG.jobXPMaxCost = jobXPMaxCost;
                ConfigInit.CONFIG.brewerXP = brewerXP;
                ConfigInit.CONFIG.builderXP = builderXP;
                ConfigInit.CONFIG.farmerXP = farmerXP;
                ConfigInit.CONFIG.fisherXP = fisherXP;
                ConfigInit.CONFIG.lumberjackXP = lumberjackXP;
                ConfigInit.CONFIG.minerXP = minerXP;
                ConfigInit.CONFIG.smitherXP = smitherXP;
                ConfigInit.CONFIG.warriorXP = warriorXP;
                ConfigInit.CONFIG.xpMultiplicator = xpMultiplicator;
                ConfigInit.CONFIG.levelZXPMultiplicator = levelZXPMultiplicator;
                ConfigInit.CONFIG.moneyMultiplicator = moneyMultiplicator;
            });

        });
    }

    public static void writeC2SSelectJobPacket(JobsManager jobsManager, String jobName, boolean employJob) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(jobName);
        buf.writeBoolean(employJob);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(JobsServerPacket.EMPLOY_JOB_PACKET, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SSyncJobConfigPacket() {
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(JobsServerPacket.SEND_JOB_CONFIG_SYNC_PACKET, new PacketByteBuf(Unpooled.buffer())));
    }

}
