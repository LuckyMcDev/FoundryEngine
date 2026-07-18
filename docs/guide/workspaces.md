# Workspaces

A workspace is where you develop your bundle. FoundryEngine supports two approaches: the **in-game folder** for quick iteration and the **template project** for production-ready development.

## Comparison

| Aspect                    | In-Game Folder                    | Template Project                              |
|---------------------------|-----------------------------------|-----------------------------------------------|
| **Setup time**            | None (create a folder)            | Clone or generate from GitHub template        |
| **Build step**            | Not required                      | `gradlew deployBundle`                        |
| **Reload**                | `/engine reload` (instant)        | `gradlew deployBundle` then `/engine reload`  |
| **Version control**       | Manual                            | Git built-in                                  |
| **Dependency management** | Manual                            | Gradle + `deployBundle` resolves dependencies |
| **Gradle tasks**          | None                              | `deployBundle`, `runClient`, `test`, `clean`  |
| **IDE support**           | Text editor                       | IntelliJ IDEA / VS Code with Groovy support   |
| **Best for**              | Quick prototyping, small projects | Team projects, distribution, CI/CD            |

## In-game folder

The simplest way to start. Create a folder directly in your Minecraft run directory:

```
.minecraft/FoundryEngine/bundles/your-bundle/
```

The `.bundles.toml` manifest goes at the bundle root:

```
.minecraft/FoundryEngine/bundles/your-bundle/your-bundle.bundles.toml
```

**Workflow:**

1. Edit or add files in the folder
2. Run `/engine reload` in-game
3. Test your changes immediately. No build step, no restart.

This is ideal for rapid prototyping. The trade-off is the lack of built-in version control and dependency resolution.

## Template project

The [FoundryEngine Bundle Template](https://github.com/LuckyMcDev/FoundryEngineBundleTemplate) is a GitHub template repository with a full Gradle project structure.

### Creating a project

```bash
git clone https://github.com/LuckyMcDev/FoundryEngineBundleTemplate my-bundle
cd my-bundle
```

Or click **Use this template** on the GitHub page and clone the result.

### Project structure

```
my-bundle/
├── build.gradle                 # Gradle build script
├── settings.gradle              # Gradle settings
├── gradle.properties            # Version and dependency config
├── gradlew / gradlew.bat        # Gradle wrapper scripts
├── gradle/
│   └── wrapper/
├── src/
│   └── main/
│       ├── groovy/              # Groovy scripts
│       │   ├── common/          #   Shared logic (items, blocks, recipes)
│       │   ├── client/          #   Client-only logic
│       │   └── server/          #   Server-only logic
│       └── resources/           # Assets, data, and manifest
│           ├── your-bundle.bundles.toml
│           ├── assets/
│           │   └── <namespace>/
│           │       ├── textures/
│           │       ├── models/
│           │       └── sounds/
│           └── data/
│               └── <namespace>/
│                   ├── recipes/
│                   └── loot_tables/
└── build/
    ├── bundles/                 # deployBundle output
    └── ...
```

### Key differences from in-game folder

| Aspect            | In-Game Folder                           | Template Project                              |
|-------------------|------------------------------------------|-----------------------------------------------|
| Script location   | `scripts/common/`                        | `src/main/groovy/common/`                     |
| Manifest location | `bundle-folder/your-bundle.bundles.toml` | `src/main/resources/your-bundle.bundles.toml` |
| Asset location    | `assets/`                                | `src/main/resources/assets/`                  |
| Data location     | `data/`                                  | `src/main/resources/data/`                    |

### Key Gradle tasks

| Task           | Command                  | Purpose                                                     |
|----------------|--------------------------|-------------------------------------------------------------|
| `deployBundle` | `./gradlew deployBundle` | Build the bundle and copy it to the Minecraft run directory |
| `runClient`    | `./gradlew runClient`    | Launch Minecraft with NeoForge and the deployed bundle      |
| `clean`        | `./gradlew clean`        | Remove build artifacts                                      |
| `test`         | `./gradlew test`         | Run any JUnit tests in the project                          |

### Build and deploy workflow

```bash
# Build and deploy your bundle
./gradlew deployBundle

# Launch Minecraft to test
./gradlew runClient
```

`deployBundle` compiles your Groovy scripts, packages them with assets and data, and copies the output to the correct `bundles/` directory so `/engine reload` picks it up.

The output goes to `build/bundles/your-bundle/`. You can share this folder with other players.

### Version control

The template initializes a Git repository automatically. Use standard Git workflows:

```bash
git add .
git commit -m "Add custom item and recipe"
```

## Which should you choose?

**Start with the in-game folder** if you're exploring FoundryEngine or building a small personal project. You can always migrate to the template later by copying your scripts and assets into the `src/` structure.

**Use the template project** if you're building a bundle for distribution, working in a team, or need automated builds and dependency management.

## Migrating from in-game folder to template

1. Clone or generate the template project
2. Copy your `.bundles.toml` to `src/main/resources/`
3. Copy your `scripts/` contents to `src/main/groovy/`
4. Copy your `assets/` and `data/` to `src/main/resources/`
5. Update the `bundleId` in your manifest if needed
6. Run `./gradlew deployBundle` and verify with `/engine reload`

## Next steps

- [Getting Started: Your First Bundle](getting-started) -- Step-by-step tutorial for both approaches
- [Bundles](/concepts/core/bundles) -- Bundle structure and manifest reference
- [Builders](/concepts/core/builders) -- Register items, blocks, recipes, sounds, and particles
- [Examples](/examples/) -- Complete working examples
