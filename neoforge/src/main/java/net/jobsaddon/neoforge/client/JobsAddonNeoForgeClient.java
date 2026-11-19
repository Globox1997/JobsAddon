package net.jobsaddon.neoforge.client;

import me.shedaniel.autoconfig.AutoConfigClient;
import net.jobsaddon.JobsAddonClient;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.config.JobsAddonConfig;
import net.jobsaddon.init.KeyInit;
import net.jobsaddon.network.JobsClientPacket;
import net.jobsaddon.network.packet.JobPacket;
import net.jobsaddon.network.packet.JobXpPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = JobsAddonMain.MOD_ID, dist = Dist.CLIENT)
public class JobsAddonNeoForgeClient {

    public JobsAddonNeoForgeClient(IEventBus modBus, ModContainer container) {
        JobsAddonClient.init();
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.registerCategory(KeyInit.CATEGORY);
            event.register(KeyInit.screenKey);
        });
        modBus.addListener(RegisterClientPayloadHandlersEvent.class, event -> {
            event.register(JobPacket.PACKET_ID, (payload, context) -> JobsClientPacket.handleJob(payload));
            event.register(JobXpPacket.PACKET_ID, (payload, context) -> JobsClientPacket.handleJobXp(payload));
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> KeyInit.onClientTick(Minecraft.getInstance()));
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> AutoConfigClient.getConfigScreen(JobsAddonConfig.class, parent).get());
    }
}
