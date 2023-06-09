package net.jobsaddon.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagInit {

    // Item
    public static final TagKey<Item> FISHER_CRAFTING_ITEMS = TagKey.of(RegistryKeys.ITEM, new Identifier("jobsaddon", "fisher_crafting_items"));
    public static final TagKey<Item> FARMER_CRAFTING_ITEMS = TagKey.of(RegistryKeys.ITEM, new Identifier("jobsaddon", "farmer_crafting_items"));

    public static final TagKey<Item> FARMER_BREAKING_ITEMS = TagKey.of(RegistryKeys.ITEM, new Identifier("jobsaddon", "farmer_breaking_items"));

    // Block
    public static final TagKey<Block> BUILDER_PLACING_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("jobsaddon", "builder_placing_blocks"));
    public static final TagKey<Block> MINER_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("jobsaddon", "miner_breaking_blocks"));

    public static void init() {
    }
}
