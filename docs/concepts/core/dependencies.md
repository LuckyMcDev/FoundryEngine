# Dependencies

A dependency tells FoundryEngine: "I need this mod or bundle to run. If it is missing, do not load me."

Dependencies are declared in your bundle's `.bundles.toml` file. They can reference either other bundles or NeoForge mods.

## Bundle dependency

```toml
dependencies = [
    "bundle:my-library@1.0.0"
]
```

## Mod dependency

```toml
dependencies = [
    "mod:neoforge@26.1.0.1-beta"
]
```

## Version syntax

Dependencies use `@` to separate the name from the version requirement. Version strings follow semantic versioning. If a dependency is not met, the bundle will not load and an error is shown in the mods menu.

| Prefix | Type |
|---|---|
| `bundle:` | Another FoundryEngine bundle |
| `mod:` | A NeoForge mod (mod ID) |

## See also

- [Bundles](bundles) -- Bundle manifest format
