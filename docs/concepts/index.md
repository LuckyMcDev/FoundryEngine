# Concepts

Welcome to the FoundryEngine concepts documentation. These articles cover everything you need to know to build with FoundryEngine.

## Core Concepts

| Article                        | Description                                                              |
|--------------------------------|--------------------------------------------------------------------------|
| [Bundles](bundles)             | The fundamental packaging unit — scripts, assets, and data in one folder |
| [Builders](builders)           | Fluent API for creating items, blocks, recipes, sounds, and particles    |
| [Registries](registries)       | How custom content is registered with the game                           |
| [Scripts](scripts)             | Groovy scripting: entrypoints, helpers, and execution model              |
| [Entrypoints](entrypoint)      | The bridge between the engine and your Groovy code                       |
| [Events](events)               | Full event system reference — over 50 events across 12 categories        |
| [Blueprints](blueprints)       | Node-based visual scripting with `.febp` files                           |
| [Dependencies](dependencies)   | Declaring mod and bundle dependencies                                    |
| [Sides](sides)                 | Client/server separation and what goes where                             |
| [Bundle Config](config)        | Per-bundle TOML configuration with typed values                          |
| [Easing Functions](easing)     | Robert Penner's easing functions for animations                          |
| [Markdown Rendering](markdown) | Displaying GitHub-Flavored Markdown in-game                              |

## Systems

| Article                              | Description                                                           |
|--------------------------------------|-----------------------------------------------------------------------|
| [In-Game Editor](editor)             | Dear ImGui dockable panels for cutscenes, areas, blueprints, and more |
| [Cutscene System](cutscenes)         | Bezier camera paths, screen effects, timeline commands                |
| [Instanced Worlds](instanced-worlds) | Runtime dimensions with custom generators, clocks, and rules          |
| [Game Stages](stages)                | Progression framework: gate items, mobs, dimensions, and loot         |
| [Areas](areas)                       | AABB spatial zones with enter/leave/tick events                       |
| [Waypoints](waypoints)               | Persistent colored in-world markers                                   |
| [Custom Particles](particles)        | Keyframe-driven particle animation system                             |
| [Post-Processing](post-processing)   | Shader effects: bloom, blur, grayscale, and more                      |

## Reference

| Article              | Description                          |
|----------------------|--------------------------------------|
| [Commands](commands) | Complete `/engine` command reference |
