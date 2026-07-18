# Client & Server

Minecraft runs in two environments: **Client** and **Server**. Understanding the difference is important for modding.

## The two sides

| Side       | What it runs   | What it does                                  |
|------------|----------------|-----------------------------------------------|
| **Client** | Your computer  | Renders graphics, plays sounds, handles input |
| **Server** | The game world | Runs game logic, manages entities, saves data |

In **singleplayer**, both run on your computer but stay logically separate. In **multiplayer**, you run the Client and connect to a separate Server.

## Why this matters

Some code should only run on one side:

| Code                  | Side        | Reason                             |
|-----------------------|-------------|------------------------------------|
| Register items/blocks | Both        | Both sides need to know about them |
| Render a custom model | Client only | Servers do not render graphics     |
| Save player data      | Server only | Only the server saves data         |
| Open a GUI            | Client only | Only the client shows screens      |

## Script folders

FoundryEngine organizes scripts into three folders matching these sides:

```
scripts/
├── common/my_bundle/     # Runs everywhere
├── client/my_bundle/     # Runs only on the client
└── server/my_bundle/     # Runs only on the server
```

## Entrypoints per side

Each folder can have its own entrypoint. You do not need all three — only the ones your bundle needs:

```groovy
// scripts/common/my_bundle/Entrypoint.groovy
class Entrypoint implements BundleEntrypoint {
    @Override void onLoad() {
        // Register items, blocks, recipes — runs everywhere
    }
}
```

```groovy
// scripts/client/my_bundle/ClientEntrypoint.groovy
class ClientEntrypoint implements BundleEntrypoint {
    @Override void onLoad() {
        // Client-only setup — renderer hooks, keybinds
    }
}
```

```groovy
// scripts/server/my_bundle/ServerEntrypoint.groovy
class ServerEntrypoint implements BundleEntrypoint {
    @Override void onLoad() {
        // Server-only setup — commands, data
    }
}
```

## Quick reference

| What you want to do               | Put it in              |
|-----------------------------------|------------------------|
| Create items, blocks, recipes     | `common/`              |
| React to player join, block break | `common/`              |
| Custom rendering, particles       | `client/`              |
| GUI, key bindings                 | `client/`              |
| Server commands                   | `server/`              |
| Save/load data                    | `server/` or `common/` |

## Next

- [Scripts](scripts.md) — writing scripts for each side
- [Events Guide](events-guide.md) — some events are side-specific
