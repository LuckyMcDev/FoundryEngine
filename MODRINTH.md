# Foundry Engine

Foundry Engine is a Minecraft mod that extends the game with a comprehensive in-game editor suite, custom rendering, and a self-contained mod packaging system — transforming Minecraft into a game development environment. Built on NeoForge 26.1.x.x for Minecraft 26.1.x.

## Core Features

- **In-Game Editor** — A Dear ImGui-based editor overlay with 18+ dockable panels for file management, code editing, texture viewing, cutscene editing, area management, and more. Access everything without leaving the game. Includes 7 built-in UI themes.

- **Groovy Scripting Engine** — Full Groovy 5 runtime for bundle logic. Register custom items, blocks, recipes (all 9 types), sounds, and particles directly from scripts. Three-sided execution (common/client/server) with per-side entrypoints.

- **Bundle System** — A self-contained mod packaging format that lets you define items, blocks, recipes, sounds, models, textures, language files, and scripts in a single distributable folder — no separate mod project or Java compilation required. Bundles are discovered automatically, loaded dynamically, support dependency management, and can include their own resource and data packs.

## Additional Features

- **Instanced Worlds** — On-the-fly world creation with custom chunk generators (void, transient), clock controls, game rules, per-dimension difficulty, and temporary or persistent storage.

- **Cutscene Editor** — Timeline-based cinematic tooling with cubic Bezier camera paths, per-node anchor rotations, screen effects (black, circle, star, cinematic bars), and keyframe command triggers. Full in-world editing with draggable handles.

- **Game Stages** — A progression framework that gates recipes, items, mob spawns, loot tables, and dimensions behind player-specific named stages. Supports deferred stage addition, cancellable stage events, and addon systems for content gating.

- **Areas** — Spatial zone triggers with two shapes (AABB, Block) and a modular behavior system. Attach enter/leave/tick/render/block modules to zones, link areas together, create presets with pre-configured modules. Debug visualization in-world.

- **Custom Particles** — Keyframe-driven particle system with sequenced control over color, scale, position, velocity, and rotation over the particle lifetime using 31 easing functions.

- **Post-Processing** — Priority-based shader effect system with GLSL pipeline support. Includes built-in effects (grayscale, sepia, black, circle, star, cinematic bars) and supports custom GLSL shaders with three render phases (POST_WORLD, PRE_GUI, POST_RENDER), fade transitions, and dynamic uniforms.

- **Mesh Rendering & OBJ** — Custom 3D rendering engine with 6 render pipelines (position, color, lit models, textured, debug lines, through-walls). Built-in OBJ model loader and renderer with the Suzanne monkey head pre-loaded. Full vertex/normal/UV support and fan triangulation.

- **Game Sessions** — Managed lifecycle system for game modes with STARTING/RUNNING/STOPPING/STOPPED state machine, persistent NBT data storage, and bundle-scoped session management with automatic cleanup on reload.

- **Waypoints** — Persistent, colored in-world markers synced across clients with icon support, ARGB color control, and full command management.

- **Rich Event System** — Over 50 events across 15 event classes covering blocks, items, players, entities, levels, network, client, server, commands, recipes, areas, game sessions, stages, slots, and bundles. Usable from Groovy scripts, Blueprints, or Java addons.

- **Public Java Addon API** — Events and builders for Java developers to hook into the engine. Register custom editor panels, modify block/item properties at runtime, add data providers, listen to stage events, customize the title screen, and register custom key bindings.

- **In-Game Markdown Rendering** — Display GitHub-Flavored Markdown as formatted Minecraft Component text for in-game documentation, tutorials, and help screens. Supports headings, bold, italic, code blocks, tables, links, lists, and more.

- **Network System** — 18+ packet types for editor synchronization, file transfer, world state, and bundle data — all using modern codec-based serialization.


