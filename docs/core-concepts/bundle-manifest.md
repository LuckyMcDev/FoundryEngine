# Bundle Manifest

Every bundle needs a `.bundles.toml` file. It identifies the bundle to FoundryEngine: who made it, what version, and what other mods it needs.

## Example

```toml
[[bundles]]
bundleId = "my_bundle"
version = "1.0.0"
displayName = "My Bundle"
displayURL = "https://example.com"
authors = "YourName"
description = "A cool bundle with custom items!"
dependencies = [
    "mod:neoforge@26.1.0.1-beta",
    "bundle:some-library@1.0.0"
]
```

## Fields explained

| Field          | Required | What it does                                              |
|----------------|----------|-----------------------------------------------------------|
| `bundleId`     | Yes      | Unique name (lowercase, underscores, no spaces or dashes) |
| `version`      | Yes      | Version number (like `1.0.0` or `0.1.0`)                  |
| `displayName`  | Yes      | Human-readable name (shown in the mods menu)              |
| `displayURL`   | No       | Link to your project page                                 |
| `authors`      | Yes      | Your name or team                                         |
| `description`  | No       | Short description (shown in the mods menu)                |
| `dependencies` | No       | List of required mods or bundles                          |

## Naming rules

The `bundleId` has strict rules:

- Lowercase letters only
- Use underscores (`_`) instead of spaces or dashes
- Must be unique — no two bundles can have the same ID

The manifest file must be named `your-bundle-name.bundles.toml` (replace `your-bundle-name` with your bundle's name).

## Dependencies

Dependencies tell FoundryEngine: "I need this to run." If a dependency is missing, your bundle will not load.

```toml
dependencies = [
    "mod:neoforge@26.1.0.1-beta",   # Needs a mod
    "bundle:my-library@1.0.0"       # Needs another bundle
]
```

- `mod:` prefix + mod ID + `@` + version
- `bundle:` prefix + bundle ID + `@` + version

## Next

- [Your First Bundle](../getting-started/first-bundle.md) — create a bundle step by step
- [Dependencies](dependencies.md) — more about dependency syntax
