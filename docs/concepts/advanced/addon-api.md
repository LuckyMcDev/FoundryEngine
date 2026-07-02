# Java Addon API

FoundryEngine exposes events and builders for Java addon developers through NeoForge's event bus. All API events are in the package `de.luckymcdev.foundryengine.common.event` and builders in `de.luckymcdev.foundryengine.common.builder`. Subscribe using `@SubscribeEvent` on the appropriate bus.

## TitleScreenModifyEvent

**Package:** `de.luckymcdev.foundryengine.common.event.modification.TitleScreenModifyEvent`

`ICancellableEvent` — fired when title screen buttons are being created. Can cancel `SINGLEPLAYER`, `MULTIPLAYER`, or `REALMS` buttons.

```java
@SubscribeEvent
public void onTitleScreen(TitleScreenModifyEvent event) {
    if (event.getButtonType() == TitleScreenModifyEvent.ButtonType.REALMS) {
        event.setCanceled(true);
    }
}
```

## GameStageEvent

**Package:** `de.luckymcdev.foundryengine.common.game.stage.GameStageEvent`

Abstract event — listen to subclasses:

| Subclass                    | Cancellable | When                              |
|-----------------------------|-------------|-----------------------------------|
| `GameStageEvent.Add`        | Yes         | Before a stage is added           |
| `GameStageEvent.Remove`     | Yes         | Before a stage is removed         |
| `GameStageEvent.Added`      | No          | After a stage has been added      |
| `GameStageEvent.Removed`    | No          | After a stage has been removed    |

All four expose `getStageName()` and `getPlayer()`.

## BlockModificationEvent

**Package:** `de.luckymcdev.foundryengine.common.event.modification.BlockModificationEvent`

Modify block properties at runtime using a builder pattern. Methods chain together:

```java
@SubscribeEvent
public void onBlockMod(BlockModificationEvent event) {
    event.hasCollision(false)
        .explosionResistance(1000.0f)
        .lightEmission(15)
        .soundType(SoundType.AMETHYST)
        .friction(0.1f)
        .speedFactor(0.5f)
        .jumpFactor(2.0f)
        .randomlyTicking(true)
        .setDestroySpeed(0.5f)
        .setRequiresTool(true);
}
```

| Method                    | Description                     |
|---------------------------|---------------------------------|
| `hasCollision(boolean)`   | Enable/disable block collision  |
| `explosionResistance(float)` | Set explosion resistance     |
| `lightEmission(int)`      | Set light level (0-15)          |
| `soundType(SoundType)`    | Set sound type                  |
| `friction(float)`         | Set friction coefficient        |
| `speedFactor(float)`      | Set speed factor                |
| `jumpFactor(float)`       | Set jump factor                 |
| `randomlyTicking(boolean)`| Enable/disable random ticks     |
| `setDestroySpeed(float)`  | Set destroy speed               |
| `setRequiresTool(boolean)`| Whether a tool is required      |

## ItemModificationEvent

**Package:** `de.luckymcdev.foundryengine.common.event.modification.ItemModificationEvent`

Modify item properties at runtime:

```java
@SubscribeEvent
public void onItemMod(ItemModificationEvent event) {
    event.setMaxStackSize(16)
        .setMaxDamage(250)
        .setUnbreakable()
        .setFood(new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.3f).build())
        .setTool(new Tool(...))
        .setAttributeModifiers(ItemAttributeModifiers.EMPTY);
}
```

| Method                                      | Description                  |
|---------------------------------------------|------------------------------|
| `setMaxStackSize(int)`                      | Override max stack size      |
| `setMaxDamage(int)`                         | Override durability          |
| `setUnbreakable()`                          | Make item unbreakable        |
| `setFood(FoodProperties)`                   | Override food properties     |
| `setTool(Tool)`                             | Override tool properties     |
| `setAttributeModifiers(ItemAttributeModifiers)` | Override attribute modifiers |

## BundleDataGenEvent

**Package:** `de.luckymcdev.foundryengine.common.event.data.BundleDataGenEvent`

Add custom data providers during bundle data generation:

```java
@SubscribeEvent
public void onDataGen(BundleDataGenEvent event) {
    event.addProvider(new MyCustomDataProvider(
        event.getGenerator().getPackOutput(),
        event.getLookup()));
}
```

## See Also

- [Events](../core/events) — Full event reference for all API events
- [Builders](../core/builders) — Builder reference for creating content
- [Editor](../systems/editor) — Panel registration and editor extension
- [Data Generation](data-generation) — Bundle data generation system
