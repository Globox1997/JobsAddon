# JobsAddon

JobsAddon is a mod addon for the [LevelZ](https://www.curseforge.com/minecraft/mc-mods/levelz) mod and adds jobs.

### Installation

JobsAddon is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [LevelZ](https://www.curseforge.com/minecraft/mc-mods/levelz) to be installed separately; all other dependencies are installed with the mod.

### License
#### Source Code
The source code in this repository is licensed under the GPLv3 License.
See the LICENSE file for details.

#### Assets
All assets (including but not limited to images, audio, models, and other media)
are **All Rights Reserved** and may not be used, modified, or redistributed
without explicit permission from the copyright holder.

### Datapacks

Most of the mods default settings for jobs can get changed via datapacks, general settings can get changed via the config file.\
If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.fandom.com/wiki/Data_Pack) website and try to create your first one for the vanilla game.\
If you know how to create one, the folder path has to be ```data\jobsaddon\job\YOURFILE.json```\
Caution! Make sure you name the files differently than the existing ones.\

Since version 1.2.0, jobs can be created and overwritten via datapack.

#### Job
A job requires:
- id (integer): unique job id, jobs are sorted by it in the job gui - ids don't have to be continuous
- key: (string): determines the name of the job - translated like "text.jobsaddon.key" - job textures must be 14x14 at "assets/jobsaddon/textures/gui/key.png"
- maxlevel (integer): determines the max level of the job

Optional a job can have one or multiple of the following objects where experience is granted from:
- "blockbreak", "crafting", "enchanting", "brewing", "entitykill", "blockplace", "itemdrop", "smithing", "stonecutting", "breeding", "taming", "trading", "harvest"  

An integer determines the experience per completion.

- blockbreak: requires block id list
- crafting: requires item id list
- enchanting: requires enchantments object for example "minecraft:efficiency": 1
- brewing: requires effects list
- entitykill: requires entity id list
- blockplace: requires block id list
- itemdrop: requires item id list
- smithing: requires item id list of smithing table results
- stonecutting: requires item id list of stonecutter results
- breeding: requires entity id list, experience per bred pair
- taming: requires entity id list
- trading: requires item id list of bought items, experience per trade
- harvest: requires block id list of right click harvested blocks like sweet berry bushes and cave vines

Block, item, entity and potion lists can contain tags, for example `"#minecraft:logs"`.\

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

Sometimes specific recipes should not grant experience for crafting or smelting. A "restriction" object can get set to restrict certain recipes, inside a job file or at ```data\NAMESPACE\restricted\YOURFILE.json```. `"replace": true` clears the restrictions of lower datapacks.
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

### API

Other mods can use `net.jobsaddon.api`:
- `JobsAddonAPI`: read jobs, levels, experience and employment, add experience, set levels, employ and quit jobs, trigger built-in experience types (e.g. `triggerHarvest` for custom right click harvesting)
- `JobXpCallback.EVENT`: modify or cancel experience before it gets added
- `JobLevelUpCallback.EVENT`: called when a job reaches a new level
- `JobEmploymentEvents.ALLOW_EMPLOY` / `EMPLOYMENT_CHANGED`: deny employing a job or react to employment changes

```java
JobXpCallback.EVENT.register((player, job, type, experience) -> player.hasStatusEffect(StatusEffects.LUCK) ? experience * 2 : experience);
JobsAddonAPI.triggerHarvest(serverPlayer, blockState);
```

### Commands

All commands require permission level 2.

`/job <targets> add|remove|set level <job|all> <amount>`
- Change the job level, clamped between 0 and the max level

`/job <targets> add|remove|set xp <job|all> <amount>`
- Change the current job experience, adding experience can level up the job

`/job <targets> add|remove|set <job|all> <amount>`
- Old syntax, changes the job level

`/job <targets> get <job|all>`
- Print level, experience and employment

`/job <targets> employ <job>`
- Employ the job, ignores the job cooldown

`/job <targets> quit <job|all>`
- Quit the job

`/job <targets> reset <job|all>`
- Reset level and experience to 0

`/job <targets> cooldown <ticks>`
- Set the job change cooldown, 0 clears it
