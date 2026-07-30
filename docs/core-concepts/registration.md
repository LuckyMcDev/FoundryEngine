# Registration

Registration is how the game learns about your custom content. FoundryEngine handles most of this for you.

## How it works

You create builders (ItemBuilder, BlockBuilder, etc.) and register them through `BundleEvents.registry`:

```groovy
def myItem = ItemBuilder.create(id("my_item"))
def myBlock = BlockBuilder.create(id("my_block"))

BundleEvents.registry {
    it.items(myItem)
    it.blocks(myBlock)
}
```

The engine takes care of the rest — it registers your content with NeoForge's registry system internally.

## What you can register

| Method                                     | What it registers                     |
|--------------------------------------------|---------------------------------------|
| `it.items(ItemBuilder...)`                 | Custom items                          |
| `it.blocks(BlockBuilder...)`               | Custom blocks (and their block items) |
| `it.recipes(RecipeBuilder...)`             | Recipes of all types                  |
| `it.sounds(SoundBuilder...)`               | Sound events                          |
| `it.particles(ParticleBuilder...)`         | Particle types                        |
| `it.blockEntities(BlockEntityBuilder...)`  | Block entity types                    |
| `it.toolMaterials(ToolMaterialBuilder...)` | Tool materials                        |
| `it.tags(TagBuilder<?>...)`                | Block/item tags                       |

## Getting the registered object

After registration, use `builder.get()` to retrieve the registered object:

```groovy
def myItem = ItemBuilder.create(id("my_item"))
BundleEvents.registry { it.items(myItem) }

// Later, get the actual Item:
def item = myItem.get()
```

## Auto data generation

By default, each builder generates asset and data files automatically (models, recipes, blockstates, sound JSONs). You can disable this:

```groovy
BlockBuilder.create(id("custom_block"))
    .generateData(false)
```

## Advanced registration

Beyond the common methods above, `RegistryEvent` also supports:

| Method                                                  | What it registers                          |
|---------------------------------------------------------|--------------------------------------------|
| `it.recipes(RecipeBuilder...)`                          | Crafting, smelting, smithing, stonecutting |
| `it.blockEntities(BlockEntityBuilder...)`               | Block entity types for blocks              |
| `it.toolMaterials(ToolMaterialBuilder...)`              | Custom tool materials                      |
| `it.tags(TagBuilder<?>...)`                             | Block and item tag definitions             |
| `it.register(ResourceKey<Registry<T>>, RegisterHelper)` | Any NeoForge registry directly             |

## Next

- [Creating Items](creating-items.md) — build items to register
- [Creating Blocks](creating-blocks.md) — build blocks to register
