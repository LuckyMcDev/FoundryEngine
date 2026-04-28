package de.luckymcdev.foundryengine.common.blueprint.engine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.graph.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
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
    public final NodePinType<Object> objectType = BlueprintTypes.OBJECT;
    public final NodePinType<Object> anyType = BlueprintTypes.ANY;

    private final List<NodeTemplate> nodeRegistry = new ArrayList<>();
    private final Map<String, NodeBehavior> behaviors = new HashMap<>();

    public void registerNode(String category, String name, Supplier<List<NodePin>> pins,
                             Map<String, Object> defaults, @Nullable NodeBehavior behavior) {
        nodeRegistry.add(new NodeTemplate(category, name, pins, defaults));
        if (behavior != null) behaviors.put(name, behavior);
    }

    public NodeBuilder node(String category, String name) {
        return new NodeBuilder(this, category, name);
    }

    public List<NodeTemplate> getRegistry() {
        return Collections.unmodifiableList(nodeRegistry);
    }

    public @Nullable NodeBehavior getBehavior(String nodeName) {
        return behaviors.get(nodeName);
    }

    public BlueprintNode createNode(NodeTemplate template) {
        List<NodePin> pins = template.pins().get();
        BlueprintNode node = new BlueprintNode(template.name(), template.category(), pins);
        for (var pin : node.inputPins) {
            Object def = template.pinDefaults().get(pin.pin.label());
            if (def != null) pin.defaultValue = def;
        }
        return node;
    }

    public boolean canConnect(NodePinInfo src, NodePinInfo dst) {
        return src.pin.type().isCompatibleWith(dst.pin.type());
    }

    public void executeGraph(BlueprintGraph graph) {
        executeEvent("BeginPlay", graph);
    }

    public void executeEvent(String eventName, BlueprintGraph graph) {
        executeEvent(eventName, graph, Collections.emptyMap());
    }

    public void executeEvent(String eventName, BlueprintGraph graph, Map<String, Object> payload) {
        for (BlueprintNode node : graph.nodes.values()) {
            if (node.name.equals(eventName)) {
                BlueprintContext ctx = new BlueprintContext(graph);
                payload.forEach(ctx::setVar);
                executeNext(node, graph, ctx);
            }
        }
    }

    public void executeNext(BlueprintNode node, BlueprintGraph graph, BlueprintContext ctx) {
        NodeBehavior behavior = behaviors.get(node.name);
        if (behavior != null) {
            behavior.execute(node, this, graph, ctx);
        }

        for (var pin : node.outputPins) {
            if (pin.pin.type() == execType) {
                var connectedInput = graph.getConnectedInputPin(pin);
                if (connectedInput != null) {
                    executeNext(connectedInput.node, graph, ctx);
                    return;
                }
            }
        }
    }

    public void registerBuiltins() {
        registerEvents();
        registerVariableNodes();
        registerInputNodes();
        registerUtilityNodes();
    }

    private void registerEvents() {
        node(Categories.EVENTS, "BeginPlay")
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> {
                })
                .register();

        node(Categories.EVENTS, "Client Tick").out(execType, "Out").register();
        node(Categories.EVENTS, "Render GUI").out(execType, "Out").register();
        node(Categories.EVENTS, "Chat Message").out(execType, "Out").register();
        node(Categories.EVENTS, "Server Started").out(execType, "Out").register();
        node(Categories.EVENTS, "Server Tick").out(execType, "Out").register();
    }

    private void registerVariableNodes() {
        node(Categories.VARIABLES, "Set Variable")
                .in(execType, "In")
                .in(stringType, "Name").defaultValue("Name", "myVar")
                .in(anyType, "Value")
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "unnamed");
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    ctx.setVar(name, value);
                }).register();

        node(Categories.VARIABLES, "Get Variable")
                .in(stringType, "Name").defaultValue("Name", "myVar")
                .out(anyType, "Value")
                .behavior((n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    n.setOutput("Value", ctx.getVar(name, Object.class));
                }).register();
    }

    private void registerInputNodes() {
        node(Categories.INPUTS, "String")
                .in(stringType, "Value").defaultValue("Value", "")
                .out(stringType, "Out")
                .behavior((n, e, g, ctx) -> n.setOutput("Out", ctx.resolvePin(n.inputPin("Value"))))
                .register();

        node(Categories.INPUTS, "Integer")
                .in(intType, "Value").defaultValue("Value", 0)
                .out(intType, "Out")
                .behavior((n, e, g, ctx) -> n.setOutput("Out", ctx.resolvePin(n.inputPin("Value"))))
                .register();

        node(Categories.INPUTS, "Float")
                .in(floatType, "Value").defaultValue("Value", 0.0f)
                .out(floatType, "Out")
                .behavior((n, e, g, ctx) -> n.setOutput("Out", ctx.resolvePin(n.inputPin("Value"))))
                .register();

        node(Categories.INPUTS, "Boolean")
                .in(boolType, "Value").defaultValue("Value", false)
                .out(boolType, "Out")
                .behavior((n, e, g, ctx) -> n.setOutput("Out", ctx.resolvePin(n.inputPin("Value"))))
                .register();
    }

    private void registerUtilityNodes() {
        node(Categories.UTILS, "Print String")
                .in(execType, "In")
                .in(stringType, "String").defaultValue("String", "Hello")
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> {
                    String text = ctx.resolvePinAs(n.inputPin("String"), String.class, "");
                    LOGGER.info("[Blueprint] {}", text);
                }).register();

        node(Categories.UTILS, "Tell")
                .in(execType, "In")
                .in(stringType, "Target").defaultValue("Target", "Player")
                .in(stringType, "Message").defaultValue("Message", "Notification")
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> {
                    String target = ctx.resolvePinAs(n.inputPin("Target"), String.class, "Unknown");
                    String msg = ctx.resolvePinAs(n.inputPin("Message"), String.class, "");
                    var server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) return;
                    var player = server.getPlayerList().getPlayer(target);
                    if (player == null) return;
                    player.sendSystemMessage(Component.literal(msg));
                }).register();
    }

    @FunctionalInterface
    public interface NodeBehavior {
        void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx);
    }

    public static final class Categories {
        public static final String EVENTS = "Events";
        public static final String VARIABLES = "Variables";
        public static final String UTILS = "Utilities";
        public static final String INPUTS = "Inputs";

        private Categories() {
        }
    }

    public record NodeTemplate(
            String category,
            String name,
            Supplier<List<NodePin>> pins,
            Map<String, Object> pinDefaults
    ) {
    }
}