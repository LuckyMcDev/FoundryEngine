# FoundryEngine — Agent Instructions

Before starting any work, list all available skills and decide which apply to this task. Skills marked **always-on** below are mandatory for every task; the rest are on-demand and trigger from their descriptions.

## Tool-Agnostic Note

These instructions are written once for any coding agent (GitHub Copilot, opencode, Cursor, Claude Code, ...). They are intentionally not tied to a single client. Where tooling is named, it refers to the IntelliJ IDEA MCP server; each agent maps it to its own integration.

## IntelliJ MCP — Mandatory Tool Layer (use for EVERYTHING)

This project is developed against an IntelliJ IDEA MCP server. It indexes decompiled Minecraft/NeoForge sources, provides IDE inspections, structural refactoring, run configurations, and builds. **ALWAYS use it for all code navigation, analysis, refactoring, verification, builds, and running.** This is the primary way to work in this repository.

### Strict tool ordering (MUST follow)

1. **Symbol Lookup** (`search_symbol`) — find classes, methods, fields by name. Pass `include_external: true` to search Minecraft/NeoForge/library symbols.
2. **Text Search** (`search_in_files_by_text` / `search_in_files_by_regex`) — find text occurrences across files.
3. **File Reading** (`read_file`) — read project files and sources inside jars.
4. **Symbol Info** (`get_symbol_info`) — quick documentation at a cursor position.
5. **File Discovery** (`find_files_by_glob` / `find_files_by_name_keyword`) — find files by pattern or name.
6. **Problems** (`get_file_problems`) — check for errors after edits.
7. **Build** (`build_project`) — compile/verify after changes.

### Fall back to filesystem tools ONLY if IntelliJ fails

- Regex search issues with complex patterns
- Cross-project searches across unrelated projects

**Banned shortcuts**: Do NOT use filesystem `grep`, `find`, `ls`, `read`, or similar for Java or dependency code before trying IntelliJ MCP tools first.

### Constraints (avoid common mistakes)

- **Always pass `projectPath`** where the tool requires it.
- Paths are relative to the project root; most tools only operate on project files.
- Line/column positions are **1-based**.
- **Batch reads** — read multiple related files in parallel.
- Use context from previous searches — remember what you found.
- Prefer `rename_refactoring` over plain text replace for renames.
- Use `reformat_file` for formatting.
- **Check problems** before committing changes; **build after** major edits.
- Use `maxLinesCount` + `truncateMode` for large outputs.

### Running things (no raw console)

Run and build through IntelliJ MCP, not a bare shell:

- **Build / tests** — `build_project`, or run the `FoundryEngine [build]` Gradle run configuration via `execute_run_configuration`.
- **Launch Minecraft** — `execute_run_configuration` with `Client`, `Server`, `ClientAndServer`, or `GameTestServer`.
- **Run data gen** — `execute_run_configuration` with `Data`.
- **Copy bundles** — `execute_run_configuration` with `FoundryEngine [copyExampleBundles]`.
- **Any other Gradle task** — `execute_terminal_command` (IDE-integrated terminal), never a standalone shell. Terminal commands and run configurations are high-risk; require explicit confirmation for destructive commands.

## Skills

Skills provide specialized instructions and workflows. They are loaded via the agent's skill tool; **always-on** skills below apply to every task, the rest trigger on demand from their descriptions.

### Always-on (apply to every task)

- **`clean-code`** — DRY/KISS/YAGNI, naming, function design, refactoring. Enforce on every change.
- **`deslop`** — remove AI-generated slop (unnecessary comments, defensive try/catch, type-escape casts, deep nesting).
- **`jetbrains-skill`** — JetBrains IDE MCP usage, constraints, and high-value patterns. Complements the IntelliJ MCP section above.

### On-demand (trigger by description)

- **`mixin-writing`** / **`mixinmcp-tools`** — mixin development and Minecraft/NeoForge source lookup.
- **`code-view`** — structured code comprehension before modifying unfamiliar code.
- **`java-debugging`** — stack traces and exception diagnosis.
- **`code-review`** / **`doc-review`** — review branches/docs.
- **`vitepress-docs`** — documentation site work.
- **`humanizer`** — humanize AI-written text.
- **`caveman`** — ultra-compressed communication when requested.
- **`goal`** / **`find-skills`** — autonomous goal tracking and skill discovery.

## Project

NeoForge Minecraft mod (`foundryengine`) that turns MC into a game engine.

- **Java**: 25
- **Build**: Gradle
- **NeoForge MDG**: `net.neoforged.moddev` v2.0.140
- **Package**: `de.luckymcdev.foundryengine` (single project)
- **Version**: `0.1.1` (alpha)

## Architecture

```
src/main/java/de/luckymcdev/foundryengine/
  ├── client/   — client-only (ImGui editor, rendering, particles, post-processing)
  ├── common/   — shared logic (events, builders, cutscenes, easing, network, blueprints, bundles, world)
  ├── server/   — server-only (commands, dynamic packs)
  ├── interfaces/ — mixin accessor interfaces for runtime property modification
  ├── mixin/    — mixin patches
  ├── config/   — NeoForge config (CommonConfig, Config, ClientConfig, ServerConfig, StartupConfig)
  ├── FoundryEngineMod.java     — common entrypoint
  ├── FoundryEngineModClient.java
  ├── FoundryEngineModServer.java
  └── package-info.java
```

### Key Design Patterns

**Singleton Managers** (`Common.java`):

- `Common.getBundleManager()`
- `Common.getGameStageHandler()`
- `Common.getNetworkManager()`
- `Common.getAreaManager()`
- `Common.getCutsceneManager()`
- `Common.getCutsceneSessionManager()`
- `Common.getSavedDataManager()`
- `Common.getWaypointManager()`
- `Common.getGameManager()`
- `Common.getDialogueManager()`

**Event Buses**:

- `NeoForge.EVENT_BUS` — system events
- `modBus` — mod-specific events (via `FoundryEngineMod.getModBus()`)

**Identifiers**:

- Namespaced: `Common.id("path")`
- Default namespace: `Common.mId("path")`

## Project Entry Points

| File                          | Purpose     | Event Bus         | Distribution     |
|-------------------------------|-------------|-------------------|------------------|
| `FoundryEngineMod.java`       | Common init | NeoForge + modBus | ALL              |
| `FoundryEngineModClient.java` | Client init | NeoForge + modBus | CLIENT           |
| `FoundryEngineModServer.java` | Server init | NeoForge + modBus | DEDICATED_SERVER |

## Code Conventions

**Annotations**:

- `@NullMarked` on all packages (generated by `generatePackageInfo`)
- `@ApiStatus.Internal` for internal-only classes (should not be used by mods)
- `@ApiStatus.Experimental` for experimental features

**Naming**:

- Classes: PascalCase
- Methods/fields: camelCase
- Constants: UPPER_SNAKE_CASE
- Identifiers: `Common.id("path")`

**Imports**:

- Use fully qualified names for imported classes
- Don't use long fully qualified names repeatedly
- Prefer importing over qualifying

**ImGui Panels**:

- Registered via `RegisterPanelEvent` (client only)
- Events: `ImGuiPreRenderEvent`, `ImGuiPostRenderEvent`

**No Java Linter/Formatter**:

- `check` is the main verification command
- Code style is enforced manually via formatting

## Common Workflows

### Adding a New Event

1. Create event class in `common/event/` or subdirectory
2. Add internal registration in `FoundryEngineMod.registerInternalEvents()`
3. Add handler in `FoundryEngineMod.registerNeoForgeEventHandlers()` or `registerModEventHandlers()`
4. Test with existing test infrastructure
5. Document in AGENTS.md (if public API)

### Adding a New Network Packet

1. Create packet class extending `AbstractPacket` (or from subdirectory)
2. Register packet definition in `onCommonSetup()`
3. Add sync methods in `onServerStarted()` / `onPlayerChangedDimension()`
4. Test with `test`
5. Verify packet bounds with `get_file_problems`

### Adding a New Bundle

1. Create bundle directory in `ExampleBundles/`
2. Add Groovy entrypoint script
3. Add `@StringDefine` annotations
4. Update `copyExampleBundles` if needed
5. Test in game with `runClient`
6. Generate data with `runData`

### Adding a New Manager

1. Create manager class in `common/` or appropriate package
2. Add static factory method to `Common.java`
3. Initialize in `FoundryEngineMod` constructor
4. Add lifecycle methods (load/save)
5. Register in appropriate events
6. Add tests in `src/test/java/`

## On-Demand Reference (read the file only when relevant)

| Topic                                                     | File                         |
|-----------------------------------------------------------|------------------------------|
| Troubleshooting (build/test/mixin/bundle/packet failures) | `.agents/troubleshooting.md` |
| Performance considerations                                | `.agents/performance.md`     |
| Security best practices                                   | `.agents/security.md`        |
| Bundles system & generated code                           | `.agents/bundles.md`         |
| Mixin development                                         | `.agents/mixin.md`           |
| Docs site & CI/CD                                         | `.agents/docs-cicd.md`       |
