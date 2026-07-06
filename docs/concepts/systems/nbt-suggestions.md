# NBT Command Suggestions

FoundryEngine adds smart tab-completion for NBT data in Minecraft commands. When typing NBT inside `/data`, `/summon`, `/setblock`, `/give`, `/item`, entity selectors, or any command that accepts NBT, the mod suggests valid field names, values, and structure based on the target type.

## Supported Contexts

| Context                                    | What Gets Suggestions                                 |
|--------------------------------------------|-------------------------------------------------------|
| `/data get/merge/modify block <pos>`       | Block entity fields for the block at that position    |
| `/data get/merge/modify entity <selector>` | Entity NBT fields matching the selector's entity type |
| `/data get/merge/modify storage <id>`      | All known field types                                 |
| `/summon <entity> <pos> `                  | NBT for the summoned entity type                      |
| `/setblock <pos> <block> `                 | Block entity NBT for the placed block                 |
| `/give <player> <item> `                   | Item component NBT                                    |
| `/item replace entity/block ...`           | Component NBT based on target                         |
| `@e[tag=, nbt=]` / `@p[tag=, nbt=]`        | NBT matching the selector's entity type               |
| `{<field>: ` (after any `:`)               | Field values based on NBT type                        |

## How It Works

The system uses mixins to intercept Brigadier's suggestion engine at the points where Minecraft parses NBT arguments:

- **`CompoundTagArgument`** — top-level `{...}` in `/data`, `/summon`, etc.
- **`NbtTagArgument`** — raw NBT compounds in `/data merge`
- **`NbtPathArgument`** — NBT paths like `{Items[0].tag}`
- **`ComponentArgument`** — JSON text components `{"text":"hello"}`
- **`StyleArgument`** — text component style objects `{"bold":true}`
- **`BlockStateParser`** — block entity NBT in `/setblock`
- **`EntitySelectorParser`** — NBT within entity selectors

The parser (`NbtSuggestionEngine`) reads the current cursor position, tracks brace/bracket depth and compound nesting, and determines whether you're typing a field name, value, list entry, or closing bracket — then provides context-appropriate suggestions.

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

Item component names are derived dynamically from the `minecraft:data_component_type` registry.

## Enum Value Suggestions

Where NBT fields expect specific string values (e.g., `instrument`, `color`, `rotation`), the system suggests valid options derived from Minecraft's own enums:

| NBT Enum Key                   | Source                            |
|--------------------------------|-----------------------------------|
| `DyeColor` / `color` / `Color` | `DyeColor` enum values            |
| `instrument`                   | `NoteBlockInstrument` enum values |
| `rotation`                     | `Rotation` enum names             |
| `mirror`                       | `Mirror` enum names               |
| `mode`                         | `StructureMode` enum names        |

Registry-driven enums (`CatVariant`, `FrogVariant`, `PaintingVariant`) and other constants are provided from static lists.

## JSON Text Component Suggestions

Inside JSON text components (`{"text":"..."}`), the system suggests all valid component keys (`text`, `translate`, `with`, `score`, `selector`, `keybind`, `nbt`, `extra`, `color`, `font`, `bold`, `italic`, etc.) and provides value suggestions:

- `color` — all valid ChatFormatting color names
- `font` — known font resource locations
- `bold`, `italic`, `underlined`, `strikethrough`, `obfuscated` — `true`
- `text` / `translate` / `insertion` — empty string template
- `selector` — `@p`

Style objects embedded in components (`"style": {...}`) get the same treatment independently.
