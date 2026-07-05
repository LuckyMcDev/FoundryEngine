---
name: event-creation
description: Standardized workflow for creating new events in FoundryEngine. Covers event class creation, registration in FoundryEngineMod, handler registration, and testing patterns. Uses IntelliJ MCP tools throughout.
---

# Event Creation Workflow

## Overview
Standardized process for adding new events to the FoundryEngine mod. Follow this workflow to ensure consistency and proper integration with the event bus system.

## Step-by-Step Process

### 1. Create Event Class
**Location:** `src/main/java/de/luckymcdev/foundryengine/common/event/` or subdirectory

**Pattern:**

```java
public record MyNewEvent(SomeData data) extends Event {
}
```

**Internal Registration Class:**
```java
public class MyNewEvent {
    public static final class Internal {
        public static void register(IEventBus bus) {
            bus.addListener(MyNewEvent::handle);
        }
        private static void handle(MyNewEvent event) { /* logic */ }
    }
}
```

### 2. Register in FoundryEngineMod
**File:** `src/main/java/de/luckymcdev/foundryengine/FoundryEngineMod.java`

Add to `registerInternalEvents()`:
```java
MyNewEvent.Internal.register(BUS);
```

Add handler in appropriate method:
```java
// For mod bus events
private void registerModEventHandlers(IEventBus modBus) {
    modBus.addListener(this::onMyNewEvent);
}

// For NeoForge bus events
private void registerNeoForgeEventHandlers() {
    BUS.addListener(this::onMyNewEvent);
}
```

### 3. Create Handler Method
```java
private void onMyNewEvent(MyNewEvent event) {
    // Handle event
    Common.post(new DownstreamEvent(event.getData()));
}
```

### 4. Add Tests
**Location:** `src/test/java/de/luckymcdev/foundryengine/common/event/`

```java
@Test
void testMyNewEvent() {
    var event = new MyNewEvent(testData);
    assertDoesNotThrow(() -> Common.post(event));
}
```

### 5. Verify with IntelliJ
```bash
./gradlew.bat test --tests *MyNewEventTest
```

## Event Types & Priorities

| Event Type | Bus | Priority | Use Case |
|------------|-----|----------|----------|
| Internal | NeoForge | DEFAULT | Core mod logic |
| Public API | modBus | DEFAULT | Addon/mod integration |
| Modification | NeoForge | LOWEST | Late-stage changes |
| Registry | RegisterEvent | HIGH | Registry registration |

## IntelliJ MCP Commands for Events

```json
// Find existing events
{ "q": "extends Event", "projectPath": "C:/Data/Projects/FoundryEngine" }

// Find event registration
{ "searchText": "registerInternalEvents", "projectPath": "C:/Data/Projects/FoundryEngine" }

// Check for problems after adding event
{ "filePath": "src/main/java/de/luckymcdev/foundryengine/FoundryEngineMod.java", "projectPath": "C:/Data/Projects/FoundryEngine" }
```