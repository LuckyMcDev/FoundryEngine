# Blueprint JSON Syntax

Blueprints in Foundry Engine are serialized as JSON files with the `.febp` extension. This document describes the
complete JSON schema for blueprint files.

You should not make blueprints via this json format. use the ingame editor.

## Schema Overview

A blueprint file consists of three top-level arrays:

- `nodes` - The visual/logical nodes in the graph
- `pins` - Input and output connection points on nodes
- `links` - Connections between pins

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

| Field          | Type                  | Description                                         |
|----------------|-----------------------|-----------------------------------------------------|
| `id`           | `int`                 | Unique identifier for the node                      |
| `identifier`   | `string`              | Stable runtime id (behavior lookup, event dispatch) |
| `name`         | `string`              | Display name of the node (e.g., `"Print String"`)   |
| `category`     | `string` or `null`    | Optional category for grouping nodes                |
| `posX`         | `float`               | Grid-space X position in the editor                 |
| `posY`         | `float`               | Grid-space Y position in the editor                 |
| `outputValues` | `Map<string, object>` | Computed output values keyed by pin label           |
| `data`         | `Map<string, object>` | Optional editor metadata (comments, UI state, etc)  |

## Pin

Pins are the connection points on nodes. They come in two flavors: `INPUT` and `OUTPUT`.

| Field            | Type                    | Description                                                     |
|------------------|-------------------------|-----------------------------------------------------------------|
| `id`             | `int`                   | Unique identifier for the pin                                   |
| `nodeId`         | `int`                   | ID of the parent node this pin belongs to                       |
| `label`          | `string`                | Display label of the pin (e.g., `"Execute"`, `"Value"`)         |
| `typeName`       | `string`                | Type of data flowing through this pin (see **Supported Types**) |
| `connectionType` | `"INPUT"` or `"OUTPUT"` | Direction of the pin                                            |
| `defaultValue`   | `any` or `null`         | Default value when the pin is not connected                     |
| `isConnected`    | `boolean`               | Whether this pin currently has a link                           |

### Supported Pin Types

| `typeName` | Description                |
|------------|----------------------------|
| `Exec`     | Execution flow (trigger)   |
| `Bool`     | Boolean (`true` / `false`) |
| `Int`      | Integer value              |
| `Float`    | Floating-point number      |
| `String`   | Text string                |
| `Object`   | Generic object reference   |
| `Any`      | Accepts any type           |

## Link

Links define data or execution flow connections between pins. A link always connects an `OUTPUT` pin to an `INPUT` pin.

| Field         | Type  | Description                   |
|---------------|-------|-------------------------------|
| `sourcePinId` | `int` | ID of the output pin (source) |
| `targetPinId` | `int` | ID of the input pin (target)  |

## Notes

- Node positions (`posX`, `posY`) are stored in grid-space coordinates and are purely for visual layout in the editor.
  They do not affect blueprint execution.
- The `outputValues` map on nodes is populated at runtime and typically serialized as empty in saved files.
- Links are only stored from the target pin's perspective — you do not need to define a reverse link.
