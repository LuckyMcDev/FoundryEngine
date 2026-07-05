---
name: network-packet-development
description: Standardized workflow for creating network packets in FoundryEngine. Covers packet creation, registration, bounds checking, syncing, and testing. Uses IntelliJ MCP tools throughout.
---

# Network Packet Development Workflow

## Packet Structure
```
src/main/java/de/luckymcdev/foundryengine/common/network/packets/
├── AbstractPacket.java           # Base class
├── PacketBounds.java             # Validation bounds
├── editor/                       # Editor packets
├── dialogue/                     # Dialogue packets
├── explorer/                     # File explorer packets
├── sync/                         # Data sync packets
└── world/                        # World modification packets
```

## Creating a New Packet

### 1. Extend AbstractPacket
```java
public class MyCustomPacket extends AbstractPacket {
    private final String data;
    private final int value;
    
    public MyCustomPacket(String data, int value) {
        this.data = data;
        this.value = value;
    }
    
    // Serialization
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(data);
        buf.writeInt(value);
    }
    
    // Deserialization
    public static MyCustomPacket read(FriendlyByteBuf buf) {
        return new MyCustomPacket(buf.readUtf(), buf.readInt());
    }
    
    // Packet definition for registration
    public static final PacketDefinition<MyCustomPacket> DEFINITION = 
        new PacketDefinition<>(Common.id("my_custom"), MyCustomPacket::read);
    
    @Override
    public PacketDefinition<?> definition() { return DEFINITION; }
}
```

### 2. Add Packet Bounds (Security)
```java
// In PacketBounds.java or create new bounds class
public static final PacketBounds MY_CUSTOM_BOUNDS = PacketBounds.builder()
    .maxStringLength(256)  // Max data string length
    .maxIntValue(10000)    // Max value
    .build();
```

### 3. Register in FoundryEngineMod.onCommonSetup()
```java
// In FoundryEngineMod.onCommonSetup(FMLCommonSetupEvent event)
var network = Common.getNetworkManager();
network.register(MyCustomPacket.DEFINITION);
network.registerBounds(MyCustomPacket.DEFINITION, PacketBounds.MY_CUSTOM_BOUNDS);
```

### 4. Send Packet
```java
// Server to client
Common.getNetworkManager().sendToPlayer(player, new MyCustomPacket("hello", 42));

// Client to server
Common.getNetworkManager().sendToServer(new MyCustomPacket("request", 1));

// Broadcast to all
Common.getNetworkManager().sendToAll(new MyCustomPacket("broadcast", 0));
```

### 5. Handle Packet
```java
// In packet class or separate handler
public static void handle(MyCustomPacket packet, PacketContext context) {
    // context.getSender() - sender (player or null for server)
    // context.enqueueWork(() -> { /* thread-safe logic */ });
}
```

### 6. Add Sync Logic (if needed)
```java
// In FoundryEngineMod.onServerStarted()
event.getServer().getPlayerList().getPlayers().forEach(player -> {
    Common.getNetworkManager().sendToPlayer(player, new MyCustomPacket(syncedData, value));
});

// In FoundryEngineMod.onPlayerChangedDimension()
Common.getNetworkManager().sendToPlayer(player, new MyCustomPacket(syncedData, value));
```

## IntelliJ MCP Commands

```json
// Find existing packets
{ "q": "extends AbstractPacket", "projectPath": "C:/Data/Projects/FoundryEngine" }

// Find packet registration
{ "searchText": "network.register", "projectPath": "C:/Data/Projects/FoundryEngine" }

// Find PacketBounds usage
{ "q": "PacketBounds", "projectPath": "C:/Data/Projects/FoundryEngine" }

// Check for problems
{ "filePath": "src/main/java/de/luckymcdev/foundryengine/common/network/packets/MyCustomPacket.java", "projectPath": "C:/Data/Projects/FoundryEngine" }
```

## Testing
```bash
./gradlew.bat test --tests *PacketBoundsTest
./gradlew.bat test --tests *NetworkManagerTest
```