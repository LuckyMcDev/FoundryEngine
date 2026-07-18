# Data Generation

FoundryEngine automatically creates asset and data files (models, recipes, loot tables, etc.) from your builders. This runs during Minecraft's startup phases.

## How it works

```
BundleDataGenerator
    ├── runAll() — runs for all loaded bundles
    └── run(Bundle) — runs for a single bundle

EngineDataGenerator
    ├── addProvider(DataProvider) — add a data provider
    └── run() — execute all providers
```

Generated output goes to `<gameDir>/FoundryEngine/temp/instances/<hash>/bundles/`.

## What gets generated

### Server-side

| Provider                         | Generates         |
|----------------------------------|-------------------|
| `EngineAdvancementProvider`      | Advancement JSONs |
| `EngineLootTableProvider`        | Loot table JSONs  |
| `EngineRecipePrioritiesProvider` | Recipe priorities |
| `EngineBlockTagsProvider`        | Block tag JSONs   |
| `EngineItemTagsProvider`         | Item tag JSONs    |

### Client-side

| Provider                            | Generates                  |
|-------------------------------------|----------------------------|
| `EngineLanguageProvider`            | `en_us.json` language file |
| `EngineModelProvider`               | Block/item model JSONs     |
| `EngineParticleDescriptionProvider` | Particle description JSONs |
| `EngineSoundDefinitionsProvider`    | `sounds.json`              |
| `EngineEquipmentAssetProvider`      | Equipment asset data       |

## Per-builder toggle

Each builder has `generateData(boolean)` to control auto-generation:

```groovy
BlockBuilder.create(id("custom_block"))
    .generateData(false)
```

Default is `true` for all builders.

## Custom providers

```groovy
BundleEvents.dataGen { event ->
    event.addProvider(myDataProvider)
}
```

## DynamicPackRepository

Generated data is served to the game as a virtual resource/data pack, so assets are immediately available without restarting.

## Next

- [Bundles](../core-concepts/what-is-a-bundle.md) — bundle structure
- [Java Addon API](addon-api.md) — BundleDataGenEvent
