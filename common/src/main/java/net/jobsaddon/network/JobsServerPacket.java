package net.jobsaddon.network;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.api.JobEmploymentEvents;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.packet.EmployPacket;
import net.jobsaddon.network.packet.JobPacket;
import net.jobsaddon.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

public class JobsServerPacket {

    public static void handleEmploy(ServerPlayer player, EmployPacket payload) {
        int jobId = payload.jobId();
        JobsManager jobsManager = ((JobsManagerAccess) player).getJobsManager();
        if (payload.employ()) {
            if (JobsManager.JOBS.containsKey(jobId) && jobsManager.canEmployJob(jobId)
                    && JobEmploymentEvents.ALLOW_EMPLOY.invoker().allowEmploy(player, jobsManager.getJob(jobId))) {
                jobsManager.employJob(jobId);
                jobsManager.setEmployedJobTime(ConfigInit.CONFIG.jobChangeTime);
            }
        } else {
            jobsManager.quitJob(jobId);
        }
        writeS2CJobPacket(jobsManager, player);
    }

    public static void writeS2CJobPacket(JobsManager jobsManager, ServerPlayer serverPlayerEntity) {
        List<Job> activeJobs = jobsManager.getPlayerJobs().values().stream().filter(job -> JobsManager.JOBS.containsKey(job.getId())).toList();
        Services.network().sendToPlayer(serverPlayerEntity, new JobPacket(new JobPacket.JobRecord(activeJobs), jobsManager.getEmployedJobsList(), jobsManager.getEmployedJobTime()));
    }

}
