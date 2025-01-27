package net.jobsaddon.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.packet.JobPacket;
import net.jobsaddon.network.packet.JobXpPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobsClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(JobPacket.PACKET_ID, (payload, context) -> {
            JobPacket.JobRecord jobRecord = payload.jobRecord();
            List<Integer> employedJobsList = payload.employedJobsList();
            int employedJobTime = payload.employedJobTime();

            context.client().execute(() -> {
                JobsManager jobsManager = ((JobsManagerAccess) context.player()).getJobsManager();

                Map<Integer, Job> jobMap = new HashMap();
                for (Job job : jobRecord.jobs()) {
                    jobMap.put(job.getId(), job);
                }
                jobsManager.setPlayerJobs(jobMap);
                jobsManager.getEmployedJobsList().clear();
                jobsManager.getEmployedJobsList().addAll(employedJobsList);
                jobsManager.setEmployedJobTime(employedJobTime);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(JobXpPacket.PACKET_ID, (payload, context) -> {
            int jobId = payload.jobId();
            int experience = payload.experience();

            context.client().execute(() -> {
                ((JobsManagerAccess) context.player()).getJobsManager().setJobXP(jobId, experience);
            });
        });
    }

}
