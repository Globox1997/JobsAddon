package net.jobsaddon.network.packet;

import net.jobsaddon.JobsAddonMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record JobXpPacket(int jobId, int experience) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JobXpPacket> PACKET_ID = new CustomPacketPayload.Type<>(JobsAddonMain.identifierOf("job_xp_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JobXpPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeInt(value.jobId);
        buf.writeInt(value.experience);
    }, buf -> new JobXpPacket(buf.readInt(), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}


