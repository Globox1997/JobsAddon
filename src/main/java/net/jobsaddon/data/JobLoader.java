package net.jobsaddon.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobExperience;
import net.jobsaddon.jobs.JobsManager;
import net.levelz.registry.EnchantmentRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JobLoader implements SimpleSynchronousResourceReloadListener {

    private static final List<String> TYPES = List.of("blockbreak", "crafting", "enchanting", "brewing", "entitykill", "blockplace", "itemdrop");

    private static final List<Integer> JOB_LIST = new ArrayList<>();

    @Override
    public Identifier getFabricId() {
        return JobsAddonMain.identifierOf("job_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        JOB_LIST.clear();
        JobsManager.JOBS.clear();
        JobsManager.BLOCK_BREAK_EXPERIENCE.clear();
        JobsManager.BLOCK_PLACE_EXPERIENCE.clear();
        JobsManager.ITEM_DROP_EXPERIENCE.clear();
        JobsManager.ITEM_CRAFT_EXPERIENCE.clear();
        JobsManager.ENCHANTMENT_EXPERIENCE.clear();
        JobsManager.BREWING_EXPERIENCE.clear();
        JobsManager.ENTITY_KILL_EXPERIENCE.clear();
        JobsManager.RESTRICTED_RECIPES.clear();

        manager.findResources("job", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                if (!ConfigInit.CONFIG.defaultJobs && id.getPath().endsWith("/default.json")) {
                    return;
                }

                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                for (String mapKey : data.keySet()) {
                    JsonObject jobJsonObject = data.getAsJsonObject(mapKey);

                    if (mapKey.equals("restriction")) {
                        for (int u = 0; u < jobJsonObject.getAsJsonArray("recipes").size(); u++) {
                            Identifier recipeId = Identifier.of(jobJsonObject.getAsJsonArray("recipes").get(u).getAsString());
//                            if (!Registries.RECIPE_TYPE.containsId(recipeId)) {
//                                JobsAddonMain.LOGGER.warn("{} is not a valid recipe identifier", recipeId);
//                                continue;
//                            }
                            JobsManager.RESTRICTED_RECIPES.add(recipeId);
                        }
                        continue;
                    }

                    // replace check
                    int identification = jobJsonObject.get("id").getAsInt();
                    if (JOB_LIST.contains(identification)) {
                        continue;
                    }
                    if (jobJsonObject.has("replace") && jobJsonObject.get("replace").getAsBoolean()) {
                        JOB_LIST.add(identification);
                    }
                    // loading check
                    if (JobsManager.JOBS.containsKey(identification)) {
                        if (jobJsonObject.has("key") && !JobsManager.JOBS.get(identification).getKey().equals(jobJsonObject.get("key").getAsString())) {
                            JobsAddonMain.LOGGER.warn("Id {} in job {} was already used by another skill.", identification, jobJsonObject.get("id").getAsString());
                            continue;
                        }
                    } else {
                        // job creation
                        String key = jobJsonObject.get("key").getAsString();
                        int maxLevel = jobJsonObject.get("maxlevel").getAsInt();
                        JobsManager.JOBS.put(identification, new Job(identification, key, maxLevel, 0, 0));
                    }
                    for (String type : TYPES) {
                        if (!jobJsonObject.has(type)) {
                            continue;
                        }
                        createJobManagerExperience(jobJsonObject, type, identification);
                    }
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

    private void createJobManagerExperience(JsonObject jsonObject, String type, int jobIdentification) {
        for (String key : jsonObject.getAsJsonObject(type).keySet()) {
            JsonObject typeJsonObject = jsonObject.getAsJsonObject(type).getAsJsonObject(key);
            int experience = Integer.parseInt(key);
            boolean replace = typeJsonObject.has("replace") && typeJsonObject.get("replace").getAsBoolean();

            if (type.equals("blockbreak")) {
                if (replace) {
                    JobsManager.BLOCK_BREAK_EXPERIENCE.values().removeIf(jobExperience -> jobExperience.getExperience() == experience);
                }
                if (!typeJsonObject.has("blocks") || !typeJsonObject.get("blocks").isJsonArray()) {
                    JobsAddonMain.LOGGER.warn("{} is missing an blocks array", typeJsonObject);
                    continue;
                }
                for (int u = 0; u < typeJsonObject.getAsJsonArray("blocks").size(); u++) {
                    // if (jsonObject.getAsJsonArray("blocks").get(u).getAsString().startsWith("#")) {
                    //     JobLists.builderBlockTagMap.put(TagKey.of(RegistryKeys.BLOCK, new Identifier(jsonObject.getAsJsonArray("blocks").get(u).getAsString().replace("#", ""))), i);
                    //     continue;
                    // }
                    Identifier blockId = Identifier.of(typeJsonObject.getAsJsonArray("blocks").get(u).getAsString());
                    if (!Registries.BLOCK.containsId(blockId)) {
                        JobsAddonMain.LOGGER.warn("{} is not a valid block identifier", blockId);
                        continue;
                    }
                    JobsManager.BLOCK_BREAK_EXPERIENCE.put(Registries.BLOCK.getRawId(Registries.BLOCK.get(blockId)), new JobExperience(jobIdentification, experience));
                }
            } else if (type.equals("crafting")) {
                if (replace) {
                    JobsManager.ITEM_CRAFT_EXPERIENCE.values().removeIf(jobExperience -> jobExperience.getExperience() == experience);
                }
                if (!typeJsonObject.has("items") || !typeJsonObject.get("items").isJsonArray()) {
                    JobsAddonMain.LOGGER.warn("{} is missing an items array", typeJsonObject);
                    continue;
                }
                for (int u = 0; u < typeJsonObject.getAsJsonArray("items").size(); u++) {
                    Identifier itemId = Identifier.of(typeJsonObject.getAsJsonArray("items").get(u).getAsString());
                    if (!Registries.ITEM.containsId(itemId)) {
                        JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", itemId);
                        continue;
                    }
                    JobsManager.ITEM_CRAFT_EXPERIENCE.put(Registries.ITEM.getRawId(Registries.ITEM.get(itemId)), new JobExperience(jobIdentification, experience));
                }
            } else if (type.equals("enchanting")) {
                if (replace) {
                    JobsManager.ENCHANTMENT_EXPERIENCE.values().removeIf(jobExperience -> jobExperience.getExperience() == experience);
                }
                if (!typeJsonObject.has("enchantments") || !typeJsonObject.get("enchantments").isJsonObject()) {
                    JobsAddonMain.LOGGER.warn("{} is missing an enchantments object list", typeJsonObject);
                    continue;
                }
                for (String enchantment : typeJsonObject.getAsJsonObject("enchantments").keySet()) {
                    Identifier enchantmentId = Identifier.of(enchantment);
                    int level = typeJsonObject.getAsJsonObject("enchantments").get(enchantment).getAsInt();
                    if (!EnchantmentRegistry.containsId(enchantmentId, level)) {
                        JobsAddonMain.LOGGER.warn("{} is not a valid enchantment identifier", enchantmentId);
                        continue;
                    }
                    JobsManager.ENCHANTMENT_EXPERIENCE.put(EnchantmentRegistry.getId(enchantmentId, level), new JobExperience(jobIdentification, experience));
                }
            } else if (type.equals("brewing")) {
                if (replace) {
                    JobsManager.BREWING_EXPERIENCE.values().removeIf(jobExperience -> jobExperience.getExperience() == experience);
                }
                if (!typeJsonObject.has("effects") || !typeJsonObject.get("effects").isJsonArray()) {
                    JobsAddonMain.LOGGER.warn("{} is missing an effect array", typeJsonObject);
                    continue;
                }
                for (int u = 0; u < typeJsonObject.getAsJsonArray("effects").size(); u++) {
                    Identifier effectId = Identifier.of(typeJsonObject.getAsJsonArray("effects").get(u).getAsString());
//                    Registries.POTION.
                    if (!Registries.POTION.containsId(effectId)) {
                        JobsAddonMain.LOGGER.warn("{} is not a valid effect identifier", effectId);
                        continue;
                    }
                    JobsManager.BREWING_EXPERIENCE.put(Registries.POTION.getRawId(Registries.POTION.get(effectId)), new JobExperience(jobIdentification, experience));
                }
            } else if (type.equals("entitykill")) {
                if (replace) {
                    JobsManager.ENTITY_KILL_EXPERIENCE.values().removeIf(jobExperience -> jobExperience.getExperience() == experience);
                }
                if (!typeJsonObject.has("entities") || !typeJsonObject.get("entities").isJsonArray()) {
                    JobsAddonMain.LOGGER.warn("{} is missing an entities array", typeJsonObject);
                    continue;
                }
                for (int u = 0; u < typeJsonObject.getAsJsonArray("entities").size(); u++) {
                    Identifier entityId = Identifier.of(typeJsonObject.getAsJsonArray("entities").get(u).getAsString());
                    if (!Registries.ENTITY_TYPE.containsId(entityId)) {
                        JobsAddonMain.LOGGER.warn("{} is not a valid entity identifier", entityId);
                        continue;
                    }
                    JobsManager.ENTITY_KILL_EXPERIENCE.put(Registries.ENTITY_TYPE.getRawId(Registries.ENTITY_TYPE.get(entityId)), new JobExperience(jobIdentification, experience));
                }
            } else if (type.equals("blockplace")) {
                if (replace) {
                    JobsManager.BLOCK_PLACE_EXPERIENCE.values().removeIf(jobExperience -> jobExperience.getExperience() == experience);
                }
                if (!typeJsonObject.has("blocks") || !typeJsonObject.get("blocks").isJsonArray()) {
                    JobsAddonMain.LOGGER.warn("{} is missing an blocks array", typeJsonObject);
                    continue;
                }
                for (int u = 0; u < typeJsonObject.getAsJsonArray("blocks").size(); u++) {
                    Identifier blockId = Identifier.of(typeJsonObject.getAsJsonArray("blocks").get(u).getAsString());
                    if (!Registries.BLOCK.containsId(blockId)) {
                        JobsAddonMain.LOGGER.warn("{} is not a valid block identifier", blockId);
                        continue;
                    }
                    JobsManager.BLOCK_PLACE_EXPERIENCE.put(Registries.BLOCK.getRawId(Registries.BLOCK.get(blockId)), new JobExperience(jobIdentification, experience));
                }
            } else if (type.equals("itemdrop")) {
                if (replace) {
                    JobsManager.ITEM_DROP_EXPERIENCE.values().removeIf(jobExperience -> jobExperience.getExperience() == experience);
                }
                if (!typeJsonObject.has("items") || !typeJsonObject.get("items").isJsonArray()) {
                    JobsAddonMain.LOGGER.warn("{} is missing an items array", typeJsonObject);
                    continue;
                }
                for (int u = 0; u < typeJsonObject.getAsJsonArray("items").size(); u++) {
                    Identifier itemId = Identifier.of(typeJsonObject.getAsJsonArray("items").get(u).getAsString());
                    if (!Registries.ITEM.containsId(itemId)) {
                        JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", itemId);
                        continue;
                    }
                    JobsManager.ITEM_DROP_EXPERIENCE.put(Registries.ITEM.getRawId(Registries.ITEM.get(itemId)), new JobExperience(jobIdentification, experience));
                }
            }
        }
    }

}
