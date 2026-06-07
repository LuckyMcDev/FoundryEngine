# FoundryEngine

A NeoForge mod that transforms Minecraft into a game engine. 
Mod ID `foundryengine`, package `de.luckymcdev.foundryengine`.

## Features

- **In-game editor** — ImGui-based editor with panels for scene management, blueprints, cutscenes, areas, waypoints, and more
- **Cutscene system** — Bezier spline camera paths with per-node rotations, screen effects, server commands, and a full timeline editor
- **Blueprints** — Groovy-based scripting system for runtime behavior and content
- **Dynamic levels** — Create temporary or persistent runtime dimensions with configurable generators
- **Custom rendering** — Mesh renderer with custom pipelines, OBJ model support, post-processing effects
- **Waypoints & areas** — Per-dimension markers and bounded regions with persistence
- **Bundles** — Packaged content system (scripts, assets, configs) deployed at runtime
- **Data generation** — Built-in data generator for automated resource creation

## Quick start

```powershell
./gradlew.bat build                # full build
./gradlew.bat preCommit            # check + runData + build (commit pipeline)
./gradlew.bat runClient            # launch MC client
npm run docs:dev                   # VitePress docs (--host for network access)
```

## Architecture

```
FoundryEngine/
├── api/               Public contracts (events, builders)
├── common/            Shared logic (cutscenes, easing, network, world, blueprints, bundles)
├── client/            Client-only code (ImGui editor, rendering, particles, post-processing)
├── server/            Server-only code (commands, dynamic packs)
└── src/
    ├── main/java/     Java sources
    └── generated/resources/  Data generator output
```

## Key paths

| Path                               | Purpose                              |
|------------------------------------|--------------------------------------|
| `runs/client`, `runs/server`       | Minecraft run directories            |
| `ExampleBundles/`                  | Groovy scripting bundles for testing |
| `FoundryEngine/bundles/` (in-game) | Bundle/script install location       |
| `docs/`                            | VitePress documentation site         |
| `repo/`                            | Local Maven publish target           |