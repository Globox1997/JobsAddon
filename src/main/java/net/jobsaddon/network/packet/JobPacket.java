package net.jobsaddon.network.packet;

import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.jobs.Job;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record JobPacket(JobRecord jobRecord, List<Integer> employedJobsList, int employedJobTime) implements CustomPayload {

    public static final CustomPayload.Id<JobPacket> PACKET_ID = new CustomPayload.Id<>(JobsAddonMain.identifierOf("job_packet"));

    public static final PacketCodec<RegistryByteBuf, JobPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        value.jobRecord.write(buf);
        buf.writeCollection(value.employedJobsList, PacketByteBuf::writeInt);
        buf.writeInt(value.employedJobTime);

    }, buf -> new JobPacket(JobRecord.read(buf), buf.readList(PacketByteBuf::readInt), buf.readInt()));

    public record JobRecord(List<Job> jobs) {

        public void write(PacketByteBuf buf) {
            buf.writeInt(jobs().size());
            for (int i = 0; i < jobs().size(); i++) {
                Job playerJob = jobs.get(i);
                buf.writeInt(playerJob.getId());
                buf.writeString(playerJob.getKey());
                buf.writeInt(playerJob.getMaxLevel());
                buf.writeInt(playerJob.getLevel());
                buf.writeInt(playerJob.getExperience());
            }
        }

        public static JobRecord read(PacketByteBuf buf) {
            List<Job> playerJobs = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                int id = buf.readInt();
                String key = buf.readString();
                int maxLevel = buf.readInt();
                int level = buf.readInt();
                int experience = buf.readInt();
                playerJobs.add(new Job(id, key, maxLevel, level, experience));
            }
            return new JobRecord(playerJobs);
        }

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}


