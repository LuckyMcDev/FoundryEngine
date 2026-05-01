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

    // Type Definitions
    public final NodePinType<Void> execType = BlueprintTypes.EXEC;
    public final NodePinType<Boolean> boolType = BlueprintTypes.BOOL;
    public final NodePinType<Integer> intType = BlueprintTypes.INT;
    public final NodePinType<Float> floatType = BlueprintTypes.FLOAT;
    public final NodePinType<String> stringType = BlueprintTypes.STRING;
    public final NodePinType<Object> objectType = BlueprintTypes.OBJECT;
    public final NodePinType<Object> anyType = BlueprintTypes.ANY;

    private final List<NodeTemplate> nodeRegistry = new ArrayList<>();
    private final Map<String, NodeBehavior> behaviors = new HashMap<>();
    /**
     * Client/editor hint: per-category title color (ARGB).
     */
    private final Map<String, Integer> categoryColors = new HashMap<>();

    public void registerNode(String category, String id, String displayName, Supplier<List<NodePin>> pins,
                             Map<String, Object> defaults, @Nullable NodeBehavior behavior) {
        nodeRegistry.add(new NodeTemplate(category, id, displayName, pins, defaults));
        if (behavior != null) behaviors.put(id, behavior);
    }

    public void setCategoryColor(String category, int argb) {
        categoryColors.put(category, argb);
    }

    public int getCategoryColor(@Nullable String category) {
        if (category == null) return 0xFF_404040;
        int slash = category.indexOf('/');
        String key = slash == -1 ? category : category.substring(0, slash);
        return categoryColors.getOrDefault(key, 0xFF_404040);
    }

    public NodeBuilder node(String category, String id, String displayName) {
        return new NodeBuilder(this, category, id, displayName);
    }

    public NodeBuilder node(String category, String name) {
        return node(category, name, name);
    }

    public List<NodeTemplate> getRegistry() {
        return Collections.unmodifiableList(nodeRegistry);
    }

    public @Nullable NodeBehavior getBehavior(String nodeId) {
        return behaviors.get(nodeId);
    }

    public BlueprintNode createNode(NodeTemplate template) {
        List<NodePin> pins = template.pins().get();
        BlueprintNode node = new BlueprintNode(template.displayName(), template.category(), pins);
        node.identifier = template.id();
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
        executeEvent(BuiltinNodes.EVENT_BEGIN_PLAY.id, graph);
    }

    public void executeEvent(String eventName, BlueprintGraph graph) {
        for (BlueprintNode node : graph.nodes.values()) {
            if (node.identifier.equals(eventName)) {
                BlueprintContext ctx = new BlueprintContext(graph);
                executeNext(node, graph, ctx);
            }
        }
    }

    public void executeNext(BlueprintNode node, BlueprintGraph graph, BlueprintContext ctx) {
        NodeBehavior behavior = behaviors.get(node.identifier);
        if (behavior != null) {
            behavior.execute(node, this, graph, ctx);
            if (node.category.equals(Categories.LOGIC)) return;
        }

        for (var pin : node.outputPins) {
            if (pin.pin.type() == execType) {
                executePin(node, pin.pin.label(), graph, ctx);
                return;
            }
        }
    }

    public void executePin(BlueprintNode node, String pinLabel, BlueprintGraph graph, BlueprintContext ctx) {
        var pin = node.outputPin(pinLabel);
        if (pin != null) {
            var connectedInput = graph.getConnectedInputPin(pin);
            if (connectedInput != null) {
                executeNext(connectedInput.node, graph, ctx);
            }
        }
    }

    public void registerBuiltins() {
        registerCategoryColors();
        registerEvents();
        registerVariableNodes();
        registerInputNodes();
        registerLogicNodes();
        registerMathNodes();
        registerUtilityNodes();
        registerEditorNodes();
    }

    private void registerCategoryColors() {
        // Loosely inspired by Unreal's "colored title bar per category" look.
        setCategoryColor(Categories.EVENTS, 0xFF_B83B2D);
        setCategoryColor(Categories.VARIABLES, 0xFF_2D9C4B);
        setCategoryColor(Categories.INPUTS, 0xFF_7B4BB3);
        setCategoryColor(Categories.LOGIC, 0xFF_D0912A);
        setCategoryColor(Categories.MATH, 0xFF_2AA7B1);
        setCategoryColor(Categories.UTILS, 0xFF_2D6DB8);
        setCategoryColor(Categories.COMMENTS, 0xFF_B7A11E);
    }

    private void registerEvents() {
        // Core and Bundle Events
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_BEGIN_PLAY.id, "BeginPlay")
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_REGISTRY.id, "Registry")
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_VANILLA_GAME.id, "Vanilla Game")
                .out(execType, "Out").register();

        // Client Events
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_TICK.id, "Client Tick")
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_STOPPED.id, "Client Stopped")
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_STOPPING.id, "Client Stopping")
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CHAT_MESSAGE.id, "Chat Message")
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_RENDER_GUI.id, "Render GUI")
                .out(execType, "Out").register();

        // Server Events
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_TICK.id, "Server Tick")
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_ABOUT_TO_START.id, "Server About To Start")
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STARTED.id, "Server Started")
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STARTING.id, "Server Starting")
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STOPPED.id, "Server Stopped")
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STOPPING.id, "Server Stopping")
                .out(execType, "Out").register();
    }

    private void registerLogicNodes() {
        node(Categories.LOGIC, "Branch")
                .in(execType, "In")
                .in(boolType, "Condition")
                .out(execType, "True")
                .out(execType, "False")
                .behavior((n, e, g, ctx) -> {
                    boolean cond = ctx.resolvePinAs(n.inputPin("Condition"), Boolean.class, false);
                    e.executePin(n, cond ? "True" : "False", g, ctx);
                }).register();

        node(Categories.LOGIC, "Sequence")
                .in(execType, "In")
                .out(execType, "Then 0")
                .out(execType, "Then 1")
                .behavior((n, e, g, ctx) -> {
                    e.executePin(n, "Then 0", g, ctx);
                    e.executePin(n, "Then 1", g, ctx);
                }).register();

        node(Categories.LOGIC, BuiltinNodes.REROUTE_EXEC.id, "Reroute (Exec)")
                .in(execType, "In")
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> e.executePin(n, "Out", g, ctx))
                .register();

        node(Categories.LOGIC, BuiltinNodes.REROUTE_ANY.id, "Reroute (Any)")
                .in(anyType, "In")
                .out(anyType, "Out")
                .behavior((n, e, g, ctx) -> n.setOutput("Out", ctx.resolvePin(n.inputPin("In"))))
                .register();
    }

    private void registerMathNodes() {
        node(Categories.MATH, "Int Add")
                .in(intType, "A").in(intType, "B")
                .out(intType, "Result")
                .behavior((n, e, g, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a + b);
                }).register();

        node(Categories.MATH, "Int Equals")
                .in(intType, "A").in(intType, "B")
                .out(boolType, "Result")
                .behavior((n, e, g, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a == b);
                }).register();
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
                    if (player != null) player.sendSystemMessage(Component.literal(msg));
                }).register();
    }

    private void registerEditorNodes() {
        node(Categories.COMMENTS, BuiltinNodes.COMMENT.id, "Comment").register();
    }

    @FunctionalInterface
    public interface NodeBehavior {
        void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx);
    }

    /**
     * Central ids for built-in nodes/events. Use these ids for dispatch instead of display names.
     */
    public enum BuiltinNodes {
        EVENT_BEGIN_PLAY("event.begin_play", "BeginPlay"),
        EVENT_REGISTRY("event.registry", "Registry"),
        EVENT_VANILLA_GAME("event.vanilla_game", "Vanilla Game"),
        EVENT_CLIENT_TICK("event.client_tick", "Client Tick"),
        EVENT_CLIENT_STOPPED("event.client_stopped", "Client Stopped"),
        EVENT_CLIENT_STOPPING("event.client_stopping", "Client Stopping"),
        EVENT_CHAT_MESSAGE("event.chat_message", "Chat Message"),
        EVENT_RENDER_GUI("event.render_gui", "Render GUI"),
        EVENT_SERVER_TICK("event.server_tick", "Server Tick"),
        EVENT_SERVER_ABOUT_TO_START("event.server_about_to_start", "Server About To Start"),
        EVENT_SERVER_STARTED("event.server_started", "Server Started"),
        EVENT_SERVER_STARTING("event.server_starting", "Server Starting"),
        EVENT_SERVER_STOPPED("event.server_stopped", "Server Stopped"),
        EVENT_SERVER_STOPPING("event.server_stopping", "Server Stopping"),
        COMMENT("editor.comment", "Comment"),
        REROUTE_EXEC("logic.reroute_exec", "Reroute (Exec)"),
        REROUTE_ANY("logic.reroute_any", "Reroute (Any)");

        public final String id;
        public final String legacyName;

        BuiltinNodes(String id, String legacyName) {
            this.id = id;
            this.legacyName = legacyName;
        }

        public static @Nullable String idFromLegacyName(String legacyName) {
            if (legacyName == null) return null;
            for (var b : values()) {
                if (b.legacyName.equals(legacyName)) return b.id;
            }
            return null;
        }
    }

    public static final class Categories {
        public static final String EVENTS = "Events";
        public static final String EVENTS_CLIENT = "Events/Client";
        public static final String EVENTS_SERVER = "Events/Server";
        public static final String EVENTS_BUNDLE = "Events/Bundle";
        public static final String VARIABLES = "Variables";
        public static final String UTILS = "Utilities";
        public static final String INPUTS = "Inputs";
        public static final String LOGIC = "Logic";
        public static final String MATH = "Math";
        public static final String COMMENTS = "Comments";

        private Categories() {
        }
    }

    public record NodeTemplate(
            String category,
            String id,
            String displayName,
            Supplier<List<NodePin>> pins,
            Map<String, Object> pinDefaults
    ) {
    }
}
