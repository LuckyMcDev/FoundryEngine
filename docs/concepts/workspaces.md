# Concepts: Workspaces

## General Info

A workspace is where you develop your bundle. There are two approaches:

1. **In-game folder** — Create a folder directly in `.minecraft/FoundryEngine/bundles/`
2. **Template project** — Use the Foundry Engine template (GitHub template or Gradle project)

## In-Game Folder

Create a folder in `.minecraft/FoundryEngine/bundles/your-bundle/` with the
[bundle structure](bundles.md). Scripts, assets, and data go directly into this
folder. No build step needed — just `/engine reload` after changes.

**Location of `.bundles.toml`**: `.minecraft/FoundryEngine/bundles/your-bundle/your-bundle.bundles.toml`

## Template Project

Clone or generate from the Foundry Engine template. The source structure is:

```
src/
├── main/
│   ├── groovy/       # Scripts (server/, client/, common/)
│   └── resources/    # Assets and data (assets/, data/, .bundles.toml)
```

Run `gradlew deployBundle` to copy the built bundle into Minecraft's run directory,
then `gradlew runClient` to launch.

**Location of `.bundles.toml`**: `src/main/resources/your-bundle.bundles.toml`

## Which to Choose?

| Approach | Pros | Cons |
|----------|------|------|
| In-game folder | No build setup, instant reload | No version control built in |
| Template project | Version control, build tasks, deploy automation | Requires Gradle setup |

## See Also

- [Bundles](bundles.md) — Bundle structure and manifest
- [Getting Started](../getting_started.md) — Step-by-step workspace setup
