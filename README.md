<div align="center">
  <img src="src/main/resources/assets/foundryengine/textures/logo_transparent.png" alt="FoundryEngine Logo" width="128"/>
  <h1>Foundry Engine</h1>
  <p>An in-game development platform and scripting engine built as a Minecraft mod.</p>
  <p>
    <a href="https://www.curseforge.com/minecraft/mc-mods/foundry-engine">
      <img src="https://img.shields.io/badge/CurseForge-orange?logo=curseforge&logoColor=white&style=for-the-badge" alt="CurseForge"/>
    </a>
    <a href="https://modrinth.com/project/foundryengine">
      <img src="https://img.shields.io/badge/Modrinth-green?logo=modrinth&logoColor=white&style=for-the-badge" alt="Modrinth"/>
    </a>
    <a href="https://github.com/LuckyMcDev/FoundryEngine">
      <img src="https://img.shields.io/badge/GitHub-black?logo=github&logoColor=white&style=for-the-badge" alt="GitHub"/>
    </a>
    <a href="https://luckymcdev.github.io/FoundryEngine">
      <img src="https://img.shields.io/badge/Docs-blue?logo=docusaurus&logoColor=white&style=for-the-badge" alt="Documentation"/>
    </a>
    <a href="https://www.codefactor.io/repository/github/luckymcdev/foundryengine">
      <img src="https://www.codefactor.io/repository/github/luckymcdev/foundryengine/badge?style=for-the-badge" alt="CodeFactor"/>
    </a>
    <a href="https://moddex.gg/mod/foundryengine">
      <img src="https://moddex.gg/badges/projects/foundryengine/rating.svg?style=for-the-badge" alt="ModDex rating"/>
    </a>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Minecraft-26.1.2%20%7C%2026.2-yellow?style=for-the-badge" alt="Minecraft Versions"/>
    <img src="https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white&style=for-the-badge" alt="Java 25"/>
    <img src="https://img.shields.io/badge/NeoForge-Latest-e67e22?style=for-the-badge" alt="NeoForge"/>
    <img src="https://img.shields.io/github/actions/workflow/status/LuckyMcDev/FoundryEngine/build.yml?style=for-the-badge" alt="Build Status"/>
  </p>
</div>

---

## Overview

FoundryEngine is a NeoForge mod built with Java 25. It provides a scripting runtime and an in-game editor suite so creators can build custom content and game mechanics directly in the client.

Content is packaged into bundles that contain Groovy scripts, textures, models, and audio. Scripts register items, blocks, recipes, branching dialogues, cinematic camera sequences, spatial trigger zones, and isolated dimensions. Changes reload live with `/engine reload`, so you can test script and asset edits without restarting Minecraft or recompiling JAR files.

> Note: Registry changes (such as registering new items or blocks) require a game restart and cannot be hot reloaded at this time.

---

## Features

### Bundles and Scripting
- Self-contained packages: Store scripts, assets, and configurations in individual bundle folders inside `.minecraft/FoundryEngine/bundles/` (or `FoundryEngine/bundles/` on dedicated servers).
- Live reloading: Run `/engine reload` to apply script and asset edits immediately. (Registry changes require a game restart and cannot be hot reloaded at runtime.)
- Separate entrypoints: Organize logic across `common`, `server`, and `client` lifecycle scripts.

### In-Game ImGui Editor
- Script editor: Write and inspect Groovy scripts and JSON files in-game with syntax highlighting and error diagnostics.
- Dialogue editor: Build branching conversations with options and branching sequences.
- Cutscene timeline: Animate the Camera by placing points in the world and using bezier handles to define its movement.
- Spatial area gizmos: 3D areas in the world that trigger script based actions.
- Developer utilities: Browse files, inspect textures, view console output, and manage Git commits.

### Engine Subsystems
- Registry builders: Register items, blocks, block entities, recipes, sounds, particles, and tags using fluent Java/Groovy APIs.
- Event system: Subscribe to player actions, block interactions, entity lifecycles, ticks, and engine events.
- Spatial triggers: Define box or block regions backed by spatial hashing to run logic when entities enter, leave, or stay inside.
- Dialogue system: Play branching conversations in chat or on-screen with player choices.
- Cutscenes: Animate camera paths with configurable interpolation (such as sine, cubic, bounce, and elastic) and screen fades.
- Post-processing: Apply screen shaders, vignette, depth blur, color transitions, and any other shader effect.
- Progression stages: Gate items, blocks, mobs, recipes, and dimensions behind player stages.
- Instanced dimensions: Instance a world so the main copy doesn't get modified while playing in it.

---

## Multi-Version Development with Stonecutter

FoundryEngine supports Minecraft 26.1.2 and Minecraft 26.2 simultaneously using [Stonecutter](https://stonecutter.kikugie.dev/).

The codebase uses Stonecutter preprocessor comments to handle version-specific API differences:

```java
//? if >=26.2 {
/*import net.minecraft.world.item.Item;*/
//? } else {
import net.minecraft.world.item.Item;
//? }
```

To switch the active target version in your IDE or terminal, run the corresponding Gradle task:

```bash
# Switch active version to Minecraft 26.1.2
./gradlew "Set active project to 26.1"

# Switch active version to Minecraft 26.2
./gradlew "Set active project to 26.2"
```

When using IntelliJ IDEA, the Stonecutter plugin automatically syncs the active version with your project view.

---

## Building from Source

### Prerequisites
- JDK 25
- Gradle 9.x (wrapper included)
- IntelliJ IDEA with the [Stonecutter plugin]([https://stonecutter.kikugie.dev/](https://plugins.jetbrains.com/plugin/25044-stonecutter-dev)) (recommended)

### Common Gradle Tasks
```bash
# Clone the repository
git clone https://github.com/LuckyMcDev/FoundryEngine.git
cd FoundryEngine

# Switch active Minecraft target version
./gradlew "Set active project to 26.1"
# or
./gradlew "Set active project to 26.2"

# Launch the Minecraft client
./gradlew 26.1:runClient

# Build the mod JAR for the active version
./gradlew 26.1:build
```

---

## Links and Resources

- Documentation: [luckymcdev.github.io/FoundryEngine](https://luckymcdev.github.io/FoundryEngine)
- Example Bundle Template: [LuckyMcDev/ExampleBundle](https://github.com/LuckyMcDev/ExampleBundle)
- Issue Tracker: [GitHub Issues](https://github.com/LuckyMcDev/FoundryEngine/issues)

---

## Credits

- [LuckyMcDev](https://github.com/LuckyMcDev): Creator and lead developer
- [Auseawesome](https://github.com/Auseawesome): Feature design and logo artwork
- [G_cat](https://github.com/gcat101): Alpha testing

### Libraries and Tools
- [NeoForge](https://neoforged.net/): Mod loading framework
- [Apache Groovy](https://groovy-lang.org/): Dynamic scripting runtime
- [ImGuiMc](https://modrinth.com/mod/imguimc): In-game UI system
- [Stonecutter](https://stonecutter.kikugie.dev/): Multi-version build tool
- [KubeJS](https://kubejs.com/): Workflow inspiration
- [game-icons.net](https://game-icons.net/): Original cog icon base (CC-BY-3.0)
