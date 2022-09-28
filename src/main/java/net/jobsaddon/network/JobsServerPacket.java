package net.jobsaddon.network;

import java.util.List;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class JobsServerPacket {

    public static final Identifier EMPLOY_JOB_PACKET = new Identifier("jobsaddon", "employ_job");
    public static final Identifier JOB_PACKET = new Identifier("jobsaddon", "job");
    public static final Identifier JOB_XP_PACKET = new Identifier("jobsaddon", "job_xp");
    public static final Identifier SEND_JOB_CONFIG_SYNC_PACKET = new Identifier("jobsaddon", "send_config_sync_packet");
    public static final Identifier JOB_CONFIG_SYNC_PACKET = new Identifier("jobsaddon", "config_sync_packet");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(EMPLOY_JOB_PACKET, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                String jobName = buffer.readString();
                boolean employJob = buffer.readBoolean();
                int employTime = buffer.readInt();
                JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();
                if (employJob) {
                    jobsManager.employJob(jobName);
                    jobsManager.setEmployedJobTime(employTime);
                } else
                    jobsManager.quitJob(jobName);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SEND_JOB_CONFIG_SYNC_PACKET, (server, player, handler, buffer, sender) -> {
            writeS2CJobConfigSyncPacket(player, ConfigInit.CONFIG.getJobConfigList());
        });
    }

    public static void writeS2CJobPacket(JobsManager jobsManager, ServerPlayerEntity serverPlayerEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        // Job Level
        buf.writeInt(jobsManager.getJobLevel("brewer"));
        buf.writeInt(jobsManager.getJobLevel("builder"));
        buf.writeInt(jobsManager.getJobLevel("farmer"));
        buf.writeInt(jobsManager.getJobLevel("fisher"));
        buf.writeInt(jobsManager.getJobLevel("lumberjack"));
        buf.writeInt(jobsManager.getJobLevel("miner"));
        buf.writeInt(jobsManager.getJobLevel("smither"));
        buf.writeInt(jobsManager.getJobLevel("warrior"));
        // Job XP
        buf.writeInt(jobsManager.getJobXP("brewer"));
        buf.writeInt(jobsManager.getJobXP("builder"));
        buf.writeInt(jobsManager.getJobXP("farmer"));
        buf.writeInt(jobsManager.getJobXP("fisher"));
        buf.writeInt(jobsManager.getJobXP("lumberjack"));
        buf.writeInt(jobsManager.getJobXP("miner"));
        buf.writeInt(jobsManager.getJobXP("smither"));
        buf.writeInt(jobsManager.getJobXP("warrior"));
        // Employed Jobs
        if (jobsManager.getEmployedJobs() != null) {
            buf.writeInt(jobsManager.getEmployedJobs().size());
            for (int i = 0; i < jobsManager.getEmployedJobs().size(); i++)
                buf.writeString(jobsManager.getEmployedJobs().get(i));
        } else
            buf.writeInt(0);
        // Employed Time
        buf.writeInt(jobsManager.getEmployedJobTime());

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(JOB_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CJobXPPacket(ServerPlayerEntity serverPlayerEntity, String jobName, int amount) {
        // set on server
        ((JobsManagerAccess) serverPlayerEntity).getJobsManager().addJobXP(serverPlayerEntity, jobName, amount);
        // send to client
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(jobName);
        buf.writeInt(amount);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(JOB_XP_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CJobConfigSyncPacket(ServerPlayerEntity serverPlayerEntity, List<Object> list) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Integer)
                buf.writeInt((int) list.get(i));
            else if (list.get(i) instanceof Float)
                buf.writeFloat((float) list.get(i));
            else if (list.get(i) instanceof Double)
                buf.writeDouble((double) list.get(i));
            else if (list.get(i) instanceof Boolean)
                buf.writeBoolean((boolean) list.get(i));
        }
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(JOB_CONFIG_SYNC_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }
}
