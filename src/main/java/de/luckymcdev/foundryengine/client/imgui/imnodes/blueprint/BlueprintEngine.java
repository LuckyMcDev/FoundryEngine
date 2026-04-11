package de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.imgui.imnodes.Node;
import de.luckymcdev.foundryengine.client.imgui.imnodes.NodeEditorInstance;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePin;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinInfo;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

/**
 * Central engine for the Blueprint node graph.
 */
public class BlueprintEngine {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final NodePinType<Void> execType = BlueprintTypes.EXEC;
    public final NodePinType<Boolean> boolType = BlueprintTypes.BOOL;
    public final NodePinType<Integer> intType = BlueprintTypes.INT;
    public final NodePinType<Float> floatType = BlueprintTypes.FLOAT;
    public final NodePinType<String> stringType = BlueprintTypes.STRING;
    public final NodePinType<float[]> vectorType = BlueprintTypes.VECTOR;
    public final NodePinType<Object> objectType = BlueprintTypes.OBJECT;
    public final NodePinType<Object> anyType = BlueprintTypes.ANY;
    private final List<NodeTemplate> nodeRegistry = new ArrayList<>();
    private final Map<String, NodeBehavior> behaviors = new HashMap<>();

    public void registerNode(String category, String name, Supplier<List<NodePin>> pins,
                             Map<String, Object> defaults, @Nullable NodeBehavior behavior) {
        nodeRegistry.add(new NodeTemplate(category, name, pins, defaults));
        if (behavior != null) behaviors.put(name, behavior);
    }

    /**
     * Convenience overload without defaults.
     */
    public void registerNode(String category, String name, Supplier<List<NodePin>> pins,
                             @Nullable NodeBehavior behavior) {
        registerNode(category, name, pins, Map.of(), behavior);
    }

    /**
     * Fluent builder entry point.
     */
    public NodeBuilder node(String category, String name) {
        return new NodeBuilder(this, category, name);
    }

    public List<NodeTemplate> getRegistry() {
        return Collections.unmodifiableList(nodeRegistry);
    }

    public @Nullable NodeBehavior getBehavior(String nodeName) {
        return behaviors.get(nodeName);
    }

    /**
     * Creates a new {@link Node} from a template, injecting registered
     * default values into unconnected input pins.
     */
    public Node createNode(NodeTemplate template) {
        List<NodePin> pins = template.pins().get();
        Node node = new Node(template.name(), template.category(), pins);
        for (var pin : node.inputPins) {
            Object def = template.pinDefaults().get(pin.pin.label());
            if (def != null) pin.defaultValue = def;
        }
        return node;
    }

    /**
     * Returns true when {@code src} (output) may wire to {@code dst} (input).
     */
    public boolean canConnect(NodePinInfo src, NodePinInfo dst) {
        return src.pin.type().isCompatibleWith(dst.pin.type());
    }

    /**
     * Finds the first "Event BeginPlay" node and starts exec-flow traversal.
     */
    public void executeGraph(NodeEditorInstance<?> editor) {
        Node startNode = null;
        for (Node node : editor.nodes.values()) {
            if (node.name.equals("Event BeginPlay")) {
                startNode = node;
                break;
            }
        }

        if (startNode == null) {
            LOGGER.error("Execution failed: No 'Event BeginPlay' node found.");
            return;
        }

        BlueprintContext ctx = new BlueprintContext(editor);
        executeNext(startNode, editor, ctx);
    }

    void executeNext(Node node, NodeEditorInstance<?> editor, BlueprintContext ctx) {
        NodeBehavior behavior = behaviors.get(node.name);
        if (behavior != null) {
            behavior.execute(node, this, editor, ctx);
        } else {
            LOGGER.debug("Executed: {} (no behavior)", node.name);
        }

        for (var pin : node.outputPins) {
            if (pin.pin.type() == execType) {
                var connectedInput = editor.getConnectedInputPin(pin);
                if (connectedInput != null) {
                    executeNext(connectedInput.node, editor, ctx);
                    return;
                }
            }
        }
    }

    /**
     * Registers the full built-in node library.
     * Call once after construction.
     */
    public void registerBuiltins() {
        registerEvents();
        registerFlowControl();
        registerMathInt();
        registerMathFloat();
        registerStringNodes();
        registerVariableNodes();
        registerConversionNodes();
    }

    private void registerEvents() {
        node("Events", "Event BeginPlay")
                .out(execType, "Out")
                .behavior((n, e, ed, ctx) -> LOGGER.debug(">>> BeginPlay"))
                .register();

        node("Events", "Event Tick")
                .out(execType, "Out")
                .out(floatType, "DeltaTime")
                .behavior((n, e, ed, ctx) -> {
                    n.setOutput("DeltaTime", 0.016f);
                    LOGGER.debug(">>> Tick");
                })
                .register();
    }

    private void registerFlowControl() {
        node("Flow Control", "Branch")
                .in(execType, "In")
                .in(boolType, "Condition").defaultValue("Condition", false)
                .out(execType, "True")
                .out(execType, "False")
                .behavior((n, e, ed, ctx) -> {
                    boolean cond = ctx.resolvePinAs(n.inputPin("Condition"), Boolean.class, false);
                    String branch = cond ? "True" : "False";
                    var outPin = n.outputPin(branch);
                    if (outPin != null) {
                        var next = ed.getConnectedInputPin(outPin);
                        if (next != null) e.executeNext(next.node, ed, ctx);
                    }
                })
                .register();

        node("Flow Control", "Sequence")
                .in(execType, "In")
                .out(execType, "Then 0")
                .out(execType, "Then 1")
                .out(execType, "Then 2")
                .behavior((n, e, ed, ctx) -> {
                    for (var pin : n.outputPins) {
                        if (pin.pin.type() == execType) {
                            var next = ed.getConnectedInputPin(pin);
                            if (next != null) e.executeNext(next.node, ed, ctx);
                        }
                    }
                })
                .register();

        node("Flow Control", "For Loop")
                .in(execType, "In")
                .in(intType, "First Index").defaultValue("First Index", 0)
                .in(intType, "Last Index").defaultValue("Last Index", 9)
                .out(execType, "Loop Body")
                .out(execType, "Completed")
                .out(intType, "Index")
                .behavior((n, e, ed, ctx) -> {
                    int first = ctx.resolvePinAs(n.inputPin("First Index"), Integer.class, 0);
                    int last = ctx.resolvePinAs(n.inputPin("Last Index"), Integer.class, 0);
                    var bodyPin = n.outputPin("Loop Body");
                    for (int i = first; i <= last; i++) {
                        n.setOutput("Index", i);
                        if (bodyPin != null) {
                            var next = ed.getConnectedInputPin(bodyPin);
                            if (next != null) e.executeNext(next.node, ed, ctx);
                        }
                    }
                })
                .register();

        node("Flow Control", "Do Once")
                .in(execType, "In")
                .in(execType, "Reset")
                .out(execType, "Out")
                .behavior((n, e, ed, ctx) -> {
                    String key = "__doonce_" + n.id;
                    if (!Boolean.TRUE.equals(ctx.getVar(key, Boolean.class))) {
                        ctx.setVar(key, true);
                        // normal exec follow fires Out
                    }
                    // else: swallow execution
                })
                .register();
    }

    private void registerMathInt() {
        node("Math|Int", "Add (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 0)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a + b);
                }).register();

        node("Math|Int", "Subtract (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 0)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a - b);
                }).register();

        node("Math|Int", "Multiply (Int)")
                .in(intType, "A").defaultValue("A", 1)
                .in(intType, "B").defaultValue("B", 1)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 1);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 1);
                    n.setOutput("Result", a * b);
                }).register();

        node("Math|Int", "Equal (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 0)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a == b);
                }).register();

        node("Math|Int", "Greater Than (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 0)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a > b);
                }).register();
    }

    private void registerMathFloat() {
        node("Math|Float", "Add (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 0f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a + b);
                }).register();

        node("Math|Float", "Multiply (Float)")
                .in(floatType, "A").defaultValue("A", 1f)
                .in(floatType, "B").defaultValue("B", 1f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 1f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 1f);
                    n.setOutput("Result", a * b);
                }).register();

        node("Math|Float", "Clamp (Float)")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .in(floatType, "Min").defaultValue("Min", 0f)
                .in(floatType, "Max").defaultValue("Max", 1f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    float min = ctx.resolvePinAs(n.inputPin("Min"), Float.class, 0f);
                    float max = ctx.resolvePinAs(n.inputPin("Max"), Float.class, 1f);
                    n.setOutput("Result", Math.min(max, Math.max(min, v)));
                }).register();

        node("Math|Float", "Lerp (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 1f)
                .in(floatType, "Alpha").defaultValue("Alpha", 0.5f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 1f);
                    float alpha = ctx.resolvePinAs(n.inputPin("Alpha"), Float.class, 0.5f);
                    n.setOutput("Result", a + (b - a) * alpha);
                }).register();
    }

    private void registerStringNodes() {
        node("String", "Print String")
                .in(execType, "In")
                .in(stringType, "String").defaultValue("String", "Hello")
                .out(execType, "Out")
                .behavior((n, e, ed, ctx) -> {
                    String text = ctx.resolvePinAs(n.inputPin("String"), String.class, "");
                    LOGGER.info("[Blueprint Print] {}", text);
                    System.out.println("[Blueprint Print] " + text);
                }).register();

        node("String", "Append String")
                .in(stringType, "A").defaultValue("A", "")
                .in(stringType, "B").defaultValue("B", "")
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String a = ctx.resolvePinAs(n.inputPin("A"), String.class, "");
                    String b = ctx.resolvePinAs(n.inputPin("B"), String.class, "");
                    n.setOutput("Result", a + b);
                }).register();

        node("String", "String Length")
                .in(stringType, "String").defaultValue("String", "")
                .out(intType, "Length")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("String"), String.class, "");
                    n.setOutput("Length", s.length());
                }).register();

        node("String", "Contains")
                .in(stringType, "Source").defaultValue("Source", "")
                .in(stringType, "Search").defaultValue("Search", "")
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String src = ctx.resolvePinAs(n.inputPin("Source"), String.class, "");
                    String search = ctx.resolvePinAs(n.inputPin("Search"), String.class, "");
                    n.setOutput("Result", src.contains(search));
                }).register();
    }

    private void registerVariableNodes() {
        node("Variables", "Set Variable")
                .in(execType, "In")
                .in(stringType, "Name").defaultValue("Name", "myVar")
                .in(anyType, "Value")
                .out(execType, "Out")
                .behavior((n, e, ed, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "unnamed");
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    ctx.setVar(name, value);
                    LOGGER.debug("Set var '{}' = {}", name, value);
                }).register();

        node("Variables", "Get Variable")
                .in(stringType, "Name").defaultValue("Name", "myVar")
                .out(anyType, "Value")
                .behavior((n, e, ed, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    n.setOutput("Value", ctx.getVar(name, Object.class));
                }).register();
    }

    private void registerConversionNodes() {
        node("Conversion", "Int to Float")
                .in(intType, "Value").defaultValue("Value", 0)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int v = ctx.resolvePinAs(n.inputPin("Value"), Integer.class, 0);
                    n.setOutput("Result", (float) v);
                }).register();

        node("Conversion", "Float to Int")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    n.setOutput("Result", (int) v);
                }).register();

        node("Conversion", "Bool to String")
                .in(boolType, "Value").defaultValue("Value", false)
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    boolean v = ctx.resolvePinAs(n.inputPin("Value"), Boolean.class, false);
                    n.setOutput("Result", String.valueOf(v));
                }).register();

        node("Conversion", "Int to String")
                .in(intType, "Value").defaultValue("Value", 0)
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int v = ctx.resolvePinAs(n.inputPin("Value"), Integer.class, 0);
                    n.setOutput("Result", String.valueOf(v));
                }).register();

        node("Conversion", "Float to String")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    n.setOutput("Result", String.valueOf(v));
                }).register();
    }

    @FunctionalInterface
    public interface NodeBehavior {
        void execute(Node node, BlueprintEngine engine, NodeEditorInstance<?> editor, BlueprintContext ctx);
    }

    public record NodeTemplate(
            String category,
            String name,
            Supplier<List<NodePin>> pins,
            Map<String, Object> pinDefaults
    ) {
        public NodeTemplate(String category, String name, Supplier<List<NodePin>> pins) {
            this(category, name, pins, Map.of());
        }
    }

    /**
     * Fluent builder for registering nodes without boilerplate.
     *
     * <pre>{@code
     * engine.node("Math|Float", "Lerp")
     *     .in(floatType, "A").defaultValue("A", 0f)
     *     .in(floatType, "B").defaultValue("B", 1f)
     *     .in(floatType, "Alpha").defaultValue("Alpha", 0.5f)
     *     .out(floatType, "Result")
     *     .behavior((n, e, ed, ctx) -> { ... })
     *     .register();
     * }</pre>
     */
    public final class NodeBuilder {
        private final BlueprintEngine engine;
        private final String category;
        private final String name;
        private final List<NodePin> pins = new ArrayList<>();
        private final Map<String, Object> defaults = new HashMap<>();
        private @Nullable NodeBehavior behavior;

        private NodeBuilder(BlueprintEngine engine, String category, String name) {
            this.engine = engine;
            this.category = category;
            this.name = name;
        }

        public NodeBuilder in(NodePinType<?> type, String label) {
            pins.add(type.required(label));
            return this;
        }

        public NodeBuilder inOpt(NodePinType<?> type, String label) {
            pins.add(type.optional(label));
            return this;
        }

        public NodeBuilder out(NodePinType<?> type, String label) {
            pins.add(type.output(label));
            return this;
        }

        public NodeBuilder defaultValue(String pinLabel, Object value) {
            defaults.put(pinLabel, value);
            return this;
        }

        public NodeBuilder behavior(NodeBehavior behavior) {
            this.behavior = behavior;
            return this;
        }

        public BlueprintEngine register() {
            List<NodePin> capturedPins = List.copyOf(pins);
            Map<String, Object> capturedDefaults = Map.copyOf(defaults);
            engine.registerNode(category, name,
                    () -> new ArrayList<>(capturedPins),
                    capturedDefaults,
                    behavior);
            return engine;
        }
    }
}