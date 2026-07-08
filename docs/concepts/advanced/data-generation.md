# Data generation

FoundryEngine includes a bundle data generation system that automatically creates assets and data files from your builders. It runs during `FMLConstructModEvent` and `FMLCommonSetupEvent` phases.

## Architecture

```
BundleDataGenerator
    ├── runAll() — runs for all loaded bundles
    ├── run(Bundle) — runs for a single bundle
    └── EngineDataGenerator — wraps Minecraft's DataGenerator

EngineDataGenerator
    ├── addProvider(DataProvider) — add a data provider
    ├── getPackOutput() — get the PackOutput
    └── run() — execute all providers
```

Generated output goes to a temp directory at `<gameDir>/FoundryEngine/temp/instances/<hash>/bundles/`.

## Bundle data generation loop

For each bundle, `BundleDataGenerator.run(bundle)`:

1. Collects `BlockBuilder`, `ItemBuilder`, and `SoundBuilder` instances for the bundle's namespace
2. Filters to builders where `shouldGenerateData()` is `true`
3. Registers and runs server-side and client-side providers
4. Fires `BundleDataGenEvent` for custom provider registration

## Server providers

| Provider                          | Generates                              |
|-----------------------------------|----------------------------------------|
| `EngineAdvancementProvider`       | Advancement JSONs                      |
| `EngineLootTableProvider`         | Loot table JSONs                       |
| `EngineRecipePrioritiesProvider`  | Recipe priority data                   |
| `EngineBlockTagsProvider`         | Block tag JSONs                        |
| `EngineItemTagsProvider`          | Item tag JSONs                         |
| `EngineGlobalLootModifierProvider`| Global loot modifier JSONs             |

## Client providers

| Provider                             | Generates                                |
|--------------------------------------|------------------------------------------|
| `EngineLanguageProvider`             | `en_us.json` language file               |
| `EngineModelProvider`                | Block/item model JSONs                   |
| `EngineEquipmentAssetProvider`       | Equipment asset data                     |
| `EngineParticleDescriptionProvider`  | Particle description JSONs               |
| `EngineSoundDefinitionsProvider`     | `sounds.json` sound definitions          |

## Per-Builder Toggle

Each builder has a `generateData(boolean)` method to control whether data is generated:

```groovy
BlockBuilder.create(id("custom_block"))
    .generateData(false) // Skip data generation for this block
```

Default is `true` for all builders.

## BundleDataGenEvent

**Package:** `de.luckymcdev.foundryengine.common.event.data.BundleDataGenEvent`

Fire to add custom data providers. Uses `NeoForge.EVENT_BUS` and is available from both Java addons and Groovy scripts.

```groovy
BundleEvents.dataGen {
    it.addProvider(myDataProvider)
}
```

In Java:

```java
@SubscribeEvent
public void onDataGen(BundleDataGenEvent event) {
    event.addProvider(new MyCustomProvider(
        event.getGenerator().getPackOutput(),
        event.getLookup()));
}
```

### Event Properties

| Method                    | Returns                               |
|---------------------------|---------------------------------------|
| `getGenerator()`          | `EngineDataGenerator` instance        |
| `getLookup()`             | `CompletableFuture<HolderLookup.Provider>` |
| `addProvider(DataProvider)` | Register a custom provider           |

## DynamicPackRepository

Generated data is served to the game via `DynamicPackRepository`, which mounts the generated pack output as a virtual resource pack and data pack. This means generated models, recipes, and other assets are immediately available without restarting.

## See also

- [Bundles](../core/bundles) -- Bundle structure and lifecycle
- [Builders](../core/builders) -- Builder reference (generateData toggle)
- [Events](../core/events) -- BundleEvents.dataGen for Groovy usage
