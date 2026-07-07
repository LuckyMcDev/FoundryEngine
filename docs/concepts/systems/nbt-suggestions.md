# NBT Command Suggestions

FoundryEngine adds smart tab-completion for NBT data in Minecraft commands. When typing NBT inside `/data`, `/summon`, `/setblock`, `/give`, `/item`, entity selectors, or any command that accepts NBT, the mod suggests valid field names, values, and structure based on the target type.

## Supported Contexts

| Context                                    | What Gets Suggestions                                   |
|--------------------------------------------|---------------------------------------------------------|
| `/data get/merge/modify block <pos>`       | Block entity fields for the block at that position      |
| `/data get/merge/modify entity <selector>` | Entity NBT fields matching the selector's entity type   |
| `/data get/merge/modify storage <id>`      | All known field types                                   |
| `/summon <entity> <pos> `                  | NBT for the summoned entity type                        |
| `/setblock <pos> <block> `                 | Block entity NBT for the placed block                   |
| `/give <player> <item> `                   | Item component NBT + data component IDs in `components` |
| `/item replace entity/block ...`           | Data component IDs in `components`                      |
| `@e[tag=, nbt=]` / `@p[tag=, nbt=]`        | NBT matching the selector's entity type                 |
| `{<field>: ` (after any `:`)               | Field values based on NBT type                          |

## How It Works

The system uses mixins to intercept Brigadier's suggestion engine at the points where Minecraft parses NBT arguments:

- **`CompoundTagArgument`** — top-level `{...}` in `/data`, `/summon`, etc.
- **`NbtTagArgument`** — raw NBT compounds in `/data merge`
- **`NbtPathArgument`** — NBT paths like `{Items[0].tag}`
- **`ComponentArgument`** — JSON text components `{"text":"hello"}`
- **`StyleArgument`** — text component style objects `{"bold":true}`
- **`BlockStateParser`** — block entity NBT in `/setblock`
- **`EntitySelectorParser`** — NBT within entity selectors
- **`ItemParser$State`** — data component IDs when typing `components:{...}` in `/give` or `/item`

The parser (`NbtSuggestionEngine`) reads the current cursor position, tracks brace/bracket depth and compound nesting, and determines whether you're typing a field name, value, list entry, or closing bracket — then provides context-appropriate suggestions.

Each suggestion carries a **subtext** (type hint shown on the right) and a **priority** via `SuggestionData`, a static map that stores `(subtext, priority)` per `Suggestion` object. The `SuggestionsListMixin` reads this data to render subtext, dim irrelevant suggestions, and sort by priority.

## Registered NBT Fields

### Block Entities

All vanilla block entities are registered with their known NBT fields. Modded block entity types are auto-detected from the registry and get a default set of common block entity fields.

Common block entity fields available on most supported blocks:

| Field        | Type   | Description                             |
|--------------|--------|-----------------------------------------|
| `CustomName` | String | JSON text component for the custom name |
| `Lock`       | String | Lock key for containers                 |

### Entities

Vanilla entities have per-type field registrations (e.g., `zombie` gets `IsBaby`, `DrownedConversionTime`; `villager` gets `VillagerData`, `Offers`, `Gossips`). Modded entity types are auto-detected and get common entity fields:

| Field          | Type    | Description         |
|----------------|---------|---------------------|
| `Pos`          | List    | `[x, y, z]`         |
| `Rotation`     | List    | `[yaw, pitch]`      |
| `Motion`       | List    | `[dx, dy, dz]`      |
| `OnGround`     | Boolean |                     |
| `CustomName`   | String  | JSON text component |
| `id`           | String  | Entity identifier   |
| `UUID`         | UUID    |                     |
| `Tags`         | List    | String list         |
| `Passengers`   | List    | Entity list         |
| `Invulnerable` | Boolean |                     |
| `Silent`       | Boolean |                     |
| `NoGravity`    | Boolean |                     |

### Items

| Context      | Fields                                                        |
|--------------|---------------------------------------------------------------|
| Generic item | `Count`, `id`, `components`, `tag`                            |
| Block item   | `BlockEntityTag`, `BlockStateTag`, `CanPlaceOn`, `CanDestroy` |
| Spawn egg    | `EntityTag`                                                   |

Item component names are derived dynamically from the `minecraft:data_component_type` registry and suggested inside `components:{...}` blocks.

## Subtype & Registry-Driven Value Suggestions

Instead of static enum maps, field definitions carry a `Subtype` that determines how values are suggested. When you type a value for a subtype-aware field, the engine queries the appropriate registry at runtime:

| NBT Field(s)                    | Subtype                                  | Source                                   |
|---------------------------------|------------------------------------------|------------------------------------------|
| `variant` (cat)                 | `CAT_VARIANT`                            | `Registries.CAT_VARIANT` (datapack)      |
| `variant` / `Motive` (painting) | `PAINTING_VARIANT`                       | `Registries.PAINTING_VARIANT` (datapack) |
| `VillagerData.type`             | `VILLAGER_TYPE`                          | `BuiltInRegistries.VILLAGER_TYPE`        |
| `VillagerData.profession`       | `VILLAGER_PROFESSION`                    | `BuiltInRegistries.VILLAGER_PROFESSION`  |
| `Color` / `color` (sign, bed)   | `DYE_COLOR`                              | `DyeColor` enum                          |
| `instrument` (note block)       | `INSTRUMENT`                             | `Registries.INSTRUMENT` (datapack)       |
| `rotation` / `mirror` / `mode`  | `ROTATION` / `MIRROR` / `STRUCTURE_MODE` | Vanilla enum                             |
| `note_block_sound` (skull)      | `SOUND_EVENT`                            | `BuiltInRegistries.SOUND_EVENT`          |
| `Type` (boat)                   | `WOOD_TYPE`                              | Static hardcoded list                    |

For dynamic datapack registries (`ENCHANTMENT`, `PAINTING_VARIANT`, `CAT_VARIANT`, `FROG_VARIANT`, `INSTRUMENT`), the engine first attempts a runtime lookup via `Level.registryAccess()`. If no world is loaded (e.g., title screen), it falls back to a built-in hardcoded list. This means suggestions always reflect the actual datapack content when connected to a world.

## Suggestion Prioritization & Display

The `SuggestionData` system assigns each suggestion a priority:

| Priority    | Value | Visual Effect                            |
|-------------|-------|------------------------------------------|
| Recommended | +1000 | Sorted to the top of the suggestion list |
| Normal      | 0     | Default sort order                       |
| Irrelevant  | -1    | Grayed out and sorted to the bottom      |

The `SuggestionsListMixin` hooks the rendering of the suggestion dropdown to:

- Sort suggestions by priority (descending), then alphabetically
- Dim (lower opacity) irrelevant suggestions
- Render subtext (type hints) right-justified in the suggestion box

Stale `SuggestionData` is cleared at the start of each `CommandSuggestions.updateCommandInfo()` call via `CommandSuggestionsMixin`.

## Item Data Component Suggestions

Item bracket syntax (`/give @s minecraft:diamond[unbreakable:...]`) is handled natively by Minecraft's `ItemParser.State`. The vanilla client already provides data component ID suggestions. The NBT suggestion system only handles NBT arguments (`{...}`) as used in `/data merge`, `/summon`, etc.

## JSON Text Component Suggestions

Inside JSON text components (`{"text":"..."}`), the system suggests all valid component keys (`text`, `translate`, `with`, `score`, `selector`, `keybind`, `nbt`, `extra`, `color`, `font`, `bold`, `italic`, etc.) and provides value suggestions:

- `color` — all valid ChatFormatting color names
- `font` — known font resource locations
- `bold`, `italic`, `underlined`, `strikethrough`, `obfuscated` — `true`
- `text` / `translate` / `insertion` — empty string template
- `selector` — `@p`

Style objects embedded in components (`"style": {...}`) get the same treatment independently.
