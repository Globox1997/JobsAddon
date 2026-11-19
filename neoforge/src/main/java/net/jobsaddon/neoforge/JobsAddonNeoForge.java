package net.jobsaddon.neoforge;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(JobsAddonMain.MOD_ID)
public class JobsAddonNeoForge {

    public static AttachmentType<Long2IntOpenHashMap> PLACED_BLOCKS;

    public JobsAddonNeoForge(IEventBus modBus) {
        JobsAddonMain.init();
        modBus.addListener(RegisterEvent.class, JobsAddonNeoForge::register);
        modBus.addListener(RegisterPayloadHandlersEvent.class, JobsAddonNeoForge::registerPayloads);
        modBus.addListener(AddPackFindersEvent.class, JobsAddonNeoForge::addPackFinders);

        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event -> {
            HolderLookup.Provider registryLookup = event.getServerResources().getRegistryLookup();
            event.addListener(JobLoader.ID, new JobLoader(currentReload -> registryLookup));
        });
        NeoForge.EVENT_BUS.addListener(TagsUpdatedEvent.ServerDataLoad.class, event -> LoaderInit.onTagsLoaded());
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event -> {
            if (event.getPlayer() == null) {
                LoaderInit.onDataPackReload(event.getPlayerList().getServer(), true);
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> CommandInit.register(event.getDispatcher()));

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                EventInit.onPlayerJoin(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.Clone.class, event -> {
            if (event.getOriginal() instanceof ServerPlayer oldPlayer && event.getEntity() instanceof ServerPlayer newPlayer) {
                EventInit.onPlayerCopy(oldPlayer, newPlayer, !event.isWasDeath());
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                EventInit.onPlayerRespawn(player, event.isEndConquered());
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                EventInit.onPlayerChangeLevel(player);
            }
        });
    }

    private static void register(RegisterEvent event) {
        event.register(Registries.TRIGGER_TYPE, helper -> CriteriaInit.register(helper::register));
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, helper -> {
            PLACED_BLOCKS = AttachmentType.builder(PlacedBlockTracker::createMap).serialize(PlacedBlockTracker.CODEC.fieldOf("placed_blocks")).build();
            helper.register(JobsAddonMain.identifierOf("placed_blocks"), PLACED_BLOCKS);
        });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(EmployPacket.PACKET_ID, EmployPacket.PACKET_CODEC, (payload, context) -> JobsServerPacket.handleEmploy((ServerPlayer) context.player(), payload));
        registrar.playToClient(JobPacket.PACKET_ID, JobPacket.PACKET_CODEC);
        registrar.playToClient(JobXpPacket.PACKET_ID, JobXpPacket.PACKET_CODEC);
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        for (String modId : EventInit.COMPAT_PACKS) {
            if (ModList.get().isLoaded(modId)) {
                Identifier packId = JobsAddonMain.identifierOf(modId + "_compat");
                event.addPackFinders(JobsAddonMain.identifierOf("resourcepacks/" + packId.getPath()), PackType.SERVER_DATA, Component.literal(packId.toString()), PackSource.BUILT_IN, false,
                        Pack.Position.TOP);
            }
        }
    }
}
