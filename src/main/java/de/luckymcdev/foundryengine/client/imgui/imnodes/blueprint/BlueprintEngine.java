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
        registerInputNodes();
        registerFlowControl();
        registerLogicNodes();
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

    private void registerInputNodes() {
        node("Input", "Bool Value")
                .out(boolType, "Value")
                .in(boolType, "Value").defaultValue("Value", false)
                .behavior((n, e, ed, ctx) -> {
                    boolean v = ctx.resolvePinAs(n.inputPin("Value"), Boolean.class, false);
                    n.setOutput("Value", v);
                }).register();

        node("Input", "Int Value")
                .out(intType, "Value")
                .in(intType, "Value").defaultValue("Value", 0)
                .behavior((n, e, ed, ctx) -> {
                    int v = ctx.resolvePinAs(n.inputPin("Value"), Integer.class, 0);
                    n.setOutput("Value", v);
                }).register();

        node("Input", "Float Value")
                .out(floatType, "Value")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    n.setOutput("Value", v);
                }).register();

        node("Input", "String Value")
                .out(stringType, "Value")
                .in(stringType, "Value").defaultValue("Value", "")
                .behavior((n, e, ed, ctx) -> {
                    String v = ctx.resolvePinAs(n.inputPin("Value"), String.class, "");
                    n.setOutput("Value", v);
                }).register();
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
                    int last = ctx.resolvePinAs(n.inputPin("Last Index"), Integer.class, 9);
                    var bodyPin = n.outputPin("Loop Body");
                    var donePin = n.outputPin("Completed");
                    for (int i = first; i <= last; i++) {
                        n.setOutput("Index", i);
                        if (bodyPin != null) {
                            var next = ed.getConnectedInputPin(bodyPin);
                            if (next != null) e.executeNext(next.node, ed, ctx);
                        }
                    }
                    if (donePin != null) {
                        var next = ed.getConnectedInputPin(donePin);
                        if (next != null) e.executeNext(next.node, ed, ctx);
                    }
                })
                .register();

        node("Flow Control", "While Loop")
                .in(execType, "In")
                .in(boolType, "Condition").defaultValue("Condition", false)
                .out(execType, "Loop Body")
                .out(execType, "Completed")
                .behavior((n, e, ed, ctx) -> {
                    var bodyPin = n.outputPin("Loop Body");
                    var donePin = n.outputPin("Completed");
                    int guard = 0;
                    while (ctx.resolvePinAs(n.inputPin("Condition"), Boolean.class, false) && guard++ < 10_000) {
                        if (bodyPin != null) {
                            var next = ed.getConnectedInputPin(bodyPin);
                            if (next != null) e.executeNext(next.node, ed, ctx);
                        }
                    }
                    if (donePin != null) {
                        var next = ed.getConnectedInputPin(donePin);
                        if (next != null) e.executeNext(next.node, ed, ctx);
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
                        // execution continues to Out via normal flow
                    }
                    // else: swallow execution
                })
                .register();

        node("Flow Control", "Flip Flop")
                .in(execType, "In")
                .out(execType, "A")
                .out(execType, "B")
                .out(boolType, "Is A")
                .behavior((n, e, ed, ctx) -> {
                    String key = "__flipflop_" + n.id;
                    boolean isA = !Boolean.TRUE.equals(ctx.getVar(key, Boolean.class));
                    ctx.setVar(key, isA);
                    n.setOutput("Is A", isA);
                    String branch = isA ? "A" : "B";
                    var outPin = n.outputPin(branch);
                    if (outPin != null) {
                        var next = ed.getConnectedInputPin(outPin);
                        if (next != null) e.executeNext(next.node, ed, ctx);
                    }
                })
                .register();

        node("Flow Control", "Gate")
                .in(execType, "Enter")
                .in(execType, "Open")
                .in(execType, "Close")
                .in(boolType, "Start Closed").defaultValue("Start Closed", true)
                .out(execType, "Exit")
                .behavior((n, e, ed, ctx) -> {
                    String key = "__gate_open_" + n.id;
                    if (!ctx.hasVar(key)) {
                        boolean startClosed = ctx.resolvePinAs(n.inputPin("Start Closed"), Boolean.class, true);
                        ctx.setVar(key, !startClosed);
                    }
                    boolean open = Boolean.TRUE.equals(ctx.getVar(key, Boolean.class));
                    if (open) {
                        var exit = n.outputPin("Exit");
                        if (exit != null) {
                            var next = ed.getConnectedInputPin(exit);
                            if (next != null) e.executeNext(next.node, ed, ctx);
                        }
                    }
                })
                .register();
    }

    private void registerLogicNodes() {
        node("Logic", "AND")
                .in(boolType, "A").defaultValue("A", false)
                .in(boolType, "B").defaultValue("B", false)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    boolean a = ctx.resolvePinAs(n.inputPin("A"), Boolean.class, false);
                    boolean b = ctx.resolvePinAs(n.inputPin("B"), Boolean.class, false);
                    n.setOutput("Result", a && b);
                }).register();

        node("Logic", "OR")
                .in(boolType, "A").defaultValue("A", false)
                .in(boolType, "B").defaultValue("B", false)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    boolean a = ctx.resolvePinAs(n.inputPin("A"), Boolean.class, false);
                    boolean b = ctx.resolvePinAs(n.inputPin("B"), Boolean.class, false);
                    n.setOutput("Result", a || b);
                }).register();

        node("Logic", "NOT")
                .in(boolType, "Value").defaultValue("Value", false)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    boolean v = ctx.resolvePinAs(n.inputPin("Value"), Boolean.class, false);
                    n.setOutput("Result", !v);
                }).register();

        node("Logic", "XOR")
                .in(boolType, "A").defaultValue("A", false)
                .in(boolType, "B").defaultValue("B", false)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    boolean a = ctx.resolvePinAs(n.inputPin("A"), Boolean.class, false);
                    boolean b = ctx.resolvePinAs(n.inputPin("B"), Boolean.class, false);
                    n.setOutput("Result", a ^ b);
                }).register();
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

        node("Math|Int", "Divide (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 1)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 1);
                    n.setOutput("Result", b != 0 ? a / b : 0);
                }).register();

        node("Math|Int", "Modulo (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 1)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 1);
                    n.setOutput("Result", b != 0 ? a % b : 0);
                }).register();

        node("Math|Int", "Abs (Int)")
                .in(intType, "Value").defaultValue("Value", 0)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int v = ctx.resolvePinAs(n.inputPin("Value"), Integer.class, 0);
                    n.setOutput("Result", Math.abs(v));
                }).register();

        node("Math|Int", "Min (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 0)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", Math.min(a, b));
                }).register();

        node("Math|Int", "Max (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 0)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", Math.max(a, b));
                }).register();

        node("Math|Int", "Clamp (Int)")
                .in(intType, "Value").defaultValue("Value", 0)
                .in(intType, "Min").defaultValue("Min", 0)
                .in(intType, "Max").defaultValue("Max", 100)
                .out(intType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int v = ctx.resolvePinAs(n.inputPin("Value"), Integer.class, 0);
                    int min = ctx.resolvePinAs(n.inputPin("Min"), Integer.class, 0);
                    int max = ctx.resolvePinAs(n.inputPin("Max"), Integer.class, 100);
                    n.setOutput("Result", Math.min(max, Math.max(min, v)));
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

        node("Math|Int", "Less Than (Int)")
                .in(intType, "A").defaultValue("A", 0)
                .in(intType, "B").defaultValue("B", 0)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a < b);
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

        node("Math|Float", "Subtract (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 0f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a - b);
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

        node("Math|Float", "Divide (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 1f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 1f);
                    n.setOutput("Result", b != 0f ? a / b : 0f);
                }).register();

        node("Math|Float", "Abs (Float)")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    n.setOutput("Result", Math.abs(v));
                }).register();

        node("Math|Float", "Min (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 0f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", Math.min(a, b));
                }).register();

        node("Math|Float", "Max (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 0f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", Math.max(a, b));
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

        node("Math|Float", "Sin (Float)")
                .in(floatType, "Radians").defaultValue("Radians", 0f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Radians"), Float.class, 0f);
                    n.setOutput("Result", (float) Math.sin(v));
                }).register();

        node("Math|Float", "Cos (Float)")
                .in(floatType, "Radians").defaultValue("Radians", 0f)
                .out(floatType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Radians"), Float.class, 0f);
                    n.setOutput("Result", (float) Math.cos(v));
                }).register();

        node("Math|Float", "Floor (Float)")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .out(floatType, "Result")
                .out(intType, "Int Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    float r = (float) Math.floor(v);
                    n.setOutput("Result", r);
                    n.setOutput("Int Result", (int) r);
                }).register();

        node("Math|Float", "Ceil (Float)")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .out(floatType, "Result")
                .out(intType, "Int Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    float r = (float) Math.ceil(v);
                    n.setOutput("Result", r);
                    n.setOutput("Int Result", (int) r);
                }).register();

        node("Math|Float", "Round (Float)")
                .in(floatType, "Value").defaultValue("Value", 0f)
                .out(floatType, "Result")
                .out(intType, "Int Result")
                .behavior((n, e, ed, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    int r = Math.round(v);
                    n.setOutput("Result", (float) r);
                    n.setOutput("Int Result", r);
                }).register();

        node("Math|Float", "Equal (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 0f)
                .in(floatType, "Tolerance").defaultValue("Tolerance", 0.0001f)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    float tol = ctx.resolvePinAs(n.inputPin("Tolerance"), Float.class, 0.0001f);
                    n.setOutput("Result", Math.abs(a - b) <= tol);
                }).register();

        node("Math|Float", "Greater Than (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 0f)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a > b);
                }).register();

        node("Math|Float", "Less Than (Float)")
                .in(floatType, "A").defaultValue("A", 0f)
                .in(floatType, "B").defaultValue("B", 0f)
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a < b);
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

        node("String", "Replace")
                .in(stringType, "Source").defaultValue("Source", "")
                .in(stringType, "From").defaultValue("From", "")
                .in(stringType, "To").defaultValue("To", "")
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String src = ctx.resolvePinAs(n.inputPin("Source"), String.class, "");
                    String from = ctx.resolvePinAs(n.inputPin("From"), String.class, "");
                    String to = ctx.resolvePinAs(n.inputPin("To"), String.class, "");
                    n.setOutput("Result", from.isEmpty() ? src : src.replace(from, to));
                }).register();

        node("String", "To Upper")
                .in(stringType, "String").defaultValue("String", "")
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("String"), String.class, "");
                    n.setOutput("Result", s.toUpperCase());
                }).register();

        node("String", "To Lower")
                .in(stringType, "String").defaultValue("String", "")
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("String"), String.class, "");
                    n.setOutput("Result", s.toLowerCase());
                }).register();

        node("String", "Trim")
                .in(stringType, "String").defaultValue("String", "")
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("String"), String.class, "");
                    n.setOutput("Result", s.trim());
                }).register();

        node("String", "Substring")
                .in(stringType, "String").defaultValue("String", "")
                .in(intType, "Start").defaultValue("Start", 0)
                .in(intType, "Length").defaultValue("Length", 1)
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("String"), String.class, "");
                    int start = ctx.resolvePinAs(n.inputPin("Start"), Integer.class, 0);
                    int length = ctx.resolvePinAs(n.inputPin("Length"), Integer.class, 1);
                    int end = Math.min(start + length, s.length());
                    start = Math.max(0, Math.min(start, s.length()));
                    n.setOutput("Result", s.substring(start, end));
                }).register();

        node("String", "Format")
                .in(stringType, "Template").defaultValue("Template", "Hello, {}!")
                .in(anyType, "Arg 0")
                .in(anyType, "Arg 1")
                .out(stringType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String tpl = ctx.resolvePinAs(n.inputPin("Template"), String.class, "");
                    Object arg0 = ctx.resolvePin(n.inputPin("Arg 0"));
                    Object arg1 = ctx.resolvePin(n.inputPin("Arg 1"));
                    String result = tpl;
                    if (arg0 != null) result = result.replaceFirst("\\{}", String.valueOf(arg0));
                    if (arg1 != null) result = result.replaceFirst("\\{}", String.valueOf(arg1));
                    n.setOutput("Result", result);
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

        node("Conversion", "String to Int")
                .in(stringType, "Value").defaultValue("Value", "0")
                .out(intType, "Result")
                .out(boolType, "Success")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("Value"), String.class, "0");
                    try {
                        n.setOutput("Result", Integer.parseInt(s.trim()));
                        n.setOutput("Success", true);
                    } catch (NumberFormatException ex) {
                        n.setOutput("Result", 0);
                        n.setOutput("Success", false);
                    }
                }).register();

        node("Conversion", "String to Float")
                .in(stringType, "Value").defaultValue("Value", "0.0")
                .out(floatType, "Result")
                .out(boolType, "Success")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("Value"), String.class, "0.0");
                    try {
                        n.setOutput("Result", Float.parseFloat(s.trim()));
                        n.setOutput("Success", true);
                    } catch (NumberFormatException ex) {
                        n.setOutput("Result", 0f);
                        n.setOutput("Success", false);
                    }
                }).register();

        node("Conversion", "String to Bool")
                .in(stringType, "Value").defaultValue("Value", "false")
                .out(boolType, "Result")
                .behavior((n, e, ed, ctx) -> {
                    String s = ctx.resolvePinAs(n.inputPin("Value"), String.class, "false");
                    n.setOutput("Result", Boolean.parseBoolean(s.trim()));
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