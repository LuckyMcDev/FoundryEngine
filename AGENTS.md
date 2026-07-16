# FoundryEngine — Agent Instructions

Before starting any work, list all available skills and what they could be used for in this context.

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

## Navigation Protocol ⚠️ (MUST READ FIRST — strict ordering)

### 1. ALWAYS use IntelliJ MCP tools first (in this order):
1. **Symbol Lookup** (`intellij_search_symbol`) — find classes, methods, fields by name
2. **Text Search** (`intellij_search_in_files_by_text` / `intellij_search_in_files_by_regex`) — find occurrences
3. **File Reading** (`intellij_read_file`) — read file contents
4. **Symbol Info** (`intellij_get_symbol_info`) — get documentation at cursor
5. **File Discovery** (`intellij_find_files_by_glob` / `intellij_find_files_by_name_keyword`) — find files
6. **Problems** (`intellij_get_file_problems`) — check for errors after edits
7. **Build** (`intellij_build_project`) — compile/verify after edits

### 2. ONLY fall back to filesystem tools if IntelliJ fails:
- `intellij_search_in_files_by_regex` has issues with complex patterns
- Cross-project searches across unrelated projects

**Banned shortcuts**: Do NOT use `grep`, `find`, `ls`, `read`, or filesystem tools for Java code before trying IntelliJ tools first.

### 3. Best Practices
- **Batch reads** — read multiple related files in parallel
- **Ask users** for quick info instead of burning tool calls
- **Always pass `projectPath`** parameter where required
- **Use context** from previous searches — remember what you found
- **Check problems** before committing changes
- **Build after** major edits

## Project Entry Points

| File | Purpose | Event Bus | Distribution |
|------|---------|-----------|--------------|
| `FoundryEngineMod.java` | Common init | NeoForge + modBus | ALL |
| `FoundryEngineModClient.java` | Client init | NeoForge + modBus | CLIENT |
| `FoundryEngineModServer.java` | Server init | NeoForge + modBus | DEDICATED_SERVER |

## Key Commands

| Command | Purpose | Notes |
|---------|---------|-------|
| `./gradlew.bat build` | Full build | Check → runData → compile |
| `./gradlew.bat preCommit` | Pre-commit check | RunData → Build |
| `./gradlew.bat copyExampleBundles` | Sync bundles to run dirs | Runs in: client, server, gameTestServer |
| `./gradlew.bat runClient/runServer` | Launch MC | Interactive |
| `./gradlew.bat gameTestServer` | Game test server | Game tests |
| `./gradlew.bat test` | JUnit 5 tests | `src/test/java/` |
| `./gradlew.bat runData` | Generate resources | Outputs to `src/generated/resources/` |
| `./gradlew.bat clean` | Clean build | Remove `build/` and run dirs |
| `./gradlew.bat test --tests *Test` | Run specific tests | Filter by pattern |

## Generated Code

**BundleDataGenerator**:
- Runs in: `FMLConstructModEvent` + `commonSetup`
- Outputs to: `src/generated/resources/`
- Uses: `BundleConfig` specs from ExampleBundles
- Always run `runData` before committing (part of `preCommit`)

**Natural Language**:
- `runData` generates JSON from Groovy scripts in ExampleBundles
- Input: Groovy scripts with `@StringDefine` annotations
- Output: JSON files in `generated/resources/`

## Bundles System

**Structure**:
```
ExampleBundles/
  ├── scripts/           — Groovy entrypoints
  ├── assets/            — Assets to pack
  └── data/              — Data to pack
```

**Packing**:
- Bundles copied to `runs/client`, `runs/server`, `runs/gameTestServer`
- DynamicPackRepository registers bundles at runtime
- Pack order: user bundles → generated bundles

**Processing**:
1. Script discovery in `onConstruct`
2. Groovy execution with script engine hooks
3. Event callbacks: `onConstruct`, `onCommonSetup`, `onClientSetup`, `onDedicatedServerSetup`, `onPostInit`

## Dependencies (jarJar)

| Library | Purpose |
|---------|---------|
| ImGui | GUI rendering |
| Groovy | Script execution |
| CommonMark | Markdown parsing |
| RenderDoc | Debugging |
| JEI | In-game item info |
| Spark | Performance profiling |

## Testing

**Test Framework**: JUnit 5 + NeoForge test framework

**Test Locations**:
- Unit tests: `src/test/java/de/luckymcdev/foundryengine/common/`
- Game tests: Run `gameTestServer` run config

**Common Test Patterns**:
- Use `Common.get*()` static methods to access managers
- Test events with proper event bus registration
- Verify network packets with packet bounds
- Test bundles with BundleEvents

**Test Commands**:
```bash
./gradlew.bat test                    # All tests
./gradlew.bat test --tests BundleEventsTest  # Specific test class
./gradlew.bat test --tests "*PlayerEventsTest"  # Specific test
```

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

## Documentation

**VitePress**:
- Location: `docs/`
- Scripts: `npm run docs:dev` (dev) / `npm run docs:build` (prod)
- Generated via Qodana CI code analysis

**Code Documentation**:
- Use Javadoc for public APIs
- Internal classes can omit javadoc (ApiStatus.Internal)
- Keep docstrings concise and accurate

## CI/CD

**Qodana**:
- Configuration: `qodana.yaml`
- Runs on: CI pipeline
- Analyzes: Code quality, style, potential issues

**Code Analysis**:
- Disabled: Java linter/formatter
- Enabled: Qodana static analysis
- Focus: Logic errors, potential bugs, best practices

## Mixin Development

**Mixin Organization**:
- Target classes: Minecraft, NeoForge, other mods
- Mixin locations: `mixin/` directory
- Use `mixin-writing` skill for detailed guidance

**Mixin Tooling**:
- MixinMCP skills for Minecraft source indexing
- Use `intellij_search_symbol` to find mixins
- Always verify with `intellij_get_file_problems`

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
4. Test with `test` command
5. Verify packet bounds with `intellij_get_file_problems`

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

### Debugging Minecraft Issues

1. Use `mixinmcp-tools` skill for Minecraft source lookup
2. Add logging with `Common.LOGGER.error()` or `LOGGER.error()`
3. Test with `gameTestServer` for quick iteration
4. Use Spark profiling if needed
5. Check NeoForge event bus hooks

## Performance Considerations

**Event Handling**:
- Post events efficiently (avoid expensive operations in event handlers)
- Use `EventPriority.LOWEST` for late modifications
- Clear events with `Common.clearEvents()` after processing

**Network**:
- Use packet bounds to validate packets
- Sync data only when needed
- Consider batching for large packets

**Bundle Processing**:
- Bundles loaded in `onConstruct` — keep scripts fast
- Avoid heavy operations during `commonSetup`
- Use caching for expensive operations

**Rendering**:
- ImGui panels should not block main thread
- Offload heavy calculations to background threads
- Use frame limits for animations

## Security Best Practices

**Data Validation**:
- Validate all inputs from packets
- Sanitize bundle scripts
- Use packet bounds for network safety

**Access Control**:
- Use `@ApiStatus.Internal` for internal APIs
- Avoid exposing sensitive functionality in public API
- Validate permissions for server-side operations

**Resource Limits**:
- Limit bundle script execution time
- Cap packet sizes
- Prevent memory leaks in event handlers

## Troubleshooting Common Issues

**Build Fails**:
```bash
./gradlew.bat clean build  # Clean and rebuild
./gradlew.bat build --refresh-dependencies  # Refresh dependencies
```

**Tests Failing**:
```bash
./gradlew.bat test --debug  # Debug output
./gradlew.bat test --rerun-tasks  # Force re-run tests
```

**Mixin Not Working**:
- Use `mixin-writing` skill for detailed guidance
- Check target class visibility
- Verify `@Mixin` annotations
- Use MixinMCP to find correct target methods

**Bundle Not Loading**:
- Check bundle discovery in `onConstruct`
- Verify bundle path in `copyExampleBundles`
- Check bundle dependencies
- Enable debug logging for bundle loading

**Network Packet Not Receiving**:
- Verify packet registration in `onCommonSetup`
- Check packet bounds compatibility
- Verify sync in `onServerStarted()` / `onPlayerChangedDimension()`
- Check NeoForge network registration
