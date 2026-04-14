# Concepts: Dependencies

A Dependency allows you to say:

Hey, I need this mod / bundle when running, and if I don't have it. Don't let the game run.

A Dependency can be either A mod, or a bundle.

Bundles are noted in this syntax:

```toml
dependecies = [
    "bundle:bundlename@version"
]
```

Mods are noted in this syntax:

```toml
dependecies = [
    "mod:modname@version"
]
```