# Workspaces

A workspace is where you develop your bundle. FoundryEngine supports two approaches.

## Which one should you pick?

**Start with the in-game folder** if you are exploring or building a small personal project. You can always migrate later.

**Use the template project** if you are building a bundle for others to use, working in a team, or want automated builds.

## Comparison

| Aspect          | In-Game Folder             | Template Project            |
|-----------------|----------------------------|-----------------------------|
| Setup           | None (create a folder)     | Clone from GitHub template  |
| Build step      | None                       | `gradlew deployBundle`      |
| Reload          | `/engine reload` (instant) | Build → `/engine reload`    |
| Version control | Manual                     | Git built-in                |
| IDE support     | Any text editor            | IntelliJ / VS Code          |
| Best for        | Quick prototypes, learning | Team projects, distribution |

## In-game folder

Create a folder directly in your Minecraft directory:

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
3. Test changes immediately — no restart needed

## Template project

The [FoundryEngine Bundle Template](https://github.com/LuckyMcDev/FoundryEngineBundleTemplate) is a GitHub template with a full Gradle project.

### Creating a project

```bash
git clone https://github.com/LuckyMcDev/FoundryEngineBundleTemplate my-bundle
cd my-bundle
```

Or click **Use this template** on the GitHub page.

### Project structure

```
my-bundle/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/
│   └── wrapper/
├── src/
│   └── main/
│       ├── groovy/           # Groovy scripts (common, client, server)
│       └── resources/        # Manifest, assets, data
└── build/
    └── bundles/              # deployBundle output
```

### Key differences

| Aspect            | In-Game Folder     | Template Project             |
|-------------------|--------------------|------------------------------|
| Script location   | `scripts/common/`  | `src/main/groovy/common/`    |
| Manifest location | Bundle root folder | `src/main/resources/`        |
| Assets location   | `assets/`          | `src/main/resources/assets/` |
| Data location     | `data/`            | `src/main/resources/data/`   |

### Gradle tasks

| Task           | Command                  | What it does                           |
|----------------|--------------------------|----------------------------------------|
| `deployBundle` | `./gradlew deployBundle` | Build bundle and copy to run directory |
| `runClient`    | `./gradlew runClient`    | Launch Minecraft with the bundle       |
| `clean`        | `./gradlew clean`        | Remove build files                     |
| `test`         | `./gradlew test`         | Run JUnit tests                        |

### Build and deploy workflow

```bash
./gradlew deployBundle
./gradlew runClient
```

`deployBundle` compiles your Groovy scripts, packages assets, and copies the output to the bundles folder so `/engine reload` picks it up.

### Version control

The template initializes Git automatically:

```bash
git add .
git commit -m "Add my first bundle"
```

## Migrating from in-game folder to template

1. Clone or generate the template project
2. Copy `.bundles.toml` to `src/main/resources/`
3. Copy `scripts/` to `src/main/groovy/`
4. Copy `assets/` and `data/` to `src/main/resources/`
5. Run `./gradlew deployBundle` and verify with `/engine reload`

## What's next

- [Your First Bundle](first-bundle.md) — step-by-step tutorial
- [What is a Bundle?](../core-concepts/what-is-a-bundle.md) — bundle structure and lifecycle
- [Creating Items](../core-concepts/creating-items.md) — register items, blocks, recipes
