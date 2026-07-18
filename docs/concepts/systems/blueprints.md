# Blueprints

> **⚠️ Blueprint system has been temporarily removed.** The node-based visual scripting system is being reworked. This page exists for historical reference only. The system is not available in the current release.

Blueprints were node-based visual scripts that let you define game logic without writing code. They were serialized as JSON files with the `.febp` extension.

## Schema Overview

A blueprint file consists of three top-level arrays:

- `nodes` — The visual/logical nodes in the graph
- `pins` — Input and output connection points on nodes
- `links` — Connections between pins

## Example Blueprint

```json
{
  "nodes": [
    {
      "id": 1,
      "name": "Print String",
      "category": "Debug",
      "posX": 250.0,
      "posY": 100.0,
      "outputValues": {}
    }
  ],
  "pins": [
    {
      "id": 10,
      "nodeId": 1,
      "label": "Execute",
      "typeName": "Exec",
      "connectionType": "INPUT",
      "defaultValue": null,
      "isConnected": true
    },
    {
      "id": 11,
      "nodeId": 1,
      "label": "Value",
      "typeName": "String",
      "connectionType": "INPUT",
      "defaultValue": "Hello World",
      "isConnected": false
    }
  ],
  "links": [
    {
      "sourcePinId": 5,
      "targetPinId": 10
    }
  ]
}
```

## Node

Each node represents a discrete unit of logic in the blueprint graph.

| Field          | Type                  | Description                      |
|----------------|-----------------------|----------------------------------|
| `id`           | `int`                 | Unique identifier for the node   |
| `identifier`   | `string`              | Stable runtime ID                |
| `name`         | `string`              | Display name                     |
| `category`     | `string` or `null`    | Category for grouping            |
| `posX`         | `float`               | Grid-space X position            |
| `posY`         | `float`               | Grid-space Y position            |
| `outputValues` | `Map<string, object>` | Computed output values (runtime) |
| `data`         | `Map<string, object>` | Editor metadata                  |

## Pin

| Field            | Type                    | Description                |
|------------------|-------------------------|----------------------------|
| `id`             | `int`                   | Unique identifier          |
| `nodeId`         | `int`                   | Parent node ID             |
| `label`          | `string`                | Display label              |
| `typeName`       | `string`                | Data type                  |
| `connectionType` | `"INPUT"` or `"OUTPUT"` | Pin direction              |
| `defaultValue`   | `any` or `null`         | Default when not connected |
| `isConnected`    | `boolean`               | Whether linked             |

### Pin Types

| `typeName` | Description              |
|------------|--------------------------|
| `Exec`     | Execution flow (trigger) |
| `Bool`     | Boolean                  |
| `Int`      | Integer                  |
| `Float`    | Floating-point           |
| `String`   | Text string              |
| `Object`   | Generic object           |
| `Any`      | Accepts any type         |

## Link

| Field         | Type  | Description         |
|---------------|-------|---------------------|
| `sourcePinId` | `int` | Output pin (source) |
| `targetPinId` | `int` | Input pin (target)  |
