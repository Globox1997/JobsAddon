package net.jobsaddon.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jobsaddon.JobsAddonClient;
import net.jobsaddon.init.KeyInit;
import net.jobsaddon.network.JobsClientPacket;
import net.jobsaddon.network.packet.JobPacket;
import net.jobsaddon.network.packet.JobXpPacket;
import net.minecraft.client.KeyMapping;

public class JobsAddonFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        JobsAddonClient.init();
        ClientPlayNetworking.registerGlobalReceiver(JobPacket.PACKET_ID, (payload, context) -> context.client().execute(() -> JobsClientPacket.handleJob(payload)));
        ClientPlayNetworking.registerGlobalReceiver(JobXpPacket.PACKET_ID, (payload, context) -> context.client().execute(() -> JobsClientPacket.handleJobXp(payload)));
        KeyMapping.Category.register(KeyInit.CATEGORY.id());
        KeyMappingHelper.registerKeyMapping(KeyInit.screenKey);
        ClientTickEvents.END_CLIENT_TICK.register(KeyInit::onClientTick);
    }

}
