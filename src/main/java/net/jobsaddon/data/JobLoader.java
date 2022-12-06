package net.jobsaddon.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.init.ConfigInit;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class JobLoader implements SimpleSynchronousResourceReloadListener {

    private final List<Item> potionItems = List.of(Items.POTION, Items.LINGERING_POTION, Items.SPLASH_POTION);

    // Map to store replacing bools
    private HashMap<Integer, Boolean> replaceList = new HashMap<Integer, Boolean>();

    @Override
    public Identifier getFabricId() {
        return new Identifier("jobsaddon", "job_loader");
    }

    @Override
    public void reload(ResourceManager manager) {

        refreshReplaceList();
        // brewer
        manager.findResources("brewer", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("effects") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.brewerItemBrewingMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("effects").size(); u++) {

                                if (!Registry.POTION.containsId(new Identifier(jsonObject.getAsJsonArray("effects").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid potion effect identifier", jsonObject.getAsJsonArray("effects").get(u).getAsString());
                                    continue;
                                }

                                NbtCompound nbtCompound = new NbtCompound();
                                nbtCompound.putString("Potion", jsonObject.getAsJsonArray("effects").get(u).getAsString());
                                for (int k = 0; k < potionItems.size(); k++) {
                                    ItemStack itemStack = new ItemStack(potionItems.get(k));
                                    itemStack.setNbt(nbtCompound);
                                    JobLists.brewerItemBrewingMap.put(itemStack, i);
                                }
                            }
                        }

                        if (jsonObject.getAsJsonArray("enchantments") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.brewerEnchantmentMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("enchantments").size(); u++) {

                                if (!Registry.ENCHANTMENT.containsId(new Identifier(jsonObject.getAsJsonArray("enchantments").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid enchantment identifier", jsonObject.getAsJsonArray("enchantments").get(u).getAsString());
                                    continue;
                                }

                                JobLists.brewerEnchantmentMap.put(Registry.ENCHANTMENT.get(new Identifier(jsonObject.getAsJsonArray("enchantments").get(u).getAsString())), i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        refreshReplaceList();
        // builder
        manager.findResources("builder", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("blocks") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.builderBlockIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("blocks").size(); u++) {

                                if (!Registry.BLOCK.containsId(new Identifier(jsonObject.getAsJsonArray("blocks").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid block identifier", jsonObject.getAsJsonArray("blocks").get(u).getAsString());
                                    continue;
                                }
                                JobLists.builderBlockIdMap.put(Registry.BLOCK.getRawId(Registry.BLOCK.get(new Identifier(jsonObject.getAsJsonArray("blocks").get(u).getAsString()))), i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        refreshReplaceList();
        // farmer
        manager.findResources("farmer", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("items") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.farmerItemIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("items").size(); u++) {

                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("items").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("items").get(u).getAsString());
                                    continue;
                                }

                                JobLists.farmerItemIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("items").get(u).getAsString()))), i);
                            }
                        }

                        if (jsonObject.getAsJsonArray("crafting") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.farmerCraftingIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("crafting").size(); u++) {
                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("crafting").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("crafting").get(u).getAsString());
                                    continue;
                                }

                                // Item item = Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("crafting").get(u).getAsString()));
                                // if (!item.isFood() && !item.getDefaultStack().isIn(TagInit.FARMER_CRAFTING_ITEMS)) {
                                // JobsAddonMain.LOGGER.warn("{} is not a valid food item", jsonObject.getAsJsonArray("crafting").get(u).getAsString());
                                // continue;
                                // }

                                JobLists.farmerCraftingIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("crafting").get(u).getAsString()))), i);
                            }
                        }

                        if (jsonObject.getAsJsonArray("smoker") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.farmerSmokerIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("smoker").size(); u++) {

                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("smoker").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("smoker").get(u).getAsString());
                                    continue;
                                }

                                JobLists.farmerSmokerIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("smoker").get(u).getAsString()))), i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        refreshReplaceList();
        // fisher
        manager.findResources("fisher", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("items") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.fisherItemIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("items").size(); u++) {

                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("items").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("items").get(u).getAsString());
                                    continue;
                                }

                                JobLists.fisherItemIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("items").get(u).getAsString()))), i);
                            }
                        }

                        if (jsonObject.getAsJsonArray("crafting") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.fisherCraftingIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("crafting").size(); u++) {

                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("crafting").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("crafting").get(u).getAsString());
                                    continue;
                                }

                                JobLists.fisherCraftingIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("crafting").get(u).getAsString()))), i);
                            }
                        }

                        if (jsonObject.getAsJsonArray("entities") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.fisherEntityIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("entities").size(); u++) {

                                if (!Registry.ENTITY_TYPE.containsId(new Identifier(jsonObject.getAsJsonArray("entities").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid entity identifier", jsonObject.getAsJsonArray("entities").get(u).getAsString());
                                    continue;
                                }

                                JobLists.fisherEntityIdMap.put(Registry.ENTITY_TYPE.getRawId(Registry.ENTITY_TYPE.get(new Identifier(jsonObject.getAsJsonArray("entities").get(u).getAsString()))), i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        refreshReplaceList();
        // lumberjack
        manager.findResources("lumberjack", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("blocks") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.lumberjackBlockIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("blocks").size(); u++) {

                                if (!Registry.BLOCK.containsId(new Identifier(jsonObject.getAsJsonArray("blocks").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid block identifier", jsonObject.getAsJsonArray("blocks").get(u).getAsString());
                                    continue;
                                }

                                JobLists.lumberjackBlockIdMap.put(Registry.BLOCK.getRawId(Registry.BLOCK.get(new Identifier(jsonObject.getAsJsonArray("blocks").get(u).getAsString()))), i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        refreshReplaceList();
        // miner
        manager.findResources("miner", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("blocks") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.minerBlockIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("blocks").size(); u++) {

                                if (!Registry.BLOCK.containsId(new Identifier(jsonObject.getAsJsonArray("blocks").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid block identifier", jsonObject.getAsJsonArray("blocks").get(u).getAsString());
                                    continue;
                                }

                                JobLists.minerBlockIdMap.put(Registry.BLOCK.getRawId(Registry.BLOCK.get(new Identifier(jsonObject.getAsJsonArray("blocks").get(u).getAsString()))), i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        refreshReplaceList();
        // smither
        manager.findResources("smither", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("items") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.smitherItemIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("items").size(); u++) {

                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("items").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("items").get(u).getAsString());
                                    continue;
                                }

                                JobLists.smitherItemIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("items").get(u).getAsString()))), i);
                            }
                        }

                        if (jsonObject.getAsJsonArray("crafting") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.smitherCraftingIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("crafting").size(); u++) {

                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("crafting").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("crafting").get(u).getAsString());
                                    continue;
                                }

                                JobLists.smitherCraftingIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("crafting").get(u).getAsString()))), i);
                            }
                        }

                        if (jsonObject.getAsJsonArray("blast_furnace") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.smitherBlastFurnaceIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("blast_furnace").size(); u++) {

                                if (!Registry.ITEM.containsId(new Identifier(jsonObject.getAsJsonArray("blast_furnace").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid item identifier", jsonObject.getAsJsonArray("blast_furnace").get(u).getAsString());
                                    continue;
                                }

                                JobLists.smitherBlastFurnaceIdMap.put(Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(jsonObject.getAsJsonArray("blast_furnace").get(u).getAsString()))), i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        refreshReplaceList();
        // warrior
        manager.findResources("warrior", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++) {
                    JsonElement jsonElement = data.get(String.valueOf(i));
                    if (jsonElement != null && jsonElement instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true);

                        if (jsonObject.getAsJsonArray("entities") != null) {

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                Iterator<Integer> iterator = JobLists.warriorEntityIdMap.values().iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().equals(i))
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("entities").size(); u++) {

                                if (!Registry.ENTITY_TYPE.containsId(new Identifier(jsonObject.getAsJsonArray("entities").get(u).getAsString()))) {
                                    JobsAddonMain.LOGGER.warn("{} is not a valid entity identifier", jsonObject.getAsJsonArray("entities").get(u).getAsString());
                                    continue;
                                }

                                JobLists.warriorEntityIdMap.put(Registry.ENTITY_TYPE.getRawId(Registry.ENTITY_TYPE.get(new Identifier(jsonObject.getAsJsonArray("entities").get(u).getAsString()))),
                                        i);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
        refreshReplaceList();
        // restricted
        manager.findResources("restricted", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                if (JsonHelper.getBoolean(data, "replace", false))
                    JobLists.restrictedRecipeIds.clear();

                if (data.getAsJsonArray("recipes") != null)
                    for (int u = 0; u < data.getAsJsonArray("recipes").size(); u++) {
                        // if (!Registry.RECIPE_TYPE.containsId(new Identifier(data.getAsJsonArray("recipes").get(u).getAsString()))) {
                        // JobsAddonMain.LOGGER.warn("{} is not a valid recipe identifier", data.getAsJsonArray("recipes").get(u).getAsString());
                        // continue;
                        // }
                        JobLists.restrictedRecipeIds.add(new Identifier(data.getAsJsonArray("recipes").get(u).getAsString()));
                    }
            } catch (Exception e) {
                JobsAddonMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

    private void refreshReplaceList() {
        replaceList.clear();
        for (int i = 0; i < ConfigInit.CONFIG.jobMaxLevel; i++)
            replaceList.put(i, false);
    }

}
