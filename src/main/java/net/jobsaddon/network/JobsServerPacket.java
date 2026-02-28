package net.jobsaddon.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.packet.EmployPacket;
import net.jobsaddon.network.packet.JobPacket;
import net.jobsaddon.network.packet.JobXpPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class JobsServerPacket {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(EmployPacket.PACKET_ID, EmployPacket.PACKET_CODEC);

        PayloadTypeRegistry.playS2C().register(JobPacket.PACKET_ID, JobPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(JobXpPacket.PACKET_ID, JobXpPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(EmployPacket.PACKET_ID, (payload, context) -> {
            int jobId = payload.jobId();
            boolean employJob = payload.employ();
            context.server().execute(() -> {
                JobsManager jobsManager = ((JobsManagerAccess) context.player()).getJobsManager();
                if (employJob) {
                    if (jobsManager.canEmployJob(jobId)) {
                        jobsManager.employJob(jobId);
                        jobsManager.setEmployedJobTime(ConfigInit.CONFIG.jobChangeTime);
                    }
                } else {
                    jobsManager.quitJob(jobId);
                }
            });
        });
    }

    public static void writeS2CJobPacket(JobsManager jobsManager, ServerPlayerEntity serverPlayerEntity) {
        List<Job> activeJobs = jobsManager.getPlayerJobs().values().stream().filter(job -> JobsManager.JOBS.containsKey(job.getId())).toList();
        ServerPlayNetworking.send(serverPlayerEntity, new JobPacket(new JobPacket.JobRecord(activeJobs), jobsManager.getEmployedJobsList(), jobsManager.getEmployedJobTime()));
    }

}
