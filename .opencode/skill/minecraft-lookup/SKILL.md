---
name: minecraft-lookup
description: Use MixinMCP tools to search Minecraft, NeoForge, and dependency sources. Essential for finding mixin targets, obfuscated mappings, and vanilla behavior. Always use before writing mixins or debugging MC internals.
---

# Minecraft Source Lookup Skill

## Purpose
Leverage MixinMCP IntelliJ tooling to search and read Minecraft, NeoForge, and dependency sources. These tools index inside jars that grep cannot see and return structured results using less context than raw file dumps.

## When to Use
- Writing mixins (finding correct target classes/methods)
- Debugging vanilla behavior
- Finding obfuscated/intermediate mappings
- Understanding Minecraft internals
- Diagnosing "target not found" mixin errors
- Looking up NeoForge API usage in dependencies

## Available Tools (via intellij MCP)

### Search Minecraft/Mod Sources
```json
{ "q": "ChunkMap", "include_external": true, "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### Find Mixin Targets
```json
{ "q": "resizeGui", "include_external": true, "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### Read Decompiled Sources
```json
{ "file_path": "jar://net.minecraft.client.Minecraft!/net/minecraft/client/Minecraft.class", "projectPath": "C:/Data/Projects/FoundryEngine" }
```

## Workflow for Mixin Development
1. **Search target** — `intellij_search_symbol` with `include_external: true`
2. **Verify signature** — `intellij_get_symbol_info` on the target method
3. **Read source** — `intellij_read_file` on the decompiled class
4. **Write mixin** — Use correct mappings (official/intermediate)
5. **Verify** — `intellij_get_file_problems` on your mixin file

## Common Targets
| Need | Search For |
|------|------------|
| Player interaction | `PlayerInteractEvent`, `ItemInteractionForEntity` |
| Block breaking | `BlockBreakEvent`, `BreakBlock` |
| Rendering | `LevelRenderer`, `GameRenderer` |
| Networking | `PacketListener`, `ServerGamePacketListener` |
| World gen | `ChunkGenerator`, `BiomeSource` |

## Key Patterns
- Use `include_external: true` to search SDK/library symbols
- MixinMCP handles Mojang → official mapping translation
- For `@At` strings, copy exact bytecode patterns from decompiled source
- Lambdas appear as `lambda$methodName$0` — search for these