# JobsAddon

JobsAddon is a mod addon for the [LevelZ](https://www.curseforge.com/minecraft/mc-mods/levelz) mod and adds jobs.

### Installation

JobsAddon is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [LevelZ](https://www.curseforge.com/minecraft/mc-mods/levelz) to be installed separately; all other dependencies are installed with the mod.

### License

JobsAddon is licensed under GPLv3.

### Datapacks

Most of the mods default settings for jobs can get changed via datapacks, general settings can get changed via the config file.\
If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.fandom.com/wiki/Data_Pack) website and try to create your first one for the vanilla game.\
If you know how to create one, the folder path has to be ```data\jobsaddon\job\YOURFILE.json```\
Caution! Make sure you name the files differently than the existing ones.\

Since version 1.2.0, jobs can be created and overwritten via datapack.

#### Job
A job requires:
- id (integer): determines the location in the job gui - starting at 0 -> 1,2,3,4
- key: (string): determines the name of the job - translated like "text.jobsaddon.key" - job textures must be 14x14 at "assets/jobsaddon/textures/gui/key.png"
- maxlevel (integer): determines the max level of the job

Optional a job can have one or multiple of the following objects where experience is granted from:
- "blockbreak", "crafting", "enchanting", "brewing", "entitykill", "blockplace", "itemdrop"  

An integer determines the experience per completion.

- blockbreak: requires block id list
- crafting: requires item id list
- enchanting: requires enchantments object for example "minecraft:efficiency": 1
- brewing: requires effects list
- entitykill: requires entity id list
- blockplace: requires block id list
- itemdrop: requires item id list

An example for the structure:
```json
{
  "lumberjack": {
    "replace": false,
    "id": 0,
    "key": "lumberjack",
    "maxlevel": 100,
    "blockbreak": {
      "2": {
        "replace": false,
        "blocks": [
          "minecraft:jungle_log"
        ]
      },
      "3": {
        "replace": false,
        "blocks": [
          "minecraft:birch_log",
          "minecraft:dirt"
        ]
      }
    }
  }
}
```

Sometimes specific recipes should not grant experience for crafting. A "restriction" object can get set to restrict certain recipes.
An example for the structure:
```json
{
  "restriction": {
    "replace": false,
    "recipes": [
      "minecraft:wheat"
    ]
  }
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
