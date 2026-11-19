package net.jobsaddon.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobExperience;
import net.jobsaddon.jobs.JobExperienceType;
import net.jobsaddon.jobs.JobsManager;
import net.levelz.registry.EnchantmentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;

public class JobLoader implements ResourceManagerReloadListener {

    public static final Identifier ID = JobsAddonMain.identifierOf("job_loader");

    private static final List<String> FOLDERS = List.of("job", "restricted");

    private static final List<TagExperience<?>> TAG_EXPERIENCE = new ArrayList<>();

    private final Function<PreparableReloadListener.SharedState, HolderLookup.Provider> lookupGetter;
    private HolderLookup.Provider wrapperLookup;

    public JobLoader(Function<PreparableReloadListener.SharedState, HolderLookup.Provider> lookupGetter) {
        this.lookupGetter = lookupGetter;
    }

    @Override
    public void prepareSharedState(PreparableReloadListener.SharedState currentReload) {
        this.wrapperLookup = this.lookupGetter.apply(currentReload);
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        TAG_EXPERIENCE.clear();
        JobsManager.JOBS.clear();
        JobsManager.EXPERIENCE_MAPS.forEach(Map::clear);
        JobsManager.RESTRICTED_RECIPES.clear();

        EnchantmentRegistry.updateEnchantments(this.wrapperLookup);

        List<String> packOrder = manager.listPacks().map(PackResources::packId).toList();
        List<Map.Entry<Identifier, Resource>> resources = new ArrayList<>();
        for (String folder : FOLDERS) {
            resources.addAll(manager.listResources(folder, id -> id.getPath().endsWith(".json")).entrySet());
        }
        resources.sort(Comparator.<Map.Entry<Identifier, Resource>>comparingInt(entry -> packOrder.indexOf(entry.getValue().sourcePackId())).thenComparing(Map.Entry::getKey));

        for (Map.Entry<Identifier, Resource> entry : resources) {
            Identifier id = entry.getKey();
            if (!ConfigInit.CONFIG.defaultJobs && id.getPath().equals("job/default.json")) {
                continue;
            }
            try (Reader reader = entry.getValue().openAsReader()) {
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
        if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
            JobsManager.RESTRICTED_RECIPES.clear();
        }
        for (JsonElement element : GsonHelper.getAsJsonArray(jsonObject, "recipes")) {
            Identifier recipeId = Identifier.tryParse(element.getAsString());
            if (recipeId == null) {
                JobsAddonMain.LOGGER.warn("{} is not a valid recipe identifier", element.getAsString());
                continue;
            }
            JobsManager.RESTRICTED_RECIPES.add(recipeId);
        }
    }

    private void loadJob(Identifier fileId, JsonObject jobJsonObject) {
        int jobId = GsonHelper.getAsInt(jobJsonObject, "id");
        boolean replace = GsonHelper.getAsBoolean(jobJsonObject, "replace", false);
        Job existingJob = JobsManager.JOBS.get(jobId);

        if (existingJob != null && !replace) {
            // extend the existing job
            if (jobJsonObject.has("key") && !existingJob.getKey().equals(GsonHelper.getAsString(jobJsonObject, "key"))) {
                JobsAddonMain.LOGGER.warn("Id {} of job {} in {} is already used by job {}.", jobId, GsonHelper.getAsString(jobJsonObject, "key"), fileId, existingJob.getKey());
                return;
            }
            if (jobJsonObject.has("maxlevel")) {
                JobsManager.JOBS.put(jobId, new Job(jobId, existingJob.getKey(), GsonHelper.getAsInt(jobJsonObject, "maxlevel"), 0, 0));
            }
        } else {
            String key = GsonHelper.getAsString(jobJsonObject, "key");
            Job jobWithKey = JobsManager.getJobByKey(key);
            if (jobWithKey != null && jobWithKey.getId() != jobId) {
                JobsAddonMain.LOGGER.warn("Key {} of job id {} in {} is already used by job id {}.", key, jobId, fileId, jobWithKey.getId());
                return;
            }
            if (replace) {
                JobsManager.removeJobExperience(jobId);
                TAG_EXPERIENCE.removeIf(tagExperience -> tagExperience.jobExperience().getId() == jobId);
            }
            JobsManager.JOBS.put(jobId, new Job(jobId, key, GsonHelper.getAsInt(jobJsonObject, "maxlevel"), 0, 0));
        }

        loadExperience(fileId, jobJsonObject, jobId, "blockbreak", "blocks", BuiltInRegistries.BLOCK, JobsManager.BLOCK_BREAK_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "blockplace", "blocks", BuiltInRegistries.BLOCK, JobsManager.BLOCK_PLACE_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "crafting", "items", BuiltInRegistries.ITEM, JobsManager.ITEM_CRAFT_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "itemdrop", "items", BuiltInRegistries.ITEM, JobsManager.ITEM_DROP_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "entitykill", "entities", BuiltInRegistries.ENTITY_TYPE, JobsManager.ENTITY_KILL_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "brewing", "effects", BuiltInRegistries.POTION, JobsManager.BREWING_EXPERIENCE);
        loadExperience(fileId, jobJsonObject, jobId, "smithing", "items", BuiltInRegistries.ITEM, JobExperienceType.SMITHING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "stonecutting", "items", BuiltInRegistries.ITEM, JobExperienceType.STONECUTTING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "breeding", "entities", BuiltInRegistries.ENTITY_TYPE, JobExperienceType.BREEDING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "taming", "entities", BuiltInRegistries.ENTITY_TYPE, JobExperienceType.TAMING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "trading", "items", BuiltInRegistries.ITEM, JobExperienceType.TRADING.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "harvest", "blocks", BuiltInRegistries.BLOCK, JobExperienceType.HARVEST.getExperienceMap());
        loadExperience(fileId, jobJsonObject, jobId, "casting", "items", BuiltInRegistries.ITEM, JobExperienceType.CASTING.getExperienceMap());
        loadEnchantingExperience(fileId, jobJsonObject, jobId);
    }

    private <T> void loadExperience(Identifier fileId, JsonObject jobJsonObject, int jobId, String type, String arrayKey, Registry<T> registry, Map<Integer, List<JobExperience>> map) {
        if (!jobJsonObject.has(type)) {
            return;
        }
        JsonObject typeJsonObject = GsonHelper.getAsJsonObject(jobJsonObject, type);
        for (String experienceKey : typeJsonObject.keySet()) {
            Integer experience = parseExperience(fileId, experienceKey);
            if (experience == null) {
                continue;
            }
            JsonObject experienceJsonObject = GsonHelper.getAsJsonObject(typeJsonObject, experienceKey);
            if (GsonHelper.getAsBoolean(experienceJsonObject, "replace", false)) {
                JobsManager.removeJobExperience(map, jobId, experience);
                TAG_EXPERIENCE.removeIf(tagExperience -> tagExperience.map() == map && tagExperience.jobExperience().getId() == jobId && tagExperience.jobExperience().getExperience() == experience);
            }
            if (!GsonHelper.isArrayNode(experienceJsonObject, arrayKey)) {
                JobsAddonMain.LOGGER.warn("{} in {} is missing an {} array", experienceJsonObject, fileId, arrayKey);
                continue;
            }
            JobExperience jobExperience = new JobExperience(jobId, experience);
            for (JsonElement element : GsonHelper.getAsJsonArray(experienceJsonObject, arrayKey)) {
                String entry = element.getAsString();
                if (entry.startsWith("#")) {
                    Identifier tagId = Identifier.tryParse(entry.substring(1));
                    if (tagId == null) {
                        JobsAddonMain.LOGGER.warn("{} in {} is not a valid tag identifier", entry, fileId);
                        continue;
                    }
                    TAG_EXPERIENCE.add(new TagExperience<>(registry, TagKey.create(registry.key(), tagId), map, jobExperience));
                    continue;
                }
                Identifier entryId = Identifier.tryParse(entry);
                if (entryId == null || !registry.containsKey(entryId)) {
                    JobsAddonMain.LOGGER.warn("{} in {} is not a valid {} identifier", entry, fileId, registry.key().identifier().getPath());
                    continue;
                }
                JobsManager.addJobExperience(map, registry.getId(registry.getValue(entryId)), jobExperience, true);
            }
        }
    }

    private void loadEnchantingExperience(Identifier fileId, JsonObject jobJsonObject, int jobId) {
        if (!jobJsonObject.has("enchanting")) {
            return;
        }
        JsonObject typeJsonObject = GsonHelper.getAsJsonObject(jobJsonObject, "enchanting");
        for (String experienceKey : typeJsonObject.keySet()) {
            Integer experience = parseExperience(fileId, experienceKey);
            if (experience == null) {
                continue;
            }
            JsonObject experienceJsonObject = GsonHelper.getAsJsonObject(typeJsonObject, experienceKey);
            if (GsonHelper.getAsBoolean(experienceJsonObject, "replace", false)) {
                JobsManager.removeJobExperience(JobsManager.ENCHANTMENT_EXPERIENCE, jobId, experience);
            }
            if (!GsonHelper.isObjectNode(experienceJsonObject, "enchantments")) {
                JobsAddonMain.LOGGER.warn("{} in {} is missing an enchantments object list", experienceJsonObject, fileId);
                continue;
            }
            JsonObject enchantments = GsonHelper.getAsJsonObject(experienceJsonObject, "enchantments");
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
            for (Holder<T> entry : this.registry.getTagOrEmpty(this.tagKey)) {
                JobsManager.addJobExperience(this.map, this.registry.getId(entry.value()), this.jobExperience, false);
                empty = false;
            }
            if (empty) {
                JobsAddonMain.LOGGER.warn("Tag #{} is empty or does not exist", this.tagKey.location());
            }
        }
    }

}
