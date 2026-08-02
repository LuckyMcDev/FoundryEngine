# Agent Reference — Performance

Read this file when working on performance-sensitive code. Key rule: never block the main thread; use the NeoForge event bus efficiently.

## Event Handling

- Post events efficiently (avoid expensive operations in event handlers).
- Use `EventPriority.LOWEST` for late modifications.
- Clear events with `Common.clearEvents()` after processing.

## Network

- Use packet bounds to validate packets.
- Sync data only when needed.
- Consider batching for large packets.

## Bundle Processing

- Bundles loaded in `onConstruct` — keep scripts fast.
- Avoid heavy operations during `commonSetup`.
- Use caching for expensive operations.

## Rendering

- ImGui panels should not block main thread.
- Offload heavy calculations to background threads.
- Use frame limits for animations.
