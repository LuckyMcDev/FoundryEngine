# NBT Command Suggestions

FoundryEngine adds tab-completion for NBT data in Minecraft commands. When you type NBT inside commands like `/data`, `/summon`, `/give`, or entity selectors, the mod suggests valid field names and values.

## Where it works

| Command                                    | What gets suggestions       |
|--------------------------------------------|-----------------------------|
| `/data get/merge/modify block <pos>`       | Block entity fields         |
| `/data get/merge/modify entity <selector>` | Entity NBT fields           |
| `/summon <entity> <pos>`                   | NBT for the summoned entity |
| `/setblock <pos> <block>`                  | Block entity NBT            |
| `/give <player> <item>`                    | Item component IDs          |
| Entity selectors `@e[nbt=]`                | Matching entity NBT         |

## How it works

The system reads the current cursor position, tracks brace/bracket depth, and suggests context-appropriate fields. Each suggestion shows a type hint and is sorted by relevance.

## Item data component suggestions

When typing `components:{...}` in `/give` or `/item`, the mod suggests valid data component IDs from the game's registry.

## JSON text component suggestions

Inside JSON text components (`{"text":"..."}`), the mod suggests valid keys like `text`, `translate`, `color`, `bold`, `italic`, etc., with appropriate value suggestions for each.

## See also

- [Commands](commands.md) — full command reference
