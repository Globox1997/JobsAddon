package net.jobsaddon.network.packet;

import net.jobsaddon.JobsAddonMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record EmployPacket(int jobId, boolean employ) implements CustomPayload {

    public static final CustomPayload.Id<EmployPacket> PACKET_ID = new CustomPayload.Id<>(JobsAddonMain.identifierOf("employ_packet"));

    public static final PacketCodec<RegistryByteBuf, EmployPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.jobId);
        buf.writeBoolean(value.employ);
    }, buf -> new EmployPacket(buf.readInt(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}


