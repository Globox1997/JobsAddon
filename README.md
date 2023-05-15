# JobsAddon

JobsAddon is a mod addon for the [LevelZ](https://www.curseforge.com/minecraft/mc-mods/levelz) mod and adds jobs.

### Installation

JobsAddon is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [LevelZ](https://www.curseforge.com/minecraft/mc-mods/levelz) to be installed separately; all other dependencies are installed with the mod.

### License

JobsAddon is licensed under GPLv3.

### Datapacks

Most of the mods default settings for jobs can get changed via datapacks, general settings can get changed via the config file.\
If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.fandom.com/wiki/Data_Pack) website and try to create your first one for the vanilla game.\
If you know how to create one, the folder path has to be ```data\jobsaddon\FOLDER\YOURFILE.json```\
Caution! Make sure you name the files differently than the existing ones.\

The mod has 8 different folder for different jobs.

1. brewer
2. builder
3. farmer
4. fisher
5. lumberjack
6. miner
7. smither
8. warrior

The structure of each file inside the folders is mostly the same but have a few differences.
An example for the structure:
```
{
    "5": {
        "replace": false,
        "items": [
            "minecraft:iron_sword"
        ]
    }
}
```
The defined variable 5 in this example will be the given experience for each action the player does within this job and for this specific action.
Set "replace" to true if you want to overwrite the default datapack, given by this mod.
In this example the "items" list has effects depending on in which folder (in which job) it is. Within the smither folder it would stand for item outputs from the anvil or smithing table, within the fisher folder it would stand for catchable fish items.

#### 1. Brewer

The brewer can define "effects" for potion brewing at the brewing table and "enchantments" for enchanting items at the enchantment table.

```json
{
    "5": {
        "replace": false,
        "effects": [
            "minecraft:regeneration",
            "minecraft:speed",
            "minecraft:water_breathing"
        ]
    },
    "10": {
        "replace": false,
        "effects": [
            "minecraft:fire_resistance",
            "minecraft:poison"
        ],
        "enchantments": [
            "minecraft:sharpness",
            "minecraft:fire_aspect"
        ]
    }
}
```

#### 2. Builder

The builder can define "blocks" for placing blocks on the ground.

```json
{
    "5": {
        "replace": false,
        "blocks": [
            "minecraft:grass_block",
            "minecraft:dirt"
        ]
    },
    "10": {
        "replace": false,
        "blocks": [
            "minecraft:sand"
        ]
    }
}
```

#### 3. Farmer

The farmer can define "items" for block drops like wheat and "crafting" for craftable items including outputs from the smoker.

```json
{
    "5": {
        "replace": false,
        "items": [
            "minecraft:wheat"
        ],
        "crafting": [
            "minecraft:cake",
            "minecraft:baked_potato"
        ]
    }
}
```

#### 4. Fisher

The fisher can define "items" for item drops while fishing, "entities" for killing entities and "crafting" for craftable items.

```json
{
    "5": {
        "replace": false,
        "items": [
            "minecraft:raw_cod"
        ],
        "entities": [
            "minecraft:cod"
        ],
        "crafting": [
            "minecraft:fishing_rod"
        ]
    }
}
```

#### 5. Lumberjack

The lumberjack can define "blocks" for breaking these blocks.

```json
{
    "5": {
        "replace": false,
        "blocks": [
            "minecraft:birch_log"
        ]
    }
}
```

#### 6. Miner

The miner can define "blocks" for breaking these blocks.

```json
{
    "5": {
        "replace": false,
        "blocks": [
            "minecraft:coal_ore"
        ]
    }
}
```

#### 7. Smither

The smither can define "items" for craftable items including other furnace outputs.

```json
{
    "5": {
        "replace": false,
        "items": [
            "minecraft:netherite_sword",
            "minecraft:iron_ingot"
        ]
    }
}
```

#### 8. Warrior

The warrior can define "entities" for killing them.

```json
{
    "5": {
        "replace": false,
        "entities": [
            "minecraft:zombie"
        ]
    }
}
```

#### Tags
For a few situations there are a couple of tags which provide some usage:
- fisher_crafting_items: items which can be crafted used for fisher experience
- farmer_crafting_items: items which can be crafted and are not a food item for farmer experience
- farmer_breaking_items: items which are a drop from plant blocks and their instances for farmer experience
- builder_placing_blocks: blocks which the builder can place for experience
- miner_breaking_blocks: blocks which the miner can break for experience

#### Restriction (Since version 1.0.6)
It is possible to restrict recipes by their id for granting jobsaddon experience.
The file has to be in the folder called "resticted" and an example can be found below:

```json
{
    "replace": false,
    "recipes": [
        "minecraft:golden_sword"
    ]
}
```

### Advancement

JobsAddon provides an advancement criterion trigger called "jobsaddon:job_up".\
It triggers when the player reached the set job level.

```json
    "criteria": {
        "miner_level_100": {
            "trigger": "jobsaddon:job_up",
            "conditions": {
                "job_name": "miner",
                "job_level": 100
            }
        }
    }
```

### Commands

`/jobmanager playername add jobname integer`
- Increase the specific job by the integer value
  
`/jobmanager playername remove jobname integer`
- Decrease the specific job by the integer value

`/jobmanager playername set jobname integer`
- Set the specific job to the integer value

`/jobmanager playername get jobname`
- Print the specific job level
