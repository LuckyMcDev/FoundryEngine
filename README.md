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
  </p>

A NeoForge Minecraft mod that turns Minecraft into a development-ready game engine.

![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-yellow)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.6.1-blue?logo=gradle&logoColor=white)
![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.93-e67e22)
![Builds](https://img.shields.io/github/actions/workflow/status/LuckyMcDev/FoundryEngine/build.yml)
</div>

---

<div align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/foundry-engine">
    <img src="https://modfolio.creeperkatze.dev/curseforge/project/1605117?showSummary=true" alt="CurseForge" />
  </a>
  <br />
  <a href="https://modrinth.com/project/AaUmWHXd">
    <img src="https://modfolio.creeperkatze.dev/modrinth/project/AaUmWHXd" alt="Modrinth" />
  </a>
</div>

Foundry Engine provides a large set of tools to ease the development of Minecraft additions. It does this by providing a new API called **Bundles**, a new way of loading content into the game. The mod is focused on allowing you to create custom games inside Minecraft.

## Features

- Custom scripting in Groovy
- Cutscene engine
- In-game development GUI
- Game stages
- Area management
- Waypoints

> Runs on both client and server but can function on either alone.
> Some features may not work when running on only one side.

## Project Structure

Follows a normal Java project structure. Now uses The Stonecutter library to manage developing for both 26.1 and 26.2 simultaneously. I recommend installing the Intellij companion for stonecutter to ease development.

## Contributing

Contributions like additions to the event system or full features are appreciated. Open an issue or pull request on the [GitHub repository](https://github.com/LuckyMcDev/FoundryEngine).

## Credits

- [LuckyMcDev](https://github.com/LuckyMcDev) - Main Developer
- [Auseawesome](https://github.com/Auseawesome) - helped with the design of features
- [G_cat](https://github.com/gcat101) - alpha testing

## Acknowledgements

- [NeoForge](https://neoforged.net/) for their mod loader
- [KubeJS](https://kubejs.com/) - inspired Foundry Engine
- [Ocornut](https://github.com/ocornut) and [SpaiR](https://github.com/SpaiR) for [ImGui](https://github.com/ocornut/imgui) and [imgui-java](https://github.com/SpaiR/imgui-java)
- Apache for the Groovy language
- https://game-icons.net/ for the Logo