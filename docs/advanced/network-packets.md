# Network Packets

FoundryEngine includes a custom packet system for client-server communication, built on NeoForge's payload network.

## Architecture

```
AbstractPacket<T> (CustomPacketPayload)
    └── Definition<T> record
        ├── Type<T>        — packet ID
        ├── PacketBounds   — CLIENT, SERVER, or BOTH
        ├── StreamCodec     — serialization
        ├── clientHandler  — handles on client
        └── serverHandler  — handles on server

NetworkManager
    ├── register(Definition) — register a packet type
    ├── sendToServer(T)      — send from client to server
    ├── sendToPlayer(T, ServerPlayer) — send to specific player
    └── sendToAllPlayers(T)  — broadcast
```

## Creating a custom packet

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

## CustomDataPacket

A generic packet carrying a `String` ID and `CompoundTag` payload — useful for ad-hoc communication:

```java
var packet = new CustomDataPacket("my_feature", someTag);
Common.getNetworkManager().sendToServer(packet);

// Server to player
Common.getNetworkManager().sendToPlayer(packet, player);

// Broadcast
Common.getNetworkManager().sendToAllPlayers(packet);
```

Receiving:

```java
NeoForge.EVENT_BUS.addListener(CustomDataReceivedEvent.class, event -> {
    if (event.getId().equals("my_feature")) {
        CompoundTag data = event.getData();
        // handle
    }
});
```

## Sending from scripts

```groovy
def network = Common.getNetworkManager()
network.sendToServer(new MyPacket(42))
network.sendToPlayer(somePacket, player)
network.sendToAllPlayers(broadcastPacket)
```

## Next

- [Events Reference](../core-concepts/events-reference.md) — NetworkEvents
- [Java Addon API](addon-api.md) — Java integration
