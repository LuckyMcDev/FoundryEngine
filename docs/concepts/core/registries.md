# Registries

A registry is how the game knows about your custom content. FoundryEngine wraps NeoForge's registry system with [Builders](builders) — fluent classes that make registration easy.

You register builders inside `BundleEvents.registry`:

```groovy
ItemBuilder builder = ItemBuilder.create(id("my_item"))

BundleEvents.registry {
    it.items(builder)
    it.blocks(blockBuilder)
    it.sounds(soundBuilder)
    it.particles(particleBuilder)
}
```

You register the **builder object**, not the raw output. The engine handles registration with NeoForge internally. Use `builder.get()` to retrieve the registered object after registration is complete.

## Available Registry Methods

| Method | Registers |
|---|---|
| `it.items(ItemBuilder...)` | Custom items |
| `it.blocks(BlockBuilder...)` | Custom blocks (and their block items) |
| `it.sounds(SoundBuilder...)` | Sound events |
| `it.particles(ParticleBuilder...)` | Particle types |

## See also

- [Builders](builders) -- Full builder API reference
- [Events](events) -- More about `BundleEvents.registry`
