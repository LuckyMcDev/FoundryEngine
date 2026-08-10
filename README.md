<div align="center">
  <img src="src/main/resources/assets/foundryengine/textures/logo_transparent.png" alt="FoundryEngine Logo" width="128"/>
  <h1>Foundry Engine</h1>
  <p>
    <a href="https://www.curseforge.com/minecraft/mc-mods/foundry-engine">
      <img src="https://img.shields.io/badge/CurseForge-Get%20it-orange?logo=curseforge&logoColor=white" alt="CurseForge"/>
    </a>
    <a href="https://modrinth.com/project/foundryengine">
      <img src="https://img.shields.io/badge/Modrinth-Get%20it-green?logo=modrinth&logoColor=white" alt="Modrinth"/>
    </a>
    <a href="https://github.com/LuckyMcDev/FoundryEngine">
      <img src="https://img.shields.io/badge/GitHub-View%20Source-black?logo=github&logoColor=white" alt="GitHub"/>
    </a>
    <a href="https://www.codefactor.io/repository/github/luckymcdev/foundryengine">
        <img src="https://www.codefactor.io/repository/github/luckymcdev/foundryengine/badge" alt="CodeFactor" />
    </a>
    <a href='https://moddex.gg/mod/foundryengine'>
        <img src='https://moddex.gg/badges/projects/foundryengine/rating.svg' alt='ModDex rating'>
    </a>
  </p>

A NeoForge Minecraft mod that turns Minecraft into a development-ready game engine.

![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-yellow)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.6.1-blue?logo=gradle&logoColor=white)
![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.93-e67e22)
![Builds](https://img.shields.io/github/actions/workflow/status/LuckyMcDev/FoundryEngine/build.yml)
</div>

---

Foundry Engine gives you a large set of tools for building Minecraft content. At the center of it is a new API called **Bundles**: a way to load scripts, assets, and data into the game as one package. With Bundles you can make custom items, custom blocks, and even fully custom games.

## Features

### Core

- **Groovy scripting** - write mod-like content with hot reload support
- **Bundle system** - package scripts, assets, and data together, then load them like a lightweight mod
- **In-game editor** - a Dear ImGui based editor for creating cutscenes, editing JSON files, managing areas, and more, without leaving the game

### Additional

- **Instanced worlds** - save a world in a bundle and run a copy of it, so the original is never modified
- **Cutscenes** - a custom cutscene system
- **Game stages** - gate content behind unlockable stages
- **Areas** - custom regions that trigger things
- **Post-processing** - custom post-processing effects
- **Game sessions** - the system that manages custom games
- **Waypoints** - mark and navigate to saved locations

> Runs on both the client and server, and can run on either alone.
> Some features only work fully when both sides are present.

## Project Structure

A standard Java project structure. Uses [Stonecutter](https://stonecutter.kikugie.dev/) to develop for both Minecraft 26.1 and 26.2 at the same time. Installing the IntelliJ companion for Stonecutter makes development easier.

## Contributing

Contributions are welcome, whether it is an addition to the event system or a full feature. Open an issue or a pull request on the [GitHub repository](https://github.com/LuckyMcDev/FoundryEngine).

## Credits

- [LuckyMcDev](https://github.com/LuckyMcDev) - main developer
- [Auseawesome](https://github.com/Auseawesome) - helped with feature design
- [G_cat](https://github.com/gcat101) - alpha testing

## Acknowledgements

- [NeoForge](https://neoforged.net/) for the mod loader
- [KubeJS](https://kubejs.com/) - inspired Foundry Engine
- [Ocornut](https://github.com/ocornut) and [SpaiR](https://github.com/SpaiR) for [ImGui](https://github.com/ocornut/imgui) and [imgui-java](https://github.com/SpaiR/imgui-java)
- [ImGuiMc](https://modrinth.com/mod/imguimc) adds an imgui implementation for Minecraft.
- Apache for the Groovy language
- [game-icons.net](https://game-icons.net/) for the logo
