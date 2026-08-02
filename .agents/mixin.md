# Agent Reference — Mixin Development

Read this file when writing or debugging mixins.

## Mixin Organization

- Target classes: Minecraft, NeoForge, other mods
- Mixin locations: `mixin/` directory
- Use `mixin-writing` skill for detailed guidance

## Mixin Tooling

- Use MixinMCP tools (`mixin_*`) for Minecraft source indexing — never `grep`/`read` on jars
- Use `search_symbol` with `include_external: true` to find mixin targets
- Always verify with `get_file_problems`

## Workflow

1. **Search target** — `search_symbol` with `include_external: true`, or `mixin_find_class`
2. **Verify signature** — `get_symbol_info` on the target method
3. **Read source** — `read_file` on the decompiled class
4. **Write mixin** — use correct mappings (official/intermediate)
5. **Verify** — `get_file_problems` on your mixin file
