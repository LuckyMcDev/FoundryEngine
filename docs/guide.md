# FoundryEngine User Guide

This guide covers installing Minecraft, NeoForge, and FoundryEngine so you can start using bundles.

## Prerequisites

### Java

FoundryEngine requires **Java 25+**. We recommend [Adoptium Temurin](https://adoptium.net/).

**Verify your installation:**

```bash
java -version
```

If you see a version number, you're good. If not, download and install from [Adoptium](https://adoptium.net/).

### NeoForge

FoundryEngine runs on **NeoForge 26.1.x** for Minecraft 1.21.5.

**Using a custom launcher (Prism, MultiMC, etc.):**
1. Create a new instance
2. Select Minecraft 1.21.5
3. Select NeoForge as the mod loader

**Using the vanilla launcher:**
Follow the [NeoForge client installation guide](https://docs.neoforged.net/user/docs/client).

## Installing FoundryEngine

1. Download the latest FoundryEngine `.jar` from the [releases page](https://github.com/LuckyMcDev/FoundryEngine/releases)
2. Place it in your `mods/` folder
3. Launch the game

The first launch will create the `FoundryEngine/` folder inside your Minecraft directory, ready for bundles.

## Where to Go Next

- [Getting Started](getting_started) — Set up a workspace and create your first bundle
- [Concepts Overview](concepts/index) — Understand bundles, scripts, events, and more
- [Examples](examples) — See working code for items, blocks, recipes, and events
