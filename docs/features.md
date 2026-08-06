# Feature Overview

FoundryEngine turns Minecraft into a game engine: author mods without writing Java, and drive the engine with **Groovy bundles**, an **in-game editor**, and game-engine-style **systems**. This is a single index of everything it can do — each entry links to full usage docs.

> Build mods without writing Java. Such as cut scenes, custom dimensions, an editor, and game systems.

## Content authoring (bundles & builders)

| Feature            | Summary                                                               | Docs                                                                    |
|--------------------|-----------------------------------------------------------------------|-------------------------------------------------------------------------|
| Bundle system      | Self-contained content pack (scripts + textures + data in one folder) | [What is a Bundle?](/core-concepts/what-is-a-bundle)                    |
| Groovy scripts     | Script mods in Groovy, no Java compile step                           | [Core Concepts](/core-concepts/), [Scripts](/core-concepts/scripts)     |
| Items              | Fluent `ItemBuilder` to register items                                | [Creating Items](/core-concepts/creating-items)                         |
| Blocks             | `BlockBuilder` for blocks & entities                                  | [Creating Blocks](/core-concepts/creating-blocks)                       |
| Recipes            | `RecipeBuilder` (shaped, shapeless, smelting)                         | [Creating Recipes](/core-concepts/creating-recipes)                     |
| Sounds & Particles | `SoundBuilder`, `ParticleBuilder`                                     | [Creating Sounds & Particles](/core-concepts/creating-sounds-particles) |
| Registration       | RegistryEvent + registry collectors                                   | [Registration](/core-concepts/registration)                             |
| Addon API          | Public Java events and builders                                       | [Addon API](/advanced/addon-api)                                        |
| Data Generation    | Data providers for loot, recipes, tags, models, languages             | [Data Generation](/advanced/data-generation)                            |

## Engine systems

| Feature        | Purpose                                           | Docs                                          |
|----------------|---------------------------------------------------|-----------------------------------------------|
| In-Game Editor | Dockable ImGui editor with panels                 | [Editor](/systems/editor)                     |
| Cutscenes      | Bezier camera paths, timeline effects, commands   | [Cutscenes](/systems/cutscenes)               |
| Areas          | Spatial zones with enter/leave/tick/block modules | [Areas](/systems/areas)                       |
| Waypoints      | Persistent colored in-world markers               | [Waypoints](/systems/waypoints)               |
| Dialogue       | Branching NPC conversation trees                  | [Dialogue](/systems/dialogue)                 |
| Game Stages    | Gate content behind progression milestones        | [Stages](/systems/stages)                     |
| Game Sessions  | Stateful lifecycle for minigames & game modes     | [Game Sessions](/systems/game-sessions)       |
| Custom Worlds  | Runtime dimensions on the fly                     | [Instanced Worlds](/systems/instanced-worlds) |
| Saved Data     | Persistent NBT store + server/client sync         | [Persistence](/systems/persistence)           |
| Commands       | `/engine ...` command suite                       | [Commands](/systems/commands)                 |

## Rendering & effects

| Feature          | Purpose                                     | Docs                                        |
|------------------|---------------------------------------------|---------------------------------------------|
| Custom Particles | Keyframe anim (color, scale, pos, velocity) | [Particles](/systems/particles)             |
| Post-Processing  | Shader effects with fades                   | [Post-Processing](/systems/post-processing) |
| Mesh / OBJ       | Custom 3D pipeline                          | [Mesh Rendering](/systems/mesh-rendering)   |
| Skybox           | Custom model-sky around the player          | [Skybox](/systems/skybox)                   |
| Node Graph       | Visual typed data-flow editor               | [Node Graph Editor](/systems/node-editor)   |
| Easing           | 31 easing functions + bezier                | [Easing](/systems/easing)                   |

## Interaction & developer tools

| Feature            | Purpose              | Docs                                        |
|--------------------|----------------------|---------------------------------------------|
| Item Tooltips      | Debug + tag badges   | [Tooltips](/systems/tooltips)               |
| Markdown rendering | Render Markdown text | [Markdown](/systems/markdown)               |
| NBT suggestions    | NBT tab-completion   | [NBT Suggestions](/systems/nbt-suggestions) |
| Audio Streaming    | Play MP3 / FLAC      | [Audio Streaming](/systems/audio-streaming) |
| Commands           | `/engine` reference  | [Commands](/systems/commands)               |

## For developers

- [Advanced](/advanced/) — network packets, data generation, editor themes, mixin architecture
- [Examples](/examples/) — showcase bundle