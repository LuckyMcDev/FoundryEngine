# Dependencies

A dependency tells FoundryEngine: "I need this mod or bundle to run. If it is missing, do not load me."

## Why use dependencies?

- Your bundle uses items from another mod
- Your bundle needs another bundle's library
- You want to make sure players have the right NeoForge version

## Declaring dependencies

Add a `dependencies` field to your `.bundles.toml`:

```toml
[[bundles]]
bundleId = "my_bundle"
# ... other fields ...

dependencies = [
    "mod:neoforge@26.1.0.1-beta",
    "bundle:my-library@1.0.0"
]
```

## Syntax

Each dependency follows this pattern:

```
<type>:<id>@<version>
```

| Part      | Example           | What it is                  |
|-----------|-------------------|-----------------------------|
| `type`    | `mod` or `bundle` | What kind of thing you need |
| `id`      | `neoforge`        | The mod ID or bundle ID     |
| `version` | `26.1.0.1-beta`   | Required version (semantic) |

### Types

| Prefix    | What it refers to                           |
|-----------|---------------------------------------------|
| `mod:`    | A NeoForge mod (by mod ID)                  |
| `bundle:` | Another FoundryEngine bundle (by bundle ID) |

## What happens if a dependency is missing?

The bundle will not load. An error message appears in the mods menu explaining what is missing.

## Next

- [Bundle Manifest](bundle-manifest.md) — full manifest reference
