# Concepts: Dependencies

A dependency tells Foundry Engine: "I need this mod or bundle to run, and if it's
missing, don't load me."

Dependencies are declared in your bundle's `.bundles.toml` file. They can reference
either other bundles or NeoForge mods.

## Bundle Dependency

```toml
dependencies = [
    "bundle:my-library@1.0.0"
]
```

## Mod Dependency

```toml
dependencies = [
    "mod:neoforge@26.1.0.1-beta"
]
```

## Version Syntax

Dependencies use `@` to separate the name from the version requirement.
Version strings follow semantic versioning. If a dependency is not met,
the bundle will not load and an error is shown in the mods menu.

## See Also

- [Bundles](bundles.md) — More about the bundle manifest format