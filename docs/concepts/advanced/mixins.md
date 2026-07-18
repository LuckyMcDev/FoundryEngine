# Mixin architecture

FoundryEngine uses over 40 mixin classes to inject hooks into Minecraft's core systems. These patches provide the foundation for engine features from rendering and input to world management and UI.

Mixins are organized by target area in the `de.luckymcdev.foundryengine.mixin` package.

## Minecraft lifecycle

| Mixin                   | Target            | Purpose                                    |
|-------------------------|-------------------|--------------------------------------------|
| `MinecraftMixin`        | `Minecraft`       | Client lifecycle hooks, editor integration |
| `MinecraftServerMixin`  | `MinecraftServer` | Server lifecycle hooks                     |
| `MinecraftServerAccess` | `MinecraftServer` | Accessor for server internals              |

## Rendering

| Mixin                      | Target                | Purpose                                  |
|----------------------------|-----------------------|------------------------------------------|
| `LevelRendererMixin`       | `LevelRenderer`       | Custom world rendering hooks             |
| `GameRendererMixin`        | `GameRenderer`        | Post-processing, camera effects          |
| `CameraMixin`              | `Camera`              | Cutscene camera control                  |
| `CutsceneCameraMixin`      | `Camera`              | Cutscene-specific camera transformations |
| `BlockEntityRendererMixin` | `BlockEntityRenderer` | Extended block entity rendering distance |
| `ItemInHandRendererMixin`  | `ItemInHandRenderer`  | Off-hand rendering changes               |
| `RenderPipelinesInvoker`   | `RenderPipelines`     | Custom render pipeline invocation        |

## Post-Processing

| Mixin                         | Target                | Purpose                              |
|-------------------------------|-----------------------|--------------------------------------|
| `PostChainMixin`              | `PostChain`           | Custom post-processing chain         |
| `GpuDeviceMixin`              | `GpuDevice`           | GPU device integration               |
| `ShaderLoaderAccessor`        | `ShaderLoader`        | Access shader loading internals      |
| `PostEffectProcessorAccessor` | `PostEffectProcessor` | Access to effect processor internals |
| `PostEffectPassAccessor`      | `PostEffectPass`      | Access to effect pass internals      |
| `GlGpuBufferAccessor`         | `GlGpuBuffer`         | GPU buffer access for mesh rendering |

## Input

| Mixin                  | Target            | Purpose                |
|------------------------|-------------------|------------------------|
| `MouseHandlerMixin`    | `MouseHandler`    | Mouse input hooking    |
| `KeyboardHandlerMixin` | `KeyboardHandler` | Keyboard input hooking |

## World / Level

| Mixin                     | Target               | Purpose                   |
|---------------------------|----------------------|---------------------------|
| `ServerLevelMixin`        | `ServerLevel`        | Server dimension hooks    |
| `ServerChunkCacheMixin`   | `ServerChunkCache`   | Chunk loading hooks       |
| `ChunkMapMixin`           | `ChunkMap`           | Chunk tracking hooks      |
| `LevelStorageSourceMixin` | `LevelStorageSource` | Level save/load hooks     |
| `LevelStemMixin`          | `LevelStem`          | Dimension type hooks      |
| `WorldGenSettingsMixin`   | `WorldGenSettings`   | World generation settings |

## UI / Screen

| Mixin                                         | Target                        | Purpose                          |
|-----------------------------------------------|-------------------------------|----------------------------------|
| `TitleScreenMixin`                            | `TitleScreen`                 | Title screen button modification |
| `AbstractContainerScreenMixin`                | `AbstractContainerScreen`     | Container screen hooks           |
| `CreativeModeInventoryScreenSlotWrapperMixin` | `CreativeModeInventoryScreen` | Creative inventory slot wrapping |
| `ModListScreenMixin`                          | `ModListScreen`               | Mod list screen integration      |
| `ModListWidgetMixin`                          | `ModListWidget`               | Mod list widget integration      |
| `InfoPanelAccessor`                           | `InfoPanel`                   | Access info panel internals      |
| `AbstractSelectionListAccessor`               | `AbstractSelectionList`       | Access list internals            |

## Registry

| Mixin                 | Target           | Purpose               |
|-----------------------|------------------|-----------------------|
| `MappedRegistryMixin` | `MappedRegistry` | Registry freeze hooks |

## Clock

| Mixin                     | Target               | Purpose                         |
|---------------------------|----------------------|---------------------------------|
| `ServerClockManagerMixin` | `ServerClockManager` | Server clock hooks              |
| `PlayerListMixin`         | `PlayerList`         | Player list clock sync          |
| `ClockInstanceAccessor`   | `ClockInstance`      | Access clock instance internals |

## Blocks

| Mixin                 | Target           | Purpose                                                      |
|-----------------------|------------------|--------------------------------------------------------------|
| `BlockStateBaseMixin` | `BlockStateBase` | Runtime property modification via `EngineBlockStateBehavior` |
| `BlockBehaviorMixin`  | `BlockBehaviour` | Runtime property modification via `EngineBlockBehavior`      |

## Commands

| Mixin                | Target          | Purpose               |
|----------------------|-----------------|-----------------------|
| `ReloadCommandMixin` | `ReloadCommand` | Bundle reload hooks   |
| `TimeCommandMixin`   | `TimeCommand`   | Time command override |

## Slot

| Mixin       | Target | Purpose                |
|-------------|--------|------------------------|
| `SlotMixin` | `Slot` | Slot interaction hooks |

## Engine interfaces

FoundryEngine uses mixin-injected interfaces to allow runtime property modification without subclassing:

- **`EngineBlockBehavior`** -- injected into `BlockBehaviour`, provides runtime hooks for `BlockModificationEvent`
- **`EngineBlockStateBehavior`** -- injected into `BlockStateBase`, provides per-state property overrides for light emission, destroy speed, and requires-tool

These interfaces power the `BlockModificationEvent` and `ItemModificationEvent` systems, letting any block or item be modified at runtime regardless of its original implementation.

## See also

- [Addon API](addon-api) -- BlockModificationEvent, ItemModificationEvent
- [Data Generation](data-generation) -- Provider architecture
- [Events](../core/events) -- Full event reference
