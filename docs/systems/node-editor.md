# Node Graph Editor

The **node graph** system is a visual, drag-and-drop node editor built on ImNodes. It lets you build data-flow graphs by connecting typed pins between nodes. It is the foundation for data-driven tooling (currently used by shader/graph previews and as a reusable component for new tools).

## How it works

A `NodeEditorInstance<T>` manages an editor canvas for a pin type `T`. It owns:

- a **root node** (the graph output),
- a set of **nodes**, each with typed **input/output pins**,
- **links** connecting pins.

Nodes are created from the right-click context menu. Right-clicking the canvas (or dragging a link to empty space) opens a menu of registered node types.

## Types

Pin types are defined with `NodePinType<T>`:

```java
NodePinType<Double> DOUBLE = new NodePinType<>(
    "Number",                              // display name
    NodePinShape.FILLED_TRIANGLE,          // pin shape
    List.of(                               // node options shown in context menu
        new NodeOption<>("Constant", ConstantBuilder::new),
        new NodeOption<>("Add", AddBuilder::new),
        new NodeOption<>("Multiply", MultiplyBuilder::new)
    ),
    ConstantBuilder::new                   // default (root) builder
);
```

## Writing a node

Implement `NodeBuilder<T>`:

```java
public class AddBuilder implements NodeBuilder<Double> {
    private Node<Double> node;

    @Override
    public List<NodePin<Double>> getPins() {
        return List.of(
            new NodePin<>(NodeTypes.DOUBLE, "A", NodePinConnectionType.REQUIRED_INPUT, NodePinShape.TRIANGLE),
            new NodePin<>(NodeTypes.DOUBLE, "B", NodePinConnectionType.REQUIRED_INPUT, NodePinShape.TRIANGLE),
            new NodePin<>(NodeTypes.DOUBLE, "Out", NodePinConnectionType.OUTPUT, NodePinShape.FILLED_TRIANGLE)
        );
    }

    @Override
    public boolean render() {          // custom ImGui inside the node body
        ImGui.text("+");
        return false;                  // true if the node's value changed
    }

    @Override
    public Double evaluate() {         // compute output from inputs
        var inputs = node.inputPins;
        Double a = inputs.get(0).inputLink.node.builder.evaluate();
        Double b = inputs.get(1).inputLink.node.builder.evaluate();
        return a + b;
    }

    @Override
    public void setNode(Node<Double> node) { this.node = node; }
}
```

Key `NodeBuilder` hooks:

| Method             | Purpose                                             |
|--------------------|-----------------------------------------------------|
| `getPins()`        | Declare the node's input/output pins                |
| `render()`         | Optional custom ImGui widgets in the node body      |
| `evaluate()`       | Compute the node's output from its connected inputs |
| `getDisplayName()` | Title shown in the node title bar                   |
| `setNode(node)`    | Back-reference to the owning `Node`                 |

## Embedding an editor

```java
NodeEditorInstance<Double> editor = new NodeEditorInstance<>(NodeTypes.DOUBLE);
editor.rootBuilder = new ConstantBuilder();  // optional root value provider

// In your panel render loop:
boolean changed = editor.content(imGraphics);
```

`content()` renders the full editor (nodes, links, minimap, context menu, selection, delete handling) and returns `true` when the graph changed.

## Related

- [Editor](editor.md) — in-game editor with panels
- [Node Test Panel](editor.md) — live test harness for node graphs