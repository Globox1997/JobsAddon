package net.jobsaddon.network;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.packet.JobPacket;
import net.jobsaddon.network.packet.JobXpPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class JobsClientPacket {

    public static void handleJob(JobPacket payload) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();

        Map<Integer, Job> jobMap = new HashMap<>();
        for (Job job : payload.jobRecord().jobs()) {
            jobMap.put(job.getId(), job);
        }
        jobsManager.setPlayerJobs(jobMap);
        jobsManager.getEmployedJobsList().clear();
        jobsManager.getEmployedJobsList().addAll(payload.employedJobsList());
        jobsManager.setEmployedJobTime(payload.employedJobTime());
    }

    public static void handleJobXp(JobXpPacket payload) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ((JobsManagerAccess) player).getJobsManager().setJobXP(payload.jobId(), payload.experience());
        }
    }

}
