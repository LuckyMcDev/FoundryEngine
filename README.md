# FoundryEngine

A NeoForge Minecraft mod that turns MC into a game engine. Mod ID `foundryengine`, package `de.luckymcdev.foundryengine`.

## Quick start

```powershell
./gradlew.bat build          # full build (no special steps needed)
./gradlew.bat test           # unit tests only
./gradlew.bat preCommit      # check + runData + build (commit pipeline)
./gradlew.bat runClient      # launch MC client with mod
./gradlew.bat runServer      # launch dedicated server
./gradlew.bat runData        # data generation → src/generated/resources/
./gradlew.bat copyExampleBundles  # copy ExampleBundles/ into run dirs
npm run docs:dev             # VitePress docs dev server (--host)
```

On Unix replace `./gradlew.bat` with `./gradlew`.

## Architecture

- **`api/`** — public contracts (`BlockEvents`, `ItemEvents`, `PlayerEvents`, builders)
- **`common/`** — shared logic (blueprints, bundles, cutscenes, easing, network, virtual packs, world)
- **`client/`** — client-only (ImGui editor, rendering, particles, post-processing)
- **`server/`** — server-only (commands, dynamic packs)
- Entrypoints: `FoundryEngineMod` (hybrid, `@Mod(Common.MODID)`) + `FoundryEngineModServer` (dedicated server, `dist=Dist.DEDICATED_SERVER`)
- 11 mixin configs under `src/main/resources/mixins/` (screen, level, command, clock, input, sound, render, data, invoker, entity, core)

## Key paths

| Path                               | Purpose                                            |
|------------------------------------|----------------------------------------------------|
| `runs/client`, `runs/server`       | Minecraft run directories                          |
| `ExampleBundles/`                  | Groovy scripting bundles for testing               |
| `src/main/java/`                   | Java Source Directory                              |
| `src/generated/resources/`         | Data generation output (run `runData` to populate) |
| `FoundryEngine/bundles/` (in-game) | Bundle/script install location                     |
| `docs/`                            | VitePress documentation                            |
| `repo/`                            | Local Maven publish target                         |

## Testing

- JUnit 5, pure logic tests (no MC bootstrap needed)
- No mocking frameworks
- `@BeforeEach` for setup, `@ParameterizedTest @CsvSource` for parametrized tests
- Static import `Assertions.*`, `0.001f` delta for float comparisons
- `gradle.properties` has `useJUnitPlatform()` on the test task
- Unit test support enabled via `neoForge { unitTest { enable() } }`

## Style

- `@ApiStatus.Internal` for internal APIs, `@Nullable` from JSpecify
- `neoforge.mods.toml` uses Gradle property substitution (`${mod_version}` etc.)
- All version pins live in `gradle.properties`
- Every package should have a `package-info.java` with the `org.jspecify.annotations.NullMarked` annotation

## CI/CD

- Build: `.github/workflows/build.yaml` — runs on master push to `src/**`
- Docs deploy: `.github/workflows/deploy.yml` — builds VitePress + Javadoc → GH Pages
- Publish: `.github/workflows/publish.yaml` — on release or manual dispatch (GitHub Packages)


---

Icon by game-icons.net

- Vsync breaks when opening new window?
- Imgui does double the monitors refresh rate of fps?

- When toggling off imgui, external windows still get rendered but not ticked / input handled.

- UI scale is broken when using any sort of scaling?
  Fix?: scale imgui with mc gui scale

- So font is somehow sometimes broken on Windows aswell (my machine)