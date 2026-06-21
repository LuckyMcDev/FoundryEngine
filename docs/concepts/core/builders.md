# Builders

Builders are the primary way to create and register game content in FoundryEngine. Each builder wraps a NeoForge registry and provides a fluent API.

All builders extend `AbstractBuilder<T>` (`de.luckymcdev.foundryengine.common.builder.AbstractBuilder`):

| Method | Description |
|---|---|
| `build()` | Build the object without registering it |
| `get()` | Get the registered object (throws if not registered yet) |
| `getOrCreate()` | Get the registered object, or build if not yet registered |
| `getId()` | The builder's `Identifier` |
| `newID(pre, post)` | Create a new identifier with prefix/suffix |
| `shouldGenerateData()` | Whether data is auto-generated (default: `true`) |

---

## ItemBuilder

`de.luckymcdev.foundryengine.common.builder.item.ItemBuilder`

Creates custom `Item` instances. The default factory produces `EngineItem`, which supports callback-based event hooks.

### Static Factory

```groovy
ItemBuilder.create(Identifier.fromNamespaceAndPath("mybundle", "my_item"))
```

### Configuration Methods

All methods return `ItemBuilder` for fluent chaining.

| Method | Description |
|---|---|
| `factory(Function<Properties, Item>)` | Custom item factory (default: `EngineItem::new`) |
| `properties(UnaryOperator<Properties>)` | Modify the item properties |
| `stacksTo(int)` | Max stack size |
| `fireResistant()` | Item is immune to fire/lava |
| `component(DataComponentType, T)` | Add a data component |
| `component(String, T)` | Add a data component by string key |
| `generateData(boolean)` | Toggle auto data generation |

### Callback Hooks

```groovy
ItemBuilder.create(id("wand"))
    .use { level, player, hand ->
        player.sendSystemMessage(Component.literal("Whoosh!"))
        return InteractionResult.SUCCESS
    }
    .useOn { context ->
        println "Used on ${context.pos}"
        return InteractionResult.SUCCESS
    }
    .inventoryTick { stack, level, owner, slot ->
        if (level.gameTime % 20 == 0) {
            // Per-second effect while in inventory
        }
    }
    .hurtEnemy { stack, mob, attacker ->
        mob.setSecondsOnFire(3)
    }
```

| Method | Callback Type | Parameters | Returns |
|---|---|---|---|
| `use(cb)` | `UseCallback` | `Level, Player, InteractionHand` | `InteractionResult` |
| `useOn(cb)` | `UseOnCallback` | `UseOnContext` | `InteractionResult` |
| `onUseTick(cb)` | `OnUseTickCallback` | `Level, LivingEntity, ItemStack, int ticksRemaining` | `void` |
| `finishUsingItem(cb)` | `FinishUsingItemCallback` | `ItemStack, Level, LivingEntity` | `ItemStack` |
| `hurtEnemy(cb)` | `HurtEnemyCallback` | `ItemStack, LivingEntity mob, LivingEntity attacker` | `void` |
| `postHurtEnemy(cb)` | `PostHurtEnemyCallback` | `ItemStack, LivingEntity mob, LivingEntity attacker` | `void` |
| `inventoryTick(cb)` | `InventoryTickCallback` | `ItemStack, ServerLevel, Entity, EquipmentSlot` | `void` |
| `releaseUsing(cb)` | `ReleaseUsingCallback` | `ItemStack, Level, LivingEntity, int remainingTime` | `boolean` |
| `onCraftedPostProcess(cb)` | `OnCraftedPostProcessCallback` | `ItemStack, Level` | `void` |

### Complete Example

```groovy
ItemBuilder.create(id("my_item"))
    .component(DataComponents.LORE, new ItemLore(List.of(
        Component.literal("A legendary blade,"),
        Component.literal("forged in the deeps.")
    )))
    .component(DataComponents.RARITY, Rarity.UNCOMMON)
    .stacksTo(3)

ItemBuilder.create(id("cosmic_apple"))
    .component(DataComponents.RARITY, Rarity.EPIC)
    .component(DataComponents.FOOD, new FoodProperties.Builder()
        .nutrition(4).saturationModifier(0.3f).alwaysEdible().build())
    .component(DataComponents.CONSUMABLE, Consumables.defaultFood().build())
    .stacksTo(16)
```

---

## BlockBuilder

`de.luckymcdev.foundryengine.common.builder.block.BlockBuilder`

Creates custom `Block` instances (default: `EngineBlock`). Optionally creates a corresponding `BlockItem`.

### Static Factory

```groovy
BlockBuilder.create(Identifier.fromNamespaceAndPath("mybundle", "my_block"))
```

### Configuration Methods

| Method | Description |
|---|---|
| `factory(Function<BlockBehaviour.Properties, Block>)` | Custom block factory |
| `properties(UnaryOperator<Properties>)` | Modify block properties |
| `noItem()` | Skip block item creation |
| `itemProperties(UnaryOperator<Item.Properties>)` | Modify block item properties |
| `generateData(boolean)` | Toggle auto data generation |

### Block Callback Hooks

```groovy
BlockBuilder.create(id("magic_block"))
    .stepOn { level, pos, onState, entity ->
        entity.setSecondsOnFire(3)
    }
    .destroy { level, pos, state ->
        println "Block destroyed at $pos"
    }
    .setPlacedBy { level, pos, state, by, itemStack ->
        println "Placed by ${by?.name}"
    }
```

| Method | Callback Type | Parameters | Returns |
|---|---|---|---|
| `animateTick(cb)` | `AnimateTickCallback` | `BlockState, Level, BlockPos, RandomSource` | `void` |
| `destroy(cb)` | `DestroyCallback` | `LevelAccessor, BlockPos, BlockState` | `void` |
| `wasExploded(cb)` | `WasExplodedCallback` | `ServerLevel, BlockPos, Explosion` | `void` |
| `stepOn(cb)` | `StepOnCallback` | `Level, BlockPos, BlockState, Entity` | `void` |
| `setPlacedBy(cb)` | `SetPlacedByCallback` | `Level, BlockPos, BlockState, LivingEntity, ItemStack` | `void` |
| `fallOn(cb)` | `FallOnCallback` | `Level, BlockState, BlockPos, Entity, double fallDistance` | `void` |
| `playerWillDestroy(cb)` | `PlayerWillDestroyCallback` | `Level, BlockPos, BlockState, Player` | `BlockState` |
| `playerDestroy(cb)` | `PlayerDestroyCallback` | `Level, Player, BlockPos, BlockState, BlockEntity, ItemStack` | `void` |
| `handlePrecipitation(cb)` | `HandlePrecipitationCallback` | `BlockState, Level, BlockPos, Biome.Precipitation` | `void` |

### Item Callback Hooks on Blocks

BlockBuilder also exposes item callbacks that apply to the block's item form:

| Method | Description |
|---|---|
| `itemUse(cb)` | When the block item is used |
| `itemUseOn(cb)` | When the block item is used on something |
| `itemInventoryTick(cb)` | Inventory tick for the block item |
| `itemFinishUsing(cb)` | When the block item is consumed |
| `itemHurtEnemy(cb)` | When the block item hurts an enemy |
| `itemPostHurtEnemy(cb)` | When the block item hurts an enemy (post) |
| `itemReleaseUsing(cb)` | When use is released |
| `itemOnCraftedPostProcess(cb)` | After the block item is crafted |

### Complete Example

```groovy
BlockBuilder.create(id("my_block"))
    .properties { it.strength(2.0f, 3.0f) }
    .itemProperties { it.rarity(Rarity.COMMON) }

BlockBuilder.create(id("my_slab"))
    .factory { new SlabBlock(it) }
    .properties { it.noOcclusion().lightLevel { 15 } }

BlockBuilder.create(id("invisible_wall"))
    .noItem()
    .properties { it.noCollision().strength(-1.0f, 3600000.0f) }
    .generateData(false)

BlockBuilder.create(id("hot_plate"))
    .stepOn { level, pos, onState, entity ->
        if (entity instanceof LivingEntity) {
            entity.hurt(entity.damageSources().hotFloor(), 1.0f)
        }
    }
```

---

## RecipeBuilder

`de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder`

Creates all recipe types. Uses a `RecipeResult` record that pairs a recipe ID with a save function.

### Static Factories

| Factory | Description |
|---|---|
| `RecipeBuilder.shaped(id, result)` | Shaped crafting recipe |
| `RecipeBuilder.shapeless(id, result)` | Shapeless crafting recipe |
| `RecipeBuilder.smelting(id, result)` | Furnace smelting |
| `RecipeBuilder.blasting(id, result)` | Blast furnace |
| `RecipeBuilder.smoking(id, result)` | Smoker |
| `RecipeBuilder.campfireCooking(id, result)` | Campfire cooking |
| `RecipeBuilder.stonecutting(id, result)` | Stonecutter |
| `RecipeBuilder.smithingTransform(id, result)` | Smithing table transform |
| `RecipeBuilder.smithingTrim(id)` | Smithing table trim |

### Universal Methods

| Method | Description |
|---|---|
| `count(int)` | Result count (default: 1) |
| `category(RecipeCategory)` | Recipe category |
| `group(String)` | Recipe group |
| `unlockedBy(String, Criterion)` | Unlock criterion |
| `generateData(boolean)` | Toggle auto data generation |

### Shaped Recipe

| Method | Description |
|---|---|
| `pattern(String...)` | Crafting pattern (3 lines max) |
| `define(char, ItemLike)` | Define a pattern key |
| `define(char, Ingredient)` | Define a pattern key as ingredient |

### Shapeless Recipe

| Method | Description |
|---|---|
| `requires(ItemLike)` | Add an ingredient |
| `requires(ItemLike, int)` | Add multiple of an ingredient |
| `requires(Ingredient)` | Add an ingredient |
| `requires(Ingredient, int)` | Add multiple of an ingredient |

### Cooking Recipes (smelting, blasting, smoking, campfireCooking)

| Method | Description |
|---|---|
| `ingredient(ItemLike)` | The item to cook |
| `ingredient(Ingredient)` | The ingredient to cook |
| `experience(float)` | XP rewarded (default: 0.1) |
| `cookingTime(int)` | Ticks to cook (default: 200) |

### Stonecutting

| Method | Description |
|---|---|
| `ingredient(ItemLike)` | Input item |
| `ingredient(Ingredient)` | Input ingredient |

### Smithing (transform & trim)

| Method | Description |
|---|---|
| `base(ItemLike)` | Base item |
| `base(Ingredient)` | Base ingredient |
| `addition(ItemLike)` | Addition item |
| `addition(Ingredient)` | Addition ingredient |
| `template(ItemLike)` | Smithing template |
| `template(Ingredient)` | Smithing template ingredient |

### Complete Examples

```groovy
RecipeBuilder.shaped(id("test_shaped"), Items.DIAMOND_SWORD)
    .pattern(" D ", " D ", " S ")
    .define('D' as char, Items.DIAMOND)
    .define('S' as char, Items.STICK)
    .category(RecipeCategory.COMBAT)
    .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))

RecipeBuilder.shapeless(id("test_shapeless"), Items.FLINT_AND_STEEL)
    .requires(Items.IRON_INGOT)
    .requires(Items.FLINT)
    .unlockedBy("has_iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))

RecipeBuilder.smelting(id("test_smelting"), Items.IRON_INGOT)
    .ingredient(Items.IRON_ORE)
    .experience(0.7f)
    .cookingTime(200)
    .unlockedBy("has_ore", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_ORE))

RecipeBuilder.smithingTransform(id("test_smithing"), Items.NETHERITE_SWORD)
    .base(Items.DIAMOND_SWORD)
    .addition(Items.NETHERITE_INGOT)
    .template(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
    .unlockedBy("has_netherite", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
```

---

## SoundBuilder

`de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder`

Creates custom `SoundEvent` instances with sound file definitions.

### Static Factory

```groovy
SoundBuilder.create(Identifier.fromNamespaceAndPath("mybundle", "my_sound"))
```

### Methods

| Method | Description |
|---|---|
| `range(float)` | Fixed range for the sound |
| `subtitle(String)` | Subtitle text (shown in settings) |
| `replace(boolean)` | Whether to replace existing sounds |
| `addSound(Identifier)` | Add a sound file at default settings |
| `addSound(Identifier, float, float, int, boolean, int, boolean)` | Add with full control |
| `generateData(boolean)` | Toggle auto data generation |

The full `addSound` parameters are: `location`, `volume`, `pitch`, `weight`, `stream`, `attenuationDistance`, `preload`.

### Complete Example

```groovy
SoundBuilder.create(id("my_music"))
    .subtitle("My Custom Music")
    .addSound(id("music.my_music"))
    .range(16.0f)

SoundBuilder.create(id("explosion"))
    .addSound(id("explosion_1"), 1.0f, 1.0f, 3, false, 16, false)
    .addSound(id("explosion_2"), 1.0f, 1.0f, 2, false, 16, false)
    .replace(true)
```

---

## ParticleBuilder

`de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder`

Creates custom `ParticleType` instances with keyframe-driven animation data.

### Static Factory

```groovy
ParticleBuilder.create(Identifier.fromNamespaceAndPath("mybundle", "my_particle"))
```

### Methods

| Method | Description |
|---|---|
| `factory(Function<Boolean, ParticleType>)` | Custom particle type factory |
| `alwaysShow()` | Always show (ignore particle settings) |
| `lifetime(int)` | Particle lifetime in ticks (default: 20) |
| `layer(ParticleLayer)` | Render layer (`OPAQUE` or `TRANSLUCENT`) |
| `colorData(ParticleColorData)` | Full color data control |
| `color(Color)` | Single color |
| `color(Color, Color, Easing)` | Color transition |
| `scaleData(ParticleScaleData)` | Full scale data control |
| `scale(float)` | Fixed scale |
| `velocityData(ParticleVelocityData)` | Full velocity data control |
| `velocity(Vector3d)` | Fixed velocity |
| `positionData(ParticlePositionData)` | Full position data control |
| `position(Vector3d)` | Fixed position |
| `rotationData(ParticleRotationData)` | Full rotation data control |
| `rotation(float)` | Fixed rotation |
| `rotation(float, float, Easing)` | Rotation animation |
| `generateData(boolean)` | Toggle auto data generation |

### ParticleLayer

Enum with values: `OPAQUE`, `TRANSLUCENT`

### Complete Example

```groovy
ParticleBuilder.create(id("sparkle"))
    .alwaysShow()
    .lifetime(30)
    .layer(ParticleLayer.TRANSLUCENT)
    .color(Color.WHITE, Color.RED, Easing.SINE_IN)
    .scale(0.5f, 1.5f, Easing.SINE_OUT)
    .velocity(new Vector3d(0.0, 0.1, 0.0))
```

---

## Registration

All builders are registered through `BundleEvents.registry`:

```groovy
BundleEvents.registry {
    it.items(myItem, anotherItem)
    it.blocks(myBlock, anotherBlock)
    it.recipes(shapedRecipe, shapelessRecipe)
    it.sounds(mySound)
    it.particles(myParticle)
}
```

## Data Generation

By default, each builder auto-generates asset/data files (models, recipes, blockstates, sound JSONs, etc.). You can disable this per-builder:

```groovy
BlockBuilder.create(id("custom_block"))
    .generateData(false)
```

## See Also

- [Registries](registries) — How builders interact with the registry system
- [Events](events) — Adding behavior via event hooks
