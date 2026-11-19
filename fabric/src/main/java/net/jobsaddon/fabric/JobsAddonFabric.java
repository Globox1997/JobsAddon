package net.jobsaddon.fabric;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.data.JobLoader;
import net.jobsaddon.init.CommandInit;
import net.jobsaddon.init.CriteriaInit;
import net.jobsaddon.init.EventInit;
import net.jobsaddon.init.LoaderInit;
import net.jobsaddon.jobs.PlacedBlockTracker;
import net.jobsaddon.network.JobsServerPacket;
import net.jobsaddon.network.packet.EmployPacket;
import net.jobsaddon.network.packet.JobPacket;
import net.jobsaddon.network.packet.JobXpPacket;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;

public class JobsAddonFabric implements ModInitializer {

    public static AttachmentType<Long2IntOpenHashMap> PLACED_BLOCKS;

    @Override
    public void onInitialize() {
        JobsAddonMain.init();
        CriteriaInit.register((id, trigger) -> Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger));
        PLACED_BLOCKS = AttachmentRegistry.<Long2IntOpenHashMap>builder().persistent(PlacedBlockTracker.CODEC).initializer(PlacedBlockTracker::createMap)
                .buildAndRegister(JobsAddonMain.identifierOf("placed_blocks"));

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(JobLoader.ID, new JobLoader(currentReload -> currentReload.get(ResourceLoader.REGISTRY_LOOKUP_KEY)));
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) {
                LoaderInit.onTagsLoaded();
            }
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> LoaderInit.onDataPackReload(server, success));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandInit.register(dispatcher));

        PayloadTypeRegistry.serverboundPlay().register(EmployPacket.PACKET_ID, EmployPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JobPacket.PACKET_ID, JobPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JobXpPacket.PACKET_ID, JobXpPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EmployPacket.PACKET_ID, (payload, context) -> context.server().execute(() -> JobsServerPacket.handleEmploy(context.player(), payload)));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> EventInit.onPlayerJoin(handler.getPlayer()));
        ServerPlayerEvents.COPY_FROM.register(EventInit::onPlayerCopy);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> EventInit.onPlayerRespawn(newPlayer, alive));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> EventInit.onPlayerChangeLevel(player));

        ModContainer container = FabricLoader.getInstance().getModContainer(JobsAddonMain.MOD_ID).orElseThrow();
        for (String modId : EventInit.COMPAT_PACKS) {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                ResourceLoader.registerBuiltinPack(JobsAddonMain.identifierOf(modId + "_compat"), container, PackActivationType.DEFAULT_ENABLED);
            }
        }
    }
}
