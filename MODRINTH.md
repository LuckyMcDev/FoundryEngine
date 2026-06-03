# Foundry Engine

Foundry Engine is a Minecraft mod that extends the game with a comprehensive in-game editor suite, visual scripting, and a self-contained mod packaging system — transforming Minecraft into a game development environment. Built on NeoForge 26.1.2.

## Core Features

- **In-Game Editor** — A Dear ImGui-based editor overlay with dockable panels for file management, code editing, texture viewing, asset browsing, and more. Access everything without leaving the game.
- **Blueprint Visual Scripting** — A node-based graph editor for designing game logic through execution flow, events, and variables. Create interactive behaviors without writing Java code.
- **Bundle System** — A self-contained mod packaging format that lets you define items, blocks, recipes, sounds, models, textures, language files, and Groovy scripts in a single distributable package — no separate mod project or Java compilation required. Bundles are discovered automatically, loaded dynamically, and can include their own resource packs.

## Additional Features

- **Runtime Dimensions** — On-the-fly world creation with custom chunk generators, clock controls, and per-dimension game rules.
- **Cutscene Editor** — Timeline-based cinematic tooling with Bézier camera paths, screen effects, and keyframe command triggers.
- **Game Stages** — A progression framework that gates recipes, items, mob spawns, loot tables, and dimensions behind player-specific stages.
- **Areas** — Spatial zone triggers that fire events on player entry, exit, or tick intervals.
- **Custom Particles** — Keyframe-driven particle system with control over position, scale, rotation, color, velocity, and lifetime.
- **Waypoints** — Persistent, colored in-world markers synced across clients.
- **Scripting** — Groovy 5-based runtime scripting for bundle logic.
- **Public API** — Events and builders for addon developers to hook into the engine.
