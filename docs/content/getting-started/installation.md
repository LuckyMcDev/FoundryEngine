# Installation

Install Minecraft, NeoForge, and FoundryEngine to start building bundles.

## Prerequisites

### Java 25+

FoundryEngine needs **Java 25 or later**. Download it from [Adoptium Temurin](https://adoptium.net/).

Check your Java version:

```bash
java -version
```

The version number should start with `25`. If the command is not found, install Java and restart your terminal.

### NeoForge 26.1.x

FoundryEngine runs on **NeoForge 26.1.x** for **Minecraft 26.1.x**.

**Using a custom launcher (Prism, MultiMC, ATLauncher, etc.):**

1. Create a new instance
2. Pick the right Minecraft version for your NeoForge release
3. Select **NeoForge** as the mod loader (latest 26.1.x.x version)

**Using the official Minecraft launcher:**

Follow the [NeoForge client installation guide](https://docs.neoforged.net/users/client/).

## Installing FoundryEngine

1. Download the latest FoundryEngine `.jar` from the [releases page](https://github.com/LuckyMcDev/FoundryEngine/releases)
2. Place the `.jar` into your instance's `mods/` folder
3. Launch Minecraft with the NeoForge profile

## Verifying installation

When you first launch, FoundryEngine creates a folder inside your Minecraft directory:

```
.minecraft/
└── FoundryEngine/
    ├── bundles/          # Your bundles go here
    ├── config/           # Per-bundle configuration
    └── .cache/           # Internal cache
```

If you see this folder, the mod loaded. You can also check the Mods menu — **FoundryEngine** should appear.

## What's next

Now that FoundryEngine is installed:

- [Your First Bundle](first-bundle.md) — step-by-step tutorial
- [Core Concepts](../core-concepts/) — understand bundles, scripts, events
- [Examples](../examples/) — see working code
