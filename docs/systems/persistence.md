# Saved Data & Persistence

FoundryEngine keeps its engine data (areas, cutscenes, dialogue, waypoints, and client preferences) in **NBT files** and syncs it between server and client. The `SavedDataManager` is the single point that owns loading, saving, and broadcasting.

## Storage layout

| Data                                                            | Location                                       |
|-----------------------------------------------------------------|------------------------------------------------|
| Server-managed sections (areas, cutscenes, dialogue, waypoints) | `<level>/foundryengine/engine.dat`             |
| Client-local sections (e.g. editor hotkeys)                     | `engine.dat` in the mod's global config folder |

On the first load of a world, the old global file is **imported once** into the new per-world location, so existing data survives upgrades.

## Sections

Each manager stores itself under its own NBT section key inside the shared file. The `SavedDataManager` just orchestrates the file:

- `getSection(key)` / `setSection(key, tag)` — read/write one section
- `setData(tag)` — replace the whole store
- `syncToAll()` / `syncToPlayer(player)` — request a broadcast of the full store to clients

## Deferred writes

Writes and broadcasts are **deferred**:

- Mutations only mark the store dirty.
- A tick listener flushes to disk at most every `100` ticks.
- All pending sync requests are coalesced into a **single** sync packet per tick.

This keeps the hot path cheap — no per-edit IO.

## Access from code

```groovy
def data = Common.getSavedDataManager()
data.setSection("my_section", myCompoundTag)
data.syncToAll()
```

You rarely interact with `SavedDataManager` directly — the managers expose typed APIs and call it internally:

- `Common.getAreaManager()` — `save()` / `load()`
- `Common.getCutsceneManager()` — cutscene sessions
- `Common.getDialogueManager()` — dialogue trees
- `Common.getWaypointManager()` — waypoints

## Related

- [Areas](areas.md), [Waypoints](waypoints.md), [Cutscenes](cutscenes.md), [Dialogue](dialogue.md) — the systems that use persistence