# Mixin Architecture

FoundryEngine uses over 40 mixin classes to patch Minecraft's internals. These provide the foundation for features like rendering hooks, post-processing, camera control, and more.

Mixins are in the `de.luckymcdev.foundryengine.mixin` package.

## Minecraft lifecycle

| Mixin                  | Target            | Purpose                        |
|------------------------|-------------------|--------------------------------|
| `MinecraftMixin`       | `Minecraft`       | Client lifecycle hooks, editor |
| `MinecraftServerMixin` | `MinecraftServer` | Server lifecycle hooks         |

## Rendering

| Mixin                     | Target               | Purpose                 |
|---------------------------|----------------------|-------------------------|
| `LevelRendererMixin`      | `LevelRenderer`      | Custom world rendering  |
| `GameRendererMixin`       | `GameRenderer`       | Post-processing, camera |
| `CameraMixin`             | `Camera`             | Cutscene camera control |
| `ItemInHandRendererMixin` | `ItemInHandRenderer` | Off-hand rendering      |

## Post-processing

| Mixin                  | Target         | Purpose                      |
|------------------------|----------------|------------------------------|
| `PostChainMixin`       | `PostChain`    | Custom post-processing chain |
| `GpuDeviceMixin`       | `GpuDevice`    | GPU device integration       |
| `ShaderLoaderAccessor` | `ShaderLoader` | Shader loading access        |

## World / Level

| Mixin                     | Target               | Purpose                |
|---------------------------|----------------------|------------------------|
| `ServerLevelMixin`        | `ServerLevel`        | Server dimension hooks |
| `ServerChunkCacheMixin`   | `ServerChunkCache`   | Chunk loading hooks    |
| `LevelStorageSourceMixin` | `LevelStorageSource` | Level save/load hooks  |

## UI

| Mixin                          | Target                    | Purpose             |
|--------------------------------|---------------------------|---------------------|
| `TitleScreenMixin`             | `TitleScreen`             | Button modification |
| `AbstractContainerScreenMixin` | `AbstractContainerScreen` | Container hooks     |

## Blocks

| Mixin                 | Target           | Purpose                       |
|-----------------------|------------------|-------------------------------|
| `BlockStateBaseMixin` | `BlockStateBase` | Runtime property modification |
| `BlockBehaviorMixin`  | `BlockBehaviour` | Runtime behavior hooks        |

## Engine interfaces

FoundryEngine injects interfaces into Minecraft classes for runtime modification:

- **`EngineBlockBehavior`** — injected into `BlockBehaviour` for `BlockModificationEvent`
- **`EngineBlockStateBehavior`** — injected into `BlockStateBase` for per-state property overrides

These power the runtime modification system, letting any block be modified without subclassing.

## Next

- [Java Addon API](addon-api.md) — BlockModificationEvent, ItemModificationEvent
- [Data Generation](data-generation.md) — provider architecture
