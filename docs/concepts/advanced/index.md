# Advanced topics

This section covers advanced concepts for extending and integrating with FoundryEngine beyond bundle scripting.

## Java addon API

FoundryEngine exposes events and builders for Java developers. Use NeoForge's `@SubscribeEvent` on FoundryEngine events to register custom panels, renderers, key bindings, modify blocks/items at runtime, and hook into game stages.

[Read more ->](addon-api)

## Network packets

The built-in packet system handles communication between client and server. Packets use codec-based serialization with the `AbstractPacket` interface and `NetworkManager` registry.

[Read more ->](network)

## Editor themes

Seven built-in themes for the Dear ImGui editor, with support for custom themes via the `ImTheme` interface. Theme selection is persisted through `ClientConfig.SELECTED_THEME`.

[Read more ->](themes)

## Data generation

The bundle data generation system automatically creates assets and data (models, recipes, loot tables, tags, sounds, language) from your builders during `FMLConstructModEvent` and `FMLCommonSetupEvent`.

[Read more ->](data-generation)

## Mixin architecture

Over 40 mixin classes that patch Minecraft's internals to power FoundryEngine features. From rendering and input to world/level management and UI.

[Read more ->](mixins)
