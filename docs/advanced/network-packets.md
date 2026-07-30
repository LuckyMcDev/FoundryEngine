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
    public static final Definition<MyPacket> DEFINITION = new Definition<>(
        AbstractPacket.createType(Common.id("my_packet")),
        PacketBounds.SERVER,
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MyPacket::value,
            MyPacket::new),
        null,
        MyPacket::handleServer
    );

    @Override
    public Type<MyPacket> getType() { return DEFINITION.type(); }

    @Override
    public PacketBounds getBoundTo() { return DEFINITION.bounds(); }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MyPacket> getCodec() { return DEFINITION.codec(); }

    @Override
    public void handleServer(IPayloadContext ctx) {
        // Handle on server
    }
}
```

Registration:

```java
NetworkManager network = Common.getNetworkManager();
network.register(MyPacket.DEFINITION);
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
