# Concepts: Sides

Minecraft operates in two environments: **Server** and **Client**. When you connect
to a multiplayer server, you run the Client and the server runs separately. In
singleplayer, both run on the same machine but remain logically separate.

## Sides in Foundry Engine

Foundry Engine follows this separation. Scripts are organized into three folders:

```
scripts/
├── server/     # Server-only code
├── common/     # Shared code (runs on both sides)
└── client/     # Client-only code
```

### What Goes Where

| Folder    | What you can do                                                                                                                                                                     |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `common/` | Register items, blocks, recipes, sounds, particles. Use `BundleEvents`, `BlockEvents`, `ItemEvents`, `PlayerEvents`, `ServerEvents`, `LevelEvents`, `EntityEvents`, `CommandEvents` |
| `client/` | Rendering, GUI, key bindings, client ticks. Use `ClientEvents`, editor panels                                                                                                       |
| `server/` | Server-only commands, data management. Use `ServerEvents`                                                                                                                           |

Each folder can have its own entrypoint implementing `BundleEntrypoint`. You don't
need one for every folder — only the sides your bundle needs.

## See Also

- [Scripts](scripts.md) — Writing scripts for each side
- [Entrypoints](entrypoint.md) — Defining entrypoints
- [Events](events.md) — Event reference (some events are side-specific)