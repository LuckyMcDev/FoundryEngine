# Systems overview

FoundryEngine provides game-engine-style systems that run alongside Minecraft.

## Editor & Cinematics

| System                      | What it does                                           |
|-----------------------------|--------------------------------------------------------|
| [In-Game Editor](editor.md) | ImGui-based dockable editor with panels for everything |
| [Cutscenes](cutscenes.md)   | Bezier camera paths with timeline effects and commands |

## World & Progression

| System                               | What it does                                    |
|--------------------------------------|-------------------------------------------------|
| [Custom Worlds](instanced-worlds.md) | Create runtime dimensions on the fly            |
| [Game Stages](stages.md)             | Gate content behind progression milestones      |
| [Game Sessions](game-sessions.md)    | Stateful lifecycle for minigames and game modes |
| [Areas](areas.md)                    | Spatial zones with enter/leave/tick modules     |
| [Waypoints](waypoints.md)            | Persistent colored in-world markers             |

## Rendering & Effects

| System                                | What it does                                     |
|---------------------------------------|--------------------------------------------------|
| [Custom Particles](particles.md)      | Keyframe-driven particle animation               |
| [Post-Processing](post-processing.md) | Shader effects with fade transitions             |
| [Mesh Rendering](mesh-rendering.md)   | Custom 3D rendering with OBJ support             |
| [Easing Functions](easing.md)         | 31 Penner easing functions for smooth animations |

## Interaction

| System                                | What it does                                |
|---------------------------------------|---------------------------------------------|
| [Dialogue System](dialogue.md)        | Branching NPC conversation trees            |
| [Markdown Rendering](markdown.md)     | Render Markdown as formatted Minecraft text |
| [NBT Suggestions](nbt-suggestions.md) | Tab-completion for NBT in vanilla commands  |

## Reference

| Page                              | What it covers         |
|-----------------------------------|------------------------|
| [Commands Reference](commands.md) | All `/engine` commands |
