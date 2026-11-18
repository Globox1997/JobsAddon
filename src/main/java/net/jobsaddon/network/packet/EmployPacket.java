package net.jobsaddon.network.packet;

import net.jobsaddon.JobsAddonMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EmployPacket(int jobId, boolean employ) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EmployPacket> PACKET_ID = new CustomPacketPayload.Type<>(JobsAddonMain.identifierOf("employ_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EmployPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeInt(value.jobId);
        buf.writeBoolean(value.employ);
    }, buf -> new EmployPacket(buf.readInt(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}


