# FoundryEngine

An in-game development platform and scripting engine built as a NeoForge Minecraft mod.

[![CurseForge](https://img.shields.io/badge/CurseForge-orange?logo=curseforge&logoColor=white&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/foundry-engine)
[![Modrinth](https://img.shields.io/badge/Modrinth-green?logo=modrinth&logoColor=white&style=for-the-badge)](https://modrinth.com/project/foundryengine)
[![GitHub](https://img.shields.io/badge/GitHub-black?logo=github&logoColor=white&style=for-the-badge)](https://github.com/LuckyMcDev/FoundryEngine)
[![Wiki](https://img.shields.io/badge/Wiki-blue?logo=docusaurus&logoColor=white&style=for-the-badge)](https://luckymcdev.github.io/FoundryEngine)
[![Java 25](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white&style=for-the-badge)](https://adoptium.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-Latest-e67e22?style=for-the-badge)](https://neoforged.net/)

---

### Summary
FoundryEngine is a NeoForge mod that lets creators build custom gameplay systems, items, cutscenes, and dialogues using Groovy scripts and an in-game editor. With modular bundles and live reloading, you can develop and test content directly inside Minecraft without restarting the game.

---

## What is FoundryEngine?

FoundryEngine provides an in-game development environment for Minecraft. It lets mapmakers, server owners, and modpack creators build standalone games, custom mechanics, and interactive story systems without compiling separate mod JARs.

Content is authored in Groovy scripts and organized into self-contained bundles. With the integrated Dear ImGui editor suite and runtime reloading (`/engine reload`), script edits and asset updates apply immediately while the game is running.

> Note: Registry changes (such as registering new items, blocks, or sounds) require a game restart and cannot be hot reloaded at this time.

---

## Key Features

### Bundles and Scripting
- Bundle packages: Group scripts, models, textures, and sounds into isolated folders inside `.minecraft/FoundryEngine/bundles/`
- Live reloading: Test script logic and asset changes live with `/engine reload`. (Registry changes require a game restart and cannot be hot reloaded yet.)
- Clear structure: Organize your code across dedicated `common`, `server`, and `client` entrypoints.

### In-Game ImGui Editor
- Script and JSON editor: Edit Groovy scripts directly inside Minecraft with syntax highlighting and diagnostic checks.
- Dialogue editor: Build branching conversations with player options, custom requirements, and script triggers.
- Visual cutscene timeline: Position cameras along splines with keyframes, focal points, and easing transitions.
- Spatial area tools: Place 3D bounding boxes in the world to configure trigger zones visually.
- Development utilities: Inspect textures, browse files, test recipes, monitor console logs, and commit changes to Git.

### Gameplay Subsystems
- Builder APIs: Register items, blocks, block entities, recipes, sounds, particles, and tags directly from scripts.
- Event bus: Hook into game ticks, block clicks, player interactions, combat, and custom engine lifecycle events.
- Stage progression: Lock items, blocks, mobs, recipes, and dimensions behind unlockable progression stages.
- Instanced dimensions: Spawn temporary dimension copies for custom game sessions, dungeons, or minigames without touching base world saves.
- Screen shaders: Apply post-processing effects including vignette, depth blur, color fades, and custom skyboxes.

---

## Getting Started

### For Players and Server Hosts
1. Install NeoForge on Minecraft 26.1.2 or 26.2
2. Place `FoundryEngine` and its required dependencies into your `mods/` directory.
3. Put downloaded content packages into the `/FoundryEngine/bundles/` folder.
4. Start the game or server.

### For Creators and Pack Authors
1. Check the [FoundryEngine Wiki](https://luckymcdev.github.io/FoundryEngine) for guides, builder tutorials, and API documentation.
2. Open the in-game editor using the configured shortcut or menu.
3. Create a new bundle in your `/FoundryEngine/bundles/` directory or start from the [ExampleBundle](https://github.com/LuckyMcDev/ExampleBundle) template.
4. Write your registration and gameplay logic in Groovy scripts.
5. Run `/engine reload` to test script and asset changes in real time. (Registry changes require a game restart.)

---

## Requirements and Compatibility

- Mod loader: [NeoForge](https://neoforged.net/)
- Java version: Java 25
- Supported versions: Minecraft 26.1.2 and Minecraft 26.2
- Environment: Client, Dedicated Server, and Singleplayer

---

## Links

- Documentation Wiki: [luckymcdev.github.io/FoundryEngine](https://luckymcdev.github.io/FoundryEngine)
- Source code: [GitHub Repository](https://github.com/LuckyMcDev/FoundryEngine)
- Starter template: [Example Bundle Repository](https://github.com/LuckyMcDev/ExampleBundle)
- Issues: [GitHub Issues](https://github.com/LuckyMcDev/FoundryEngine/issues)

---

## Credits

- [LuckyMcDev](https://github.com/LuckyMcDev): Creator and lead developer
- [Auseawesome](https://github.com/Auseawesome): Feature design and logo artwork
- [G_cat](https://github.com/gcat101): Alpha testing
- Built using [NeoForge](https://neoforged.net/), [Apache Groovy](https://groovy-lang.org/), [ImGuiMc](https://modrinth.com/mod/imguimc), and [Stonecutter](https://stonecutter.kikugie.dev/).
