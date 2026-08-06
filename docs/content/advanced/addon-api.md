# Java Addon API

FoundryEngine exposes events and builders for Java developers through NeoForge's event bus.

## Setup

Subscribe to FoundryEngine events using `@SubscribeEvent` on the appropriate bus. All API events are in `de.luckymcdev.foundryengine.common.event`.

## TitleScreenModificationEvent

Modify or cancel title screen buttons:

```java
@SubscribeEvent
public void onTitleScreen(TitleScreenModificationEvent event) {
    if (event.getButtonType() == TitleScreenModificationEvent.ButtonType.REALMS) {
        event.setCanceled(true);
    }
}
```

## GameStageEvent

Listen to stage changes:

```java
@SubscribeEvent
public void onStageAdd(GameStageEvent.Add event) {
    if (event.getStage().getPath().equals("too_early")) {
        event.setCanceled(true);
    }
}
```

Subclasses: `Add` (cancellable), `Remove` (cancellable), `Added`, `Removed`. All expose `getStage()` (returns `Identifier`) and `getPlayer()`.

## BlockModificationEvent

Modify block properties at runtime:

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

## ItemModificationEvent

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

## BundleDataGenEvent

Add custom data providers during bundle data generation:

```java

@SubscribeEvent
public void onDataGen(BundleDataGenEvent event) {
    event.addProvider(new MyCustomProvider(
            event.getGenerator().getPackOutput(),
            event.getLookup()));
}
```

## Next

- [Events Reference](../core-concepts/events-reference.md) — full event list
- [Data Generation](data-generation.md) — how data generation works
- [Editor Themes](editor-themes.md) — custom panel and theme registration
