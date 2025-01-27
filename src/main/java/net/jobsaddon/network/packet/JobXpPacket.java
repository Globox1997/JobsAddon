package net.jobsaddon.network.packet;

import net.jobsaddon.JobsAddonMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record JobXpPacket(int jobId, int experience) implements CustomPayload {

    public static final CustomPayload.Id<JobXpPacket> PACKET_ID = new CustomPayload.Id<>(JobsAddonMain.identifierOf("job_xp_packet"));

    public static final PacketCodec<RegistryByteBuf, JobXpPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.jobId);
        buf.writeInt(value.experience);
    }, buf -> new JobXpPacket(buf.readInt(), buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}


