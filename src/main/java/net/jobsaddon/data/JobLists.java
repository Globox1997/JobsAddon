package net.jobsaddon.data;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class JobLists {

    // brewer
    public static final HashMap<ItemStack, Integer> brewerItemBrewingMap = new HashMap<>(); // brewing items like potions, no ingredients
    public static final HashMap<Enchantment, Integer> brewerEnchantmentMap = new HashMap<>(); // enchantments
    // builder
    public static final HashMap<Integer, Integer> builderBlockIdMap = new HashMap<>(); // blocks of all types, inside builder_placing_items tag
    // farmer
    public static final HashMap<Integer, Integer> farmerItemIdMap = new HashMap<>(); // block drops like wheat, inside farmer_breaking_items tag
    public static final HashMap<Integer, Integer> farmerCraftingIdMap = new HashMap<>(); // food items
    public static final HashMap<Integer, Integer> farmerSmokerIdMap = new HashMap<>();// output items which can be used inside the smoker (or inside farmer_furnace_items tag for furnace)
    // fisher
    public static final HashMap<Integer, Integer> fisherItemIdMap = new HashMap<>(); // catchable items by fishing
    public static final HashMap<Integer, Integer> fisherEntityIdMap = new HashMap<>(); // water creatures, mostly fish
    public static final HashMap<Integer, Integer> fisherCraftingIdMap = new HashMap<>(); // items like the fishing rod, inside fisher_crafting_items tag
    // lumberjack
    public static final HashMap<Integer, Integer> lumberjackBlockIdMap = new HashMap<>(); // blocks of all types, inside minecraft:logs tag
    // miner
    public static final HashMap<Integer, Integer> minerBlockIdMap = new HashMap<>(); // blocks of all types, inside c:ores tag
    // smither
    public static final HashMap<Integer, Integer> smitherItemIdMap = new HashMap<>(); // output items from anvil and smithing table
    public static final HashMap<Integer, Integer> smitherBlastFurnaceIdMap = new HashMap<>(); // output items which can be used inside the blast furnace (or inside smither_furnace_items tag for furnace)
    public static final HashMap<Integer, Integer> smitherCraftingIdMap = new HashMap<>(); // sword items, armor items, bow items, crossbow items, tool items (or inside smither_crafting_items tag)
    // warrior
    public static final HashMap<Integer, Integer> warriorEntityIdMap = new HashMap<>(); // all types of creatures

    // restricted
    public static final ArrayList<Identifier> restrictedRecipeIds = new ArrayList<Identifier>();
}
