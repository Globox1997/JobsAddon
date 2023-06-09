package net.jobsaddon.init;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.config.JobsAddonConfig;
import net.jobsaddon.network.JobsClientPacket;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class ConfigInit {
    public static JobsAddonConfig CONFIG = new JobsAddonConfig();

    public static void init() {
        AutoConfig.register(JobsAddonConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(JobsAddonConfig.class).getConfig();

        AutoConfig.getConfigHolder(JobsAddonConfig.class).registerSaveListener((manager, data) -> {
            if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT) && !MinecraftClient.getInstance().isInSingleplayer()
                    && MinecraftClient.getInstance().getNetworkHandler() != null)
                JobsClientPacket.writeC2SSyncJobConfigPacket();
            return ActionResult.SUCCESS;
        });
        AutoConfig.getConfigHolder(JobsAddonConfig.class).registerLoadListener((manager, newData) -> {
            if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT) && !MinecraftClient.getInstance().isInSingleplayer()
                    && MinecraftClient.getInstance().getNetworkHandler() != null)
                JobsClientPacket.writeC2SSyncJobConfigPacket();
            return ActionResult.SUCCESS;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // if (!server.isSingleplayer()) // set in sp too
            server.execute(() -> {
                JobsServerPacket.writeS2CJobConfigSyncPacket(handler.player, ConfigInit.CONFIG.getJobConfigList());
            });
        });
        if (CONFIG.devMode) {
            List<Iterator<Identifier>> iteratorList = List.of(Registries.POTION.getIds().iterator(), Registries.ENCHANTMENT.getIds().iterator(), Registries.ENTITY_TYPE.getIds().iterator(),
                    Registries.ITEM.getIds().iterator(), Registries.BLOCK.getIds().iterator());
            List<String> registryNames = List.of("POTIONS", "ENCHANTMENTS", "ENTITY_TYPES", "ITEMS", "BLOCKS");

            for (int i = 0; i < iteratorList.size(); i++) {
                Iterator<Identifier> iterator = iteratorList.get(i);
                writeId(registryNames.get(i));

                while (iterator.hasNext()) {
                    writeId(iterator.next().toString());
                }
                writeId(System.lineSeparator());
            }
            JobsAddonMain.LOGGER.warn("Cauton! JobsAddon dev mode is in use, check joblist.json inside your mc directory");
        }

    }

    public static void writeId(String string) {
        try (FileWriter idFile = new FileWriter("joblist.json", true)) {
            idFile.append("\"" + string + "\",");
            idFile.append(System.lineSeparator());
        } catch (IOException e) {
        }
    }

}