# Systems Overview

FoundryEngine provides a collection of game-engine-style systems that run alongside Minecraft. These systems cover everything from progression and world management to rendering and content creation.

## Editor System

- [In-Game Editor](editor) — Dear ImGui-based dockable panels, cutscene editor, in-world editing, theming

## Camera & Cinematics

- [Cutscene System](cutscenes) — Bezier camera paths with timeline-based screen effects and server commands

## World Management

- [Instanced Worlds](instanced-worlds) — Runtime dimensions with custom chunk generators, clocks, and game rules
- [Areas](areas) — Spatial zones (AABB / single block) with modular behaviour via enter/leave/tick/render modules

## Progression

- [Game Stages](stages) — String-based progression milestones that gate items, mobs, dimensions, loot, and recipes
- [Game Sessions](game-sessions) — Stateful session lifecycle for minigames and custom game modes

## Navigation

- [Waypoints](waypoints) — Persistent colored in-world markers

## Rendering

- [Custom Particles](particles) — Keyframe-driven particle animation system
- [Post-Processing](post-processing) — Priority-based shader effects with fade transitions
- [Mesh Rendering & OBJ](mesh-rendering) — Custom mesh rendering engine, OBJ model loading, and pipeline system

## Dialogue

- [Dialogue System](dialogue) — Branching NPC dialogue trees with conditions, actions, and display modes

## Utilities

- [Easing Functions](easing) — 31 Penner easing functions plus CSS-style BezierEasing
- [Commands](commands) — Complete `/engine` command reference
