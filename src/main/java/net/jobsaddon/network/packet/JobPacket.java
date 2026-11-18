package net.jobsaddon.network.packet;

import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.jobs.Job;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.ArrayList;
import java.util.List;

public record JobPacket(JobRecord jobRecord, List<Integer> employedJobsList, int employedJobTime) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JobPacket> PACKET_ID = new CustomPacketPayload.Type<>(JobsAddonMain.identifierOf("job_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JobPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        value.jobRecord.write(buf);
        buf.writeInt(value.employedJobsList.size());
        for (int jobId : value.employedJobsList) {
            buf.writeInt(jobId);
        }
        buf.writeInt(value.employedJobTime);

    }, buf -> new JobPacket(JobRecord.read(buf), readEmployedJobs(buf), buf.readInt()));

    private static List<Integer> readEmployedJobs(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Integer> employedJobsList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            employedJobsList.add(buf.readInt());
        }
        return employedJobsList;
    }

    public record JobRecord(List<Job> jobs) {

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(jobs().size());
            for (int i = 0; i < jobs().size(); i++) {
                Job playerJob = jobs.get(i);
                buf.writeInt(playerJob.getId());
                buf.writeUtf(playerJob.getKey());
                buf.writeInt(playerJob.getMaxLevel());
                buf.writeInt(playerJob.getLevel());
                buf.writeInt(playerJob.getExperience());
            }
        }

        public static JobRecord read(FriendlyByteBuf buf) {
            List<Job> playerJobs = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                int id = buf.readInt();
                String key = buf.readUtf();
                int maxLevel = buf.readInt();
                int level = buf.readInt();
                int experience = buf.readInt();
                playerJobs.add(new Job(id, key, maxLevel, level, experience));
            }
            return new JobRecord(playerJobs);
        }

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}


