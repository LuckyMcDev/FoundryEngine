# Installation Guide

Install Minecraft, NeoForge, and FoundryEngine to start building bundles.

## Prerequisites

### Java 25+

FoundryEngine requires **Java 25 or later**. Download it from [Adoptium Temurin](https://adoptium.net/).

Verify your installation:

```bash
java -version
```

You should see a version number starting with `25`. If the command isn't found, install Java and restart your terminal.

### NeoForge 26.1.x

FoundryEngine runs on **NeoForge 26.1.x.x** for **Minecraft 26.1.x**.

**Using a custom launcher (Prism, MultiMC, ATLauncher, etc.):**

1. Create a new instance
2. Select the appropriate Minecraft version for your NeoForge release
3. Select **NeoForge** as the mod loader (latest 26.1.x.x version)

**Using the official Minecraft launcher:**

Follow the [NeoForge client installation guide](https://docs.neoforged.net/users/client/).

## Installing FoundryEngine

1. Download the latest FoundryEngine `.jar` from the [releases page](https://github.com/LuckyMcDev/FoundryEngine/releases)
2. Place the `.jar` into your instance's `mods/` folder
3. Launch Minecraft with the NeoForge profile

## Verifying Installation

On first launch, FoundryEngine creates a `FoundryEngine/` folder inside your Minecraft directory:

```
.minecraft/
└── FoundryEngine/
    ├── bundles/          # Place your bundles here
    ├── config/           # Per-bundle configuration
    └── .cache/           # Internal cache
```

If you see this folder, the mod loaded successfully. You can also check the Mods menu — FoundryEngine **0.0.65** should appear in the list.

## What's Next

Now that FoundryEngine is installed, create your first bundle:

- [Getting Started: Your First Bundle](getting-started) — Step-by-step tutorial
- [Concepts Overview](/concepts/core/) — Understand bundles, scripts, events, and more
- [Examples](/examples/) — See working code for items, blocks, recipes, and events
