# FoundryEngine

A NeoForge mod that transforms Minecraft into a game engine.
Mod ID `foundryengine`, package `de.luckymcdev.foundryengine`.

## Features

- **In-game editor** — ImGui-based editor with 18+ dockable panels for scene management, blueprints, cutscenes, areas, waypoints, code editing, file browsing, and more
- **Bundle system** — Self-contained mod packaging (scripts, assets, data packs, blueprints) — no Java compilation needed
- **Groovy scripting** — Full Groovy 5 runtime for bundle logic: register items, blocks, recipes, sounds, and particles from scripts
- **Blueprint visual scripting** — Node-based graph editor with execution flow, typed pins, and built-in event nodes
- **Cutscene system** — Bezier spline camera paths with per-node rotations, screen effects, server commands, and a full timeline editor
- **Instanced worlds** — Create temporary or persistent runtime dimensions with configurable chunk generators, clocks, and game rules
- **Game stages** — Progression framework gating items, mobs, dimensions, loot tables, and recipes behind player-specific stages
- **Custom rendering** — Mesh renderer with custom pipelines, OBJ model support, post-processing effects, and 20+ shaders
- **Areas & waypoints** — Spatial zone triggers with modular behavior system and persistent in-world markers
- **Custom particles** — Keyframe-driven particle system with sequenced color, scale, velocity, position, and rotation
- **Post-processing** — Priority-based shader effect system with GLSL pipeline support, fade transitions, and built-in effects
- **Game sessions** — Managed lifecycle system for game modes with persistent NBT data
- **Rich event system** — Over 50 events across 15 event classes for blocks, items, players, entities, levels, and more
- **Data generation** — Built-in bundle data generator for automated resource creation

## Quick start

```powershell
./gradlew.bat build                # full build
./gradlew.bat preCommit            # check + runData + build (commit pipeline)
./gradlew.bat runClient            # launch MC client
npm run docs:dev                   # VitePress docs (--host for network access)
```

## Documentation

Full documentation is available at [docs/](docs/) or run `npm run docs:dev` to serve locally.

- [Installation Guide](docs/guide/)
- [Getting Started](docs/guide/getting-started)
- [Bundle System](docs/concepts/core/bundles)
- [Builders API](docs/concepts/core/builders)
- [Events Reference](docs/concepts/core/events)
- [Examples](docs/examples/)
- [Commands Reference](docs/concepts/systems/commands)

## Architecture

```
FoundryEngine/
├── common/            Shared logic (cutscenes, easing, network, world, blueprints, bundles, events, builders)
├── client/            Client-only code (ImGui editor, rendering, particles, post-processing)
├── server/            Server-only code (commands, dynamic packs)
├── interfaces/        Mixin accessor interfaces for runtime property modification
├── mixin/             Mixin patches (50+) for rendering, world, input, UI, and more
├── config/            NeoForge config classes
├── src/
│   ├── main/java/     Java sources
│   └── generated/     Data generator output
└── docs/              VitePress documentation site
```

## Key paths

| Path                               | Purpose                              |
|------------------------------------|--------------------------------------|
| `runs/client`, `runs/server`       | Minecraft run directories            |
| `ExampleBundles/`                  | Groovy scripting bundles for testing |
| `FoundryEngine/bundles/` (in-game) | Bundle/script install location       |
| `docs/`                            | VitePress documentation site         |
| `repo/`                            | Local Maven publish target           |
