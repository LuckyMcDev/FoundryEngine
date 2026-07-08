# Sides

Minecraft operates in two environments: **Server** and **Client**. When you connect to a multiplayer server, you run the Client and the server runs separately. In singleplayer, both run on the same machine but remain logically separate.

FoundryEngine follows this separation. Scripts are organized into three folders with **sub-packages** matching your bundle namespace:

```
scripts/
├── server/<namespace>/     # Server-only code
├── common/<namespace>/     # Shared code (runs on both sides)
└── client/<namespace>/     # Client-only code
```

## What Goes Where

| Folder | What you can do |
|---|---|
| `common/` | Register items, blocks, recipes, sounds, particles. Use `BundleEvents`, `BlockEvents`, `ItemEvents`, `PlayerEvents`, `ServerEvents`, `LevelEvents`, `EntityEvents`, `CommandEvents` |
| `client/` | Rendering, GUI, key bindings, client ticks. Use `ClientEvents`, editor panels |
| `server/` | Server-only commands, data management. Use `ServerEvents` |

## Entrypoints Per Side

Each folder can have its own entrypoint implementing `BundleEntrypoint`. You do not need one for every folder — only the sides your bundle requires. The engine loads each side's entrypoint independently.

## See also

- [Scripts and Entrypoints](scripts) -- Writing scripts for each side
- [Events](events) -- Some events are side-specific
