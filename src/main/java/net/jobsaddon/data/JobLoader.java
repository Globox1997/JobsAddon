package net.jobsaddon.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobExperience;
import net.jobsaddon.jobs.JobExperienceType;
import net.jobsaddon.jobs.JobsManager;
import net.levelz.registry.EnchantmentRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.Reader;
import java.util.*;

public record JobLoader(RegistryWrapper.WrapperLookup wrapperLookup) implements SimpleSynchronousResourceReloadListener {

    public static final Identifier ID = JobsAddonMain.identifierOf("job_loader");

    private static final List<String> FOLDERS = List.of("job", "restricted");

    private static final List<TagExperience<?>> TAG_EXPERIENCE = new ArrayList<>();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        TAG_EXPERIENCE.clear();
        JobsManager.JOBS.clear();
        JobsManager.EXPERIENCE_MAPS.forEach(Map::clear);
        JobsManager.RESTRICTED_RECIPES.clear();

        EnchantmentRegistry.updateEnchantments(this.wrapperLookup());

        List<String> packOrder = manager.streamResourcePacks().map(ResourcePack::getId).toList();
        List<Map.Entry<Identifier, Resource>> resources = new ArrayList<>();
        for (String folder : FOLDERS) {
            resources.addAll(manager.findResources(folder, id -> id.getPath().endsWith(".json")).entrySet());
        }
        resources.sort(Comparator.<Map.Entry<Identifier, Resource>>comparingInt(entry -> packOrder.indexOf(entry.getValue().getPackId())).thenComparing(Map.Entry::getKey));

        for (Map.Entry<Identifier, Resource> entry : resources) {
            Identifier id = entry.getKey();
            if (!ConfigInit.CONFIG.defaultJobs && id.getPath().equals("job/default.json")) {
                continue;
            }
            try (Reader reader = entry.getValue().getReader()) {
                JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();
                for (String mapKey : data.keySet()) {
                    try {
                        if (mapKey.equals("restriction")) {
                            loadRestriction(data.getAsJsonObject(mapKey));
                        } else {
                            loadJob(id, data.getAsJsonObject(mapKey));
                        }
                    } catch (Exception e) {
                        JobsAddonMain.LOGGER.error("Error occurred while loading {} of resource {}. {}", mapKey, id, e.toString());
                    }
                }
            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id, e.toString());
            }
        }
    }

    // Gets called once the tags are loaded
    public static void resolveTags() {
        for (TagExperience<?> tagExperience : TAG_EXPERIENCE) {
            tagExperience.resolve();
        }
    }

    private void loadRestriction(JsonObject jsonObject) {
        if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
            JobsManager.RESTRICTED_RECIPES.clear();
        }
        for (JsonElement element : JsonHelper.getArray(jsonObject, "recipes")) {
            Identifier recipeId = Identifier.tryParse(element.getAsString());
            if (recipeId == null) {
                JobsAddonMain.LOGGER.warn("{} is not a valid recipe identifier", element.getAsString());
                continue;
            }
            JobsManager.RESTRICTED_RECIPES.add(recipeId);
        }
    }

    private void loadJob(Identifier fileId, JsonObject jobJsonObject) {
        int jobId = JsonHelper.getInt(jobJsonObject, "id");
        boolean replace = JsonHelper.getBoolean(jobJsonObject, "replace", false);
        Job existingJob = JobsManager.JOBS.get(jobId);

        if (existingJob != null && !replace) {
            // extend the existing job
            if (jobJsonObject.has("key") && !existingJob.getKey().equals(JsonHelper.getString(jobJsonObject, "key"))) {
                JobsAddonMain.LOGGER.warn("Id {} of job {} in {} is already used by job {}.", jobId, JsonHelper.getString(jobJsonObject, "key"), fileId, existingJob.getKey());
                return;
            }
            if (jobJsonObject.has("maxlevel")) {
                JobsManager.JOBS.put(jobId, new Job(jobId, existingJob.getKey(), JsonHelper.getInt(jobJsonObject, "maxlevel"), 0, 0));
            }
        } else {
            String key = JsonHelper.getString(jobJsonObject, "key");
            Job jobWithKey = JobsManager.getJobByKey(key);
            if (jobWithKey != null && jobWithKey.getId() != jobId) {
                JobsAddonMain.LOGGER.warn("Key {} of job id {} in {} is already used by job id {}.", key, jobId, fileId, jobWithKey.getId());
                return;
            }
            if (replace) {
                JobsManager.removeJobExperience(jobId);
                TAG_EXPERIENCE.removeIf(tagExperience -> tagExperience.jobExperience().getId() == jobId);
            }
            JobsManager.JOBS.put(jobId, new Job(jobId, key, JsonHelper.getInt(jobJsonObject, "maxlevel"), 0, 0));
        }

        loadExperience(fileId, jobJsonObject, jobId, "blockbreak", "blocks", Registries.BLOCK, JobsManager.BLOCK_BREAK_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "blockplace", "blocks", Registries.BLOCK, JobsManager.BLOCK_PLACE_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "crafting", "items", Registries.ITEM, JobsManager.ITEM_CRAFT_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "itemdrop", "items", Registries.ITEM, JobsManager.ITEM_DROP_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "entitykill", "entities", Registries.ENTITY_TYPE, JobsManager.ENTITY_KILL_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "brewing", "effects", Registries.POTION, JobsManager.BREWING_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "smithing", "items", Registries.ITEM, JobExperienceType.SMITHING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "stonecutting", "items", Registries.ITEM, JobExperienceType.STONECUTTING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "breeding", "entities", Registries.ENTITY_TYPE, JobExperienceType.BREEDING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "taming", "entities", Registries.ENTITY_TYPE, JobExperienceType.TAMING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "trading", "items", Registries.ITEM, JobExperienceType.TRADING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "harvest", "blocks", Registries.BLOCK, JobExperienceType.HARVEST.getExperienceMap());
        loadEnchantingExperience(fileId, jobJsonObject, jobId);
    }

    private <T> void loadExperience(Identifier fileId, JsonObject jobJsonObject, int jobId, String type, String arrayKey, Registry<T> registry, Map<Integer, List<JobExperience>> map) {
        if (!jobJsonObject.has(type)) {
            return;
        }
        JsonObject typeJsonObject = JsonHelper.getObject(jobJsonObject, type);
        for (String experienceKey : typeJsonObject.keySet()) {
            Integer experience = parseExperience(fileId, experienceKey);
            if (experience == null) {
                continue;
            }
            JsonObject experienceJsonObject = JsonHelper.getObject(typeJsonObject, experienceKey);
            if (JsonHelper.getBoolean(experienceJsonObject, "replace", false)) {
                JobsManager.removeJobExperience(map, jobId, experience);
                TAG_EXPERIENCE.removeIf(tagExperience -> tagExperience.map() == map && tagExperience.jobExperience().getId() == jobId && tagExperience.jobExperience().getExperience() == experience);
            }
            if (!JsonHelper.hasArray(experienceJsonObject, arrayKey)) {
                JobsAddonMain.LOGGER.warn("{} in {} is missing an {} array", experienceJsonObject, fileId, arrayKey);
                continue;
            }
            JobExperience jobExperience = new JobExperience(jobId, experience);
            for (JsonElement element : JsonHelper.getArray(experienceJsonObject, arrayKey)) {
                String entry = element.getAsString();
                if (entry.startsWith("#")) {
                    Identifier tagId = Identifier.tryParse(entry.substring(1));
                    if (tagId == null) {
                        JobsAddonMain.LOGGER.warn("{} in {} is not a valid tag identifier", entry, fileId);
                        continue;
                    }
                    TAG_EXPERIENCE.add(new TagExperience<>(registry, TagKey.of(registry.getKey(), tagId), map, jobExperience));
                    continue;
                }
                Identifier entryId = Identifier.tryParse(entry);
                if (entryId == null || !registry.containsId(entryId)) {
                    JobsAddonMain.LOGGER.warn("{} in {} is not a valid {} identifier", entry, fileId, registry.getKey().getValue().getPath());
                    continue;
                }
                JobsManager.addJobExperience(map, registry.getRawId(registry.get(entryId)), jobExperience, true);
            }
        }
    }

    private void loadEnchantingExperience(Identifier fileId, JsonObject jobJsonObject, int jobId) {
        if (!jobJsonObject.has("enchanting")) {
            return;
        }
        JsonObject typeJsonObject = JsonHelper.getObject(jobJsonObject, "enchanting");
        for (String experienceKey : typeJsonObject.keySet()) {
            Integer experience = parseExperience(fileId, experienceKey);
            if (experience == null) {
                continue;
            }
            JsonObject experienceJsonObject = JsonHelper.getObject(typeJsonObject, experienceKey);
            if (JsonHelper.getBoolean(experienceJsonObject, "replace", false)) {
                JobsManager.removeJobExperience(JobsManager.ENCHANTMENT_EXPERIENCE, jobId, experience);
            }
            if (!JsonHelper.hasJsonObject(experienceJsonObject, "enchantments")) {
                JobsAddonMain.LOGGER.warn("{} in {} is missing an enchantments object list", experienceJsonObject, fileId);
                continue;
            }
            JsonObject enchantments = JsonHelper.getObject(experienceJsonObject, "enchantments");
            for (String enchantment : enchantments.keySet()) {
                Identifier enchantmentId = Identifier.tryParse(enchantment);
                int level = enchantments.get(enchantment).getAsInt();
                if (enchantmentId == null || !EnchantmentRegistry.containsId(enchantmentId, level)) {
                    JobsAddonMain.LOGGER.warn("{} in {} is not a valid enchantment identifier", enchantment, fileId);
                    continue;
                }
                JobsManager.addJobExperience(JobsManager.ENCHANTMENT_EXPERIENCE, EnchantmentRegistry.getId(enchantmentId, level), new JobExperience(jobId, experience), true);
            }
        }
    }

    private static Integer parseExperience(Identifier fileId, String experienceKey) {
        try {
            return Integer.parseInt(experienceKey);
        } catch (NumberFormatException e) {
            JobsAddonMain.LOGGER.warn("{} in {} is not a valid experience number", experienceKey, fileId);
            return null;
        }
    }

    private record TagExperience<T>(Registry<T> registry, TagKey<T> tagKey, Map<Integer, List<JobExperience>> map, JobExperience jobExperience) {

        private void resolve() {
            boolean empty = true;
            for (RegistryEntry<T> entry : this.registry.iterateEntries(this.tagKey)) {
                JobsManager.addJobExperience(this.map, this.registry.getRawId(entry.value()), this.jobExperience, false);
                empty = false;
            }
            if (empty) {
                JobsAddonMain.LOGGER.warn("Tag #{} is empty or does not exist", this.tagKey.id());
            }
        }
    }

}
