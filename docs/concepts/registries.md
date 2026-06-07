# Concepts: Registries

A Registry is how the game knows about your custom content. Foundry Engine wraps
NeoForge's registry system with [Builders](builders.md) — fluent classes that make
registration easy.

You register builders inside `BundleEvents.registry`:

```groovy
ItemBuilder builder = ItemBuilder.create(id("my_item"))

BundleEvents.registry {
    it.items(builder)
    it.blocks(blockBuilder)
    it.recipes(recipeBuilder)
    it.sounds(soundBuilder)
    it.particles(particleBuilder)
}
```

You register the **builder object**, not the raw output. The engine handles
registration with NeoForge internally. Use `builder.get()` to retrieve the
registered object after registration is complete.

## Available Registry Methods

| Method                             | Registers                             |
|------------------------------------|---------------------------------------|
| `it.items(ItemBuilder...)`         | Custom items                          |
| `it.blocks(BlockBuilder...)`       | Custom blocks (and their block items) |
| `it.recipes(RecipeBuilder...)`     | All recipe types                      |
| `it.sounds(SoundBuilder...)`       | Sound events                          |
| `it.particles(ParticleBuilder...)` | Particle types                        |

## See Also

- [Builders](builders.md) — Full builder API reference
- [Events](events.md) — More about `BundleEvents.registry`