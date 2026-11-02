# Hearthvale - a Stardew-style village & romance mod (NeoForge 1.21.1)

Found a village, build rooms, and wanderers will arrive, settle in your houses, follow daily schedules,
befriend you, trigger heart events, and (if romanceable) date and marry you.
All NPCs, buildings, dialogue and events are **JSON data** - addons need no Java.

## Building the mod
1. Install JDK 21 and IntelliJ IDEA (or any Gradle-capable IDE).
2. Easiest: download the official **NeoForge MDK for 1.21.1**, then copy this project's `src/` folder over the MDK's
   `src/` (and use `gradle.properties` values from here). Or use this folder directly - it has a ModDevGradle `build.gradle`
   (run `gradle wrapper` once to generate the wrapper).
3. `./gradlew runClient` to test, `./gradlew build` for the jar in `build/libs`.

## Quick start (in game)
1. Craft/spawn a **Village Hearth** (stone bricks / campfire / bricks) and place it. That founds a village.
2. Build a closed room (walls + roof, doors, beds...). Stand inside and use a **Surveyor's Deed** (paper + ink sac).
   The mod flood-fills the room, counts the furniture and tells you what it detected - or exactly what's missing.
3. Every few minutes (config: `arrivalIntervalTicks`) a wanderer whose requirements are met walks in from the road to the hearth.
   **Right-click** them to welcome them; they move into a free home and start their schedule.
4. Talk to them daily, gift them things, and walk up to them to trigger heart events.

### Bundled building types
| Type | Needs (inside or touching the room) |
|---|---|
| Home | 1 bed + 1 door (sleeps = number of beds) |
| Library | 6 bookshelves + lectern + door |
| Blacksmith | anvil + furnace/blast furnace + smithing table/grindstone + door |
| Tavern | 2 barrels + campfire/smoker/cauldron + door |
| Apothecary | brewing stand + cauldron + 2 flower pots + door |
| General Store | 3 chests + sign + door |

### Bundled NPCs
Elara Voss (librarian, romanceable, 6 events), Garrick Ironhand (blacksmith, romanceable, 6 events),
Bram Hollis (tavern keeper, 4 events), Marta Quillfeather (apothecary, 4 events). They arrive once their building + a free home exist.
Skins are placeholders - drop real 64x64 skins over `assets/hearthvale/textures/entity/npc/*.png`.

### Controls
| Action | How |
|---|---|
| Talk (+20 points once/day) | right-click |
| Gift (max 1/day, 2/week) | **sneak** + right-click holding the item |
| Hug/kiss (dating+, +10/day) | sneak + right-click, empty hand |
| Ask to date (needs 8 hearts) | right-click holding a **Bouquet** (3 small flowers + string) |
| Propose (dating, 10 hearts) | right-click holding a **Promise Pendant** (4 gold + diamond) -> wedding in 3 days |
| Social overview | **Village Journal** (book + red dye) or `/hearthvale social` |

Friendship: 250 points = 1 heart. Romanceable NPCs stop at 8 hearts until you date them (10), marriage allows 14.
Loved +80, liked +45, neutral +20, disliked -20, hated -40; birthdays x8 for loved/liked.
The calendar is Stardew-like: day 0 = Monday, 7-day weeks, 28-day seasons.
After the wedding the spouse moves into the home containing your bed (set your spawn there) and uses their `spouse_schedule`.

### Commands
`/hearthvale social | village | list` for everyone.
Op-only (testing): `hearts <npc> <points>`, `status <npc> <friend|dating|engaged|married>`, `arrive <npc>`, `event <npc> <event_id>`.

### Config (`hearthvale-common.toml`)
`arrivalIntervalTicks`, `maxBuildingVolume`, `maxBuildingRadius`, `villageRadius`, `minVillageDistance`.

## Adding your own NPCs / buildings (no Java)
See **docs/ADDON_GUIDE.md** and the working sample in **example_addon/** (a datapack for the JSON + a resource pack for the skin).
