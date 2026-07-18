# Network packets

FoundryEngine includes a custom packet system built on NeoForge's payload network. Packets are registered through `NetworkManager` and use codec-based serialization via `StreamCodec<RegistryFriendlyByteBuf, T>`.

## Architecture

```
AbstractPacket<T> (CustomPacketPayload)
    └── Definition<T> record
        ├── Type<T>        — packet ID
        ├── PacketBounds   — CLIENT, SERVER, or BOTH
        ├── StreamCodec     — serialization
        ├── clientHandler  — BiConsumer<T, IPayloadContext>
        └── serverHandler  — BiConsumer<T, IPayloadContext>

NetworkManager
    ├── register(Definition) — register a packet type
    ├── sendToServer(T)      — send from client to server
    ├── sendToPlayer(T, ServerPlayer) — send to specific player
    └── sendToAllPlayers(T)  — broadcast to all players
```

### PacketBounds

Each packet declares its direction:

- `CLIENT` — sent server-to-client
- `SERVER` — sent client-to-server
- `BOTH` — sent in either direction

## Creating a Custom Packet

```java
public record MyPacket(int value) implements AbstractPacket<MyPacket> {
    public static final Type<MyPacket> TYPE = AbstractPacket.createType(
        Common.id("my_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MyPacket> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MyPacket::value,
            MyPacket::new);

    @Override
    public Type<MyPacket> getType() { return TYPE; }

    @Override
    public PacketBounds getBoundTo() { return PacketBounds.SERVER; }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MyPacket> getCodec() { return CODEC; }

    @Override
    public void handleServer(IPayloadContext ctx) {
        // Handle on server
    }
}
```

Registration:

```java
NetworkManager network = Common.getNetworkManager();
network.register(new AbstractPacket.Definition<>(
    MyPacket.TYPE, MyPacket.PACKET_BOUNDS, MyPacket.CODEC,
    null, MyPacket::handleServer));
```

## Packet Types

### Bundle Sync

- **BundleHashPacket** — syncs bundle hashes between client and server for bundle validation

### Editor

- **AreaPacket** — synchronizes area zone data (create, update, delete) between editor and server
- **CutscenePacket** — synchronizes cutscene data between the cutscene editor and server
- **CutsceneCommandPacket** — commands triggered during cutscene playback
- **CutsceneAction** — action model for cutscene editor operations
- **LinearizeCutscenePacket** — linearizes a cutscene's keyframes into a flat timeline
- **WaypointPacket** — synchronizes waypoint data
- **GiveItemPacket** — editor gives an item to a player

### Dialogue

- **ClientboundDialoguePacket** — dialogue session state sent server→client
- **ServerboundDialoguePacket** — player dialogue action sent client→server
- **DialogueSavePacket** — saves dialogue trees from the editor to the server

### File Explorer

- **ClientBoundFileContentPacket** — file content sent from server to client
- **ClientBoundFileListPacket** — directory listing sent from server to client
- **ServerBoundRequestFileContentPacket** — request file content from server
- **ServerBoundRequestFileListPacket** — request directory listing from server
- **ServerBoundSaveFilePacket** — save file content to server

### Sync

- **SavedDataSyncPacket** — synchronizes saved data between client and server
- **ScreenEffectPacket** — triggers screen effects on the client

### World

- **ServerBoundChangeWeatherPacket** — client requests weather change
- **ServerBoundSetTimePacket** — client requests time change
- **ServerBoundSpawnEntityPacket** — client requests entity spawning
- **ServerBoundTeleportPacket** — client requests player teleportation

### Custom Data

**CustomDataPacket** — a generic bidirectional packet carrying a `String` ID and a `CompoundTag` payload. Useful for ad-hoc communication without writing a dedicated packet class.

```java
// Sending (client -> server)
var packet = new CustomDataPacket("my_feature", someTag);
Common.

getNetworkManager().

sendToServer(packet);

// Sending (server -> client)
Common.

getNetworkManager().

sendToPlayer(packet, player);
Common.

getNetworkManager().

sendToAllPlayers(packet);
```

Receiving posts a **CustomDataReceivedEvent** on the NeoForge event bus:

```java
NeoForge.EVENT_BUS.addListener(CustomDataReceivedEvent .class, event ->{
        if(event.

getId().

equals("my_feature")){
CompoundTag data = event.getData();
// handle
    }
            });
```

The event is also bridgeable through `NetworkEvents`:

```java
NetworkEvents.onCustomDataReceived(event ->{
        });
```

NeoForge `PlayerEvent` subevents use `player.level().isClientSide()` instead — see [Side checking](../core/events#side-checking).

| Packet Field | Type          | Description          |
|--------------|---------------|----------------------|
| `id`         | `String`      | Arbitrary identifier |
| `data`       | `CompoundTag` | NBT payload          |

| Event Property | Type          | Description                |
|----------------|---------------|----------------------------|
| `getPlayer()`  | `Player`      | The player involved        |
| `getId()`      | `String`      | The custom data identifier |
| `getData()`    | `CompoundTag` | The NBT payload            |

## Usage

Sending packets from scripts uses `Common.getNetworkManager()`:

```groovy
def network = Common.getNetworkManager()
network.sendToServer(new MyPacket(42))
network.sendToPlayer(somePacket, player)
network.sendToAllPlayers(broadcastPacket)
```

## See also

- [Events](../core/events) -- NetworkEvents for connection lifecycle
- [Editor](../systems/editor) -- Editor packets for panel synchronization
