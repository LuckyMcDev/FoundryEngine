package de.luckymcdev.foundryengine.common.blueprint.engine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.blueprint.graph.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class BlueprintEngine {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String CTX_REGISTRY_EVENT = "_registry_event";
    public final NodePinType<Void> execType = BlueprintTypes.EXEC;
    public final NodePinType<Boolean> boolType = BlueprintTypes.BOOL;
    public final NodePinType<Integer> intType = BlueprintTypes.INT;
    public final NodePinType<Float> floatType = BlueprintTypes.FLOAT;
    public final NodePinType<String> stringType = BlueprintTypes.STRING;
    public final NodePinType<Object> objectType = BlueprintTypes.OBJECT;
    public final NodePinType<Object> anyType = BlueprintTypes.ANY;

    private final List<NodeTemplate> nodeRegistry = new ArrayList<>();
    private final Map<String, NodeBehavior> behaviors = new HashMap<>();
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

    public NodeBuilder node(String category, BuiltinNodes builtinNode) {
        return new NodeBuilder(this, category, builtinNode.id, builtinNode.legacyName);
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
        executeEvent(eventName, graph, Collections.emptyMap());
    }

    public void executeEvent(String eventName, BlueprintGraph graph, Map<String, Object> payload) {
        for (BlueprintNode node : graph.nodes.values()) {
            if (node.identifier.equals(eventName)) {
                BlueprintContext ctx = new BlueprintContext(graph);
                payload.forEach(ctx::setVar);
                if (!payload.isEmpty()) {
                    for (var out : node.outputPins) {
                        if (out.pin.type() == execType) continue;
                        Object v = payload.get(out.pin.label());
                        if (v != null) node.setOutput(out.pin.label(), v);
                    }
                }
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
        registerRegistryNodes();
        registerStringNodes();
    }

    private void registerCategoryColors() {
        setCategoryColor(Categories.EVENTS, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_BLOCK, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_ENTITY, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_ITEM, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_LEVEL, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_NETWORK, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_PLAYER, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_COMMAND, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_RECIPE, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_CLIENT, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_SERVER, 0xFF_B83B2D);
        setCategoryColor(Categories.EVENTS_BUNDLE, 0xFF_B83B2D);
        setCategoryColor(Categories.VARIABLES, 0xFF_2D9C4B);
        setCategoryColor(Categories.INPUTS, 0xFF_7B4BB3);
        setCategoryColor(Categories.LOGIC, 0xFF_D0912A);
        setCategoryColor(Categories.MATH, 0xFF_2AA7B1);
        setCategoryColor(Categories.UTILS, 0xFF_2D6DB8);
        setCategoryColor(Categories.COMMENTS, 0xFF_B7A11E);
    }

    private void registerEvents() {
        // Bundle / startup events
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_BEGIN_PLAY)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_REGISTRY)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_VANILLA_GAME)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_COMMON_SETUP)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_CLIENT_SETUP)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_DEDICATED_SERVER_SETUP)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BUNDLE, BuiltinNodes.EVENT_POST_INIT)
                .out(execType, "Out").register();

        // Client events
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_TICK)
                .out(execType, "Out")
                .out(intType, "Tick")
                .out(floatType, "DeltaSeconds")
                .register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_STOPPED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_STOPPING)
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CHAT_MESSAGE)
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_RENDER_GUI)
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_LOGGED_IN)
                .out(execType, "Out").register();
        node(Categories.EVENTS_CLIENT, BuiltinNodes.EVENT_CLIENT_LOGGED_OUT)
                .out(execType, "Out").register();

        // Server events
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_TICK)
                .out(execType, "Out")
                .out(intType, "Tick")
                .out(floatType, "DeltaSeconds")
                .register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_ABOUT_TO_START)
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STARTED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STARTING)
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STOPPED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_STOPPING)
                .out(execType, "Out").register();
        node(Categories.EVENTS_SERVER, BuiltinNodes.EVENT_SERVER_TAGS)
                .out(execType, "Out").register();

        // Block events
        node(Categories.EVENTS_BLOCK, BuiltinNodes.EVENT_BLOCK_BROKEN)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BLOCK, BuiltinNodes.EVENT_BLOCK_PLACED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BLOCK, BuiltinNodes.EVENT_BLOCK_LEFT_CLICKED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BLOCK, BuiltinNodes.EVENT_BLOCK_RIGHT_CLICKED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_BLOCK, BuiltinNodes.EVENT_FARMLAND_TRAMPLED)
                .out(execType, "Out").register();

        // Entity events
        node(Categories.EVENTS_ENTITY, BuiltinNodes.EVENT_ENTITY_JOIN_LEVEL)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ENTITY, BuiltinNodes.EVENT_LIVING_DEATH)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ENTITY, BuiltinNodes.EVENT_LIVING_DROPS)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ENTITY, BuiltinNodes.EVENT_LIVING_HURT)
                .out(execType, "Out").register();

        // Item events
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_PICKUP)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_DESTROY)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_RIGHT_CLICK)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_CRAFTED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_DROPPED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_FOOD_EATEN)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_SMELTED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_TOOLTIP)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_ENTITY_INTERACT)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_FIRST_LEFT_CLICK)
                .out(execType, "Out").register();
        node(Categories.EVENTS_ITEM, BuiltinNodes.EVENT_ITEM_FIRST_RIGHT_CLICK)
                .out(execType, "Out").register();

        // Level events
        node(Categories.EVENTS_LEVEL, BuiltinNodes.EVENT_LEVEL_LOAD)
                .out(execType, "Out").register();
        node(Categories.EVENTS_LEVEL, BuiltinNodes.EVENT_LEVEL_UNLOAD)
                .out(execType, "Out").register();
        node(Categories.EVENTS_LEVEL, BuiltinNodes.EVENT_LEVEL_SAVE)
                .out(execType, "Out").register();
        node(Categories.EVENTS_LEVEL, BuiltinNodes.EVENT_LEVEL_TICK)
                .out(execType, "Out").register();
        node(Categories.EVENTS_LEVEL, BuiltinNodes.EVENT_BEFORE_EXPLOSION)
                .out(execType, "Out").register();
        node(Categories.EVENTS_LEVEL, BuiltinNodes.EVENT_AFTER_EXPLOSION)
                .out(execType, "Out").register();

        // Network events
        node(Categories.EVENTS_NETWORK, BuiltinNodes.EVENT_NETWORK_LOGIN)
                .out(execType, "Out").register();
        node(Categories.EVENTS_NETWORK, BuiltinNodes.EVENT_NETWORK_LOGOUT)
                .out(execType, "Out").register();

        // Player events
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_PLAYER_LOGGED_IN)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_PLAYER_LOGGED_OUT)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_PLAYER_TICK)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_PLAYER_CHAT)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_PLAYER_ADVANCEMENT)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_CHEST_CLOSED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_CHEST_OPENED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_PLAYER_RESPAWNED)
                .out(execType, "Out").register();
        node(Categories.EVENTS_PLAYER, BuiltinNodes.EVENT_DECORATE_CHAT)
                .out(execType, "Out").register();

        // Command events
        node(Categories.EVENTS_COMMAND, BuiltinNodes.EVENT_COMMANDS)
                .out(execType, "Out").register();
        node(Categories.EVENTS_COMMAND, BuiltinNodes.EVENT_COMMANDS_CLIENT)
                .out(execType, "Out").register();

        // Recipe events
        node(Categories.EVENTS_RECIPE, BuiltinNodes.EVENT_RECIPE_VIEWER_UPDATED)
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

        node(Categories.LOGIC, "bool.not", "Not")
                .in(boolType, "Value").defaultValue("Value", false)
                .out(boolType, "Result")
                .behavior((n, e, g, ctx) -> {
                    boolean v = ctx.resolvePinAs(n.inputPin("Value"), Boolean.class, false);
                    n.setOutput("Result", !v);
                }).register();

        node(Categories.LOGIC, "bool.and", "And")
                .in(boolType, "A").in(boolType, "B")
                .out(boolType, "Result")
                .behavior((n, e, g, ctx) -> {
                    boolean a = ctx.resolvePinAs(n.inputPin("A"), Boolean.class, false);
                    boolean b = ctx.resolvePinAs(n.inputPin("B"), Boolean.class, false);
                    n.setOutput("Result", a && b);
                }).register();

        node(Categories.LOGIC, "bool.or", "Or")
                .in(boolType, "A").in(boolType, "B")
                .out(boolType, "Result")
                .behavior((n, e, g, ctx) -> {
                    boolean a = ctx.resolvePinAs(n.inputPin("A"), Boolean.class, false);
                    boolean b = ctx.resolvePinAs(n.inputPin("B"), Boolean.class, false);
                    n.setOutput("Result", a || b);
                }).register();
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

        node(Categories.MATH, "Int Sub")
                .in(intType, "A").in(intType, "B")
                .out(intType, "Result")
                .behavior((n, e, g, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a - b);
                }).register();

        node(Categories.MATH, "Int Mul")
                .in(intType, "A").in(intType, "B")
                .out(intType, "Result")
                .behavior((n, e, g, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a * b);
                }).register();

        node(Categories.MATH, "Int Equals")
                .in(intType, "A").in(intType, "B")
                .out(boolType, "Result")
                .behavior((n, e, g, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    n.setOutput("Result", a == b);
                }).register();

        node(Categories.MATH, "Int Mod")
                .in(intType, "A").in(intType, "B")
                .out(intType, "Result")
                .behavior((n, e, g, ctx) -> {
                    int a = ctx.resolvePinAs(n.inputPin("A"), Integer.class, 0);
                    int b = ctx.resolvePinAs(n.inputPin("B"), Integer.class, 0);
                    int res = (b != 0) ? (a % b) : 0;
                    n.setOutput("Result", res);
                }).register();

        node(Categories.MATH, "Float Add")
                .in(floatType, "A").in(floatType, "B")
                .out(floatType, "Result")
                .behavior((n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a + b);
                }).register();

        node(Categories.MATH, "Float Sub")
                .in(floatType, "A").in(floatType, "B")
                .out(floatType, "Result")
                .behavior((n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a - b);
                }).register();

        node(Categories.MATH, "Float Mul")
                .in(floatType, "A").in(floatType, "B")
                .out(floatType, "Result")
                .behavior((n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a * b);
                }).register();

        node(Categories.MATH, "Float Div")
                .in(floatType, "A").in(floatType, "B")
                .out(floatType, "Result")
                .behavior((n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 1f);
                    if (b == 0f) b = 1f;
                    n.setOutput("Result", a / b);
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
                    if (player != null) player.sendSystemMessage(Component.literal(msg));
                }).register();
    }

    private void registerEditorNodes() {
        node(Categories.COMMENTS, BuiltinNodes.COMMENT.id, "Comment").register();
    }

    private void registerRegistryNodes() {
        node(Categories.REGISTRY_ITEMS, "registry.item.register_simple", "Register Item (Simple)")
                .in(execType, "In")
                .in(stringType, "Id").defaultValue("Id", "mybundle:my_item")
                .in(intType, "Max Stack").defaultValue("Max Stack", 64)
                .in(boolType, "Fire Resistant").defaultValue("Fire Resistant", false)
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> {
                    Object ev = ctx.getVar(CTX_REGISTRY_EVENT, Object.class);
                    if (!(ev instanceof RegistryEvent regEv)) {
                        LOGGER.warn("[Blueprint] Register Item: missing registry event context ({}).", CTX_REGISTRY_EVENT);
                        e.executePin(n, "Out", g, ctx);
                        return;
                    }
                    String idStr = ctx.resolvePinAs(n.inputPin("Id"), String.class, "");
                    Identifier id = Identifier.tryParse(idStr);
                    if (id == null) {
                        LOGGER.warn("[Blueprint] Register Item: invalid id '{}'", idStr);
                        e.executePin(n, "Out", g, ctx);
                        return;
                    }
                    int stack = ctx.resolvePinAs(n.inputPin("Max Stack"), Integer.class, 64);
                    boolean fire = ctx.resolvePinAs(n.inputPin("Fire Resistant"), Boolean.class, false);
                    ItemBuilder b = ItemBuilder.create(id).stacksTo(stack);
                    if (fire) b.fireResistant();
                    regEv.items(b);
                    e.executePin(n, "Out", g, ctx);
                }).register();

        node(Categories.REGISTRY_BLOCKS, "registry.block.register_simple", "Register Block (Simple)")
                .in(execType, "In")
                .in(stringType, "Id").defaultValue("Id", "mybundle:my_block")
                .in(boolType, "Has Item").defaultValue("Has Item", true)
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> {
                    Object ev = ctx.getVar(CTX_REGISTRY_EVENT, Object.class);
                    if (!(ev instanceof RegistryEvent regEv)) {
                        LOGGER.warn("[Blueprint] Register Block: missing registry event context ({}).", CTX_REGISTRY_EVENT);
                        e.executePin(n, "Out", g, ctx);
                        return;
                    }
                    String idStr = ctx.resolvePinAs(n.inputPin("Id"), String.class, "");
                    Identifier id = Identifier.tryParse(idStr);
                    if (id == null) {
                        LOGGER.warn("[Blueprint] Register Block: invalid id '{}'", idStr);
                        e.executePin(n, "Out", g, ctx);
                        return;
                    }
                    boolean hasItem = ctx.resolvePinAs(n.inputPin("Has Item"), Boolean.class, true);
                    BlockBuilder b = BlockBuilder.create(id);
                    if (!hasItem) b.noItem();
                    regEv.blocks(b);
                    e.executePin(n, "Out", g, ctx);
                }).register();

        node(Categories.REGISTRY_SOUNDS, "registry.sound.register_simple", "Register Sound (Simple)")
                .in(execType, "In")
                .in(stringType, "Id").defaultValue("Id", "mybundle:my_sound")
                .in(stringType, "Subtitle").defaultValue("Subtitle", "")
                .in(floatType, "Range").defaultValue("Range", 16f)
                .out(execType, "Out")
                .behavior((n, e, g, ctx) -> {
                    Object ev = ctx.getVar(CTX_REGISTRY_EVENT, Object.class);
                    if (!(ev instanceof RegistryEvent regEv)) {
                        LOGGER.warn("[Blueprint] Register Sound: missing registry event context ({}).", CTX_REGISTRY_EVENT);
                        e.executePin(n, "Out", g, ctx);
                        return;
                    }
                    String idStr = ctx.resolvePinAs(n.inputPin("Id"), String.class, "");
                    Identifier id = Identifier.tryParse(idStr);
                    if (id == null) {
                        LOGGER.warn("[Blueprint] Register Sound: invalid id '{}'", idStr);
                        e.executePin(n, "Out", g, ctx);
                        return;
                    }
                    String subtitle = ctx.resolvePinAs(n.inputPin("Subtitle"), String.class, "").trim();
                    float range = ctx.resolvePinAs(n.inputPin("Range"), Float.class, 16f);
                    SoundBuilder b = SoundBuilder.create(id).range(range);
                    if (!subtitle.isEmpty()) b.subtitle(subtitle);
                    regEv.sounds(b);
                    e.executePin(n, "Out", g, ctx);
                }).register();
    }

    private void registerStringNodes() {
        node(Categories.STRINGS, "string.concat", "Concat")
                .in(stringType, "A").defaultValue("A", "")
                .in(stringType, "B").defaultValue("B", "")
                .out(stringType, "Result")
                .behavior((n, e, g, ctx) -> {
                    String a = ctx.resolvePinAs(n.inputPin("A"), String.class, "");
                    String b = ctx.resolvePinAs(n.inputPin("B"), String.class, "");
                    n.setOutput("Result", a + b);
                }).register();

        node(Categories.STRINGS, "string.equals", "Equals")
                .in(stringType, "A").defaultValue("A", "")
                .in(stringType, "B").defaultValue("B", "")
                .out(boolType, "Result")
                .behavior((n, e, g, ctx) -> {
                    String a = ctx.resolvePinAs(n.inputPin("A"), String.class, "");
                    String b = ctx.resolvePinAs(n.inputPin("B"), String.class, "");
                    n.setOutput("Result", a.equals(b));
                }).register();

        node(Categories.STRINGS, "string.is_empty", "Is Empty")
                .in(stringType, "Value").defaultValue("Value", "")
                .out(boolType, "Result")
                .behavior((n, e, g, ctx) -> {
                    String v = ctx.resolvePinAs(n.inputPin("Value"), String.class, "");
                    n.setOutput("Result", v.isEmpty());
                }).register();

        node(Categories.STRINGS, "string.length", "Length")
                .in(stringType, "Value").defaultValue("Value", "")
                .out(intType, "Length")
                .behavior((n, e, g, ctx) -> {
                    String v = ctx.resolvePinAs(n.inputPin("Value"), String.class, "");
                    n.setOutput("Length", v.length());
                }).register();
    }

    @FunctionalInterface
    public interface NodeBehavior {
        void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx);
    }

    public enum BuiltinNodes {
        EVENT_BEGIN_PLAY("event.begin_play", "BeginPlay"),
        EVENT_REGISTRY("event.registry", "Registry"),
        EVENT_VANILLA_GAME("event.vanilla_game", "Vanilla Game"),
        EVENT_COMMON_SETUP("event.common_setup", "Common Setup"),
        EVENT_CLIENT_SETUP("event.client_setup", "Client Setup"),
        EVENT_DEDICATED_SERVER_SETUP("event.dedicated_server_setup", "Dedicated Server Setup"),
        EVENT_POST_INIT("event.post_init", "Post Init"),
        EVENT_CLIENT_TICK("event.client_tick", "Client Tick"),
        EVENT_CLIENT_STOPPED("event.client_stopped", "Client Stopped"),
        EVENT_CLIENT_STOPPING("event.client_stopping", "Client Stopping"),
        EVENT_CHAT_MESSAGE("event.chat_message", "Chat Message"),
        EVENT_RENDER_GUI("event.render_gui", "Render GUI"),
        EVENT_CLIENT_LOGGED_IN("event.client_logged_in", "Client Logged In"),
        EVENT_CLIENT_LOGGED_OUT("event.client_logged_out", "Client Logged Out"),
        EVENT_SERVER_TICK("event.server_tick", "Server Tick"),
        EVENT_SERVER_ABOUT_TO_START("event.server_about_to_start", "Server About To Start"),
        EVENT_SERVER_STARTED("event.server_started", "Server Started"),
        EVENT_SERVER_STARTING("event.server_starting", "Server Starting"),
        EVENT_SERVER_STOPPED("event.server_stopped", "Server Stopped"),
        EVENT_SERVER_STOPPING("event.server_stopping", "Server Stopping"),
        EVENT_SERVER_TAGS("event.server_tags", "Server Tags"),
        EVENT_BLOCK_BROKEN("event.block_broken", "Block Broken"),
        EVENT_BLOCK_PLACED("event.block_placed", "Block Placed"),
        EVENT_BLOCK_LEFT_CLICKED("event.block_left_clicked", "Block Left Clicked"),
        EVENT_BLOCK_RIGHT_CLICKED("event.block_right_clicked", "Block Right Clicked"),
        EVENT_FARMLAND_TRAMPLED("event.farmland_trampled", "Farmland Trampled"),
        EVENT_ENTITY_JOIN_LEVEL("event.entity_join_level", "Entity Join Level"),
        EVENT_LIVING_DEATH("event.living_death", "Living Death"),
        EVENT_LIVING_DROPS("event.living_drops", "Living Drops"),
        EVENT_LIVING_HURT("event.living_hurt", "Living Hurt"),
        EVENT_ITEM_PICKUP("event.item_pickup", "Item Pickup"),
        EVENT_ITEM_DESTROY("event.item_destroy", "Item Destroy"),
        EVENT_ITEM_RIGHT_CLICK("event.item_right_click", "Item Right Click"),
        EVENT_ITEM_CRAFTED("event.item_crafted", "Item Crafted"),
        EVENT_ITEM_DROPPED("event.item_dropped", "Item Dropped"),
        EVENT_ITEM_FOOD_EATEN("event.item_food_eaten", "Item Food Eaten"),
        EVENT_ITEM_SMELTED("event.item_smelted", "Item Smelted"),
        EVENT_ITEM_TOOLTIP("event.item_tooltip", "Item Tooltip"),
        EVENT_ITEM_ENTITY_INTERACT("event.item_entity_interact", "Item Entity Interact"),
        EVENT_ITEM_FIRST_LEFT_CLICK("event.item_first_left_click", "Item First Left Click"),
        EVENT_ITEM_FIRST_RIGHT_CLICK("event.item_first_right_click", "Item First Right Click"),
        EVENT_LEVEL_LOAD("event.level_load", "Level Load"),
        EVENT_LEVEL_UNLOAD("event.level_unload", "Level Unload"),
        EVENT_LEVEL_SAVE("event.level_save", "Level Save"),
        EVENT_LEVEL_TICK("event.level_tick", "Level Tick"),
        EVENT_BEFORE_EXPLOSION("event.before_explosion", "Before Explosion"),
        EVENT_AFTER_EXPLOSION("event.after_explosion", "After Explosion"),
        EVENT_NETWORK_LOGIN("event.network_login", "Network Login"),
        EVENT_NETWORK_LOGOUT("event.network_logout", "Network Logout"),
        EVENT_PLAYER_LOGGED_IN("event.player_logged_in", "Player Logged In"),
        EVENT_PLAYER_LOGGED_OUT("event.player_logged_out", "Player Logged Out"),
        EVENT_PLAYER_TICK("event.player_tick", "Player Tick"),
        EVENT_PLAYER_CHAT("event.player_chat", "Player Chat"),
        EVENT_PLAYER_ADVANCEMENT("event.player_advancement", "Player Advancement"),
        EVENT_CHEST_CLOSED("event.chest_closed", "Chest Closed"),
        EVENT_CHEST_OPENED("event.chest_opened", "Chest Opened"),
        EVENT_PLAYER_RESPAWNED("event.player_respawned", "Player Respawned"),
        EVENT_DECORATE_CHAT("event.decorate_chat", "Decorate Chat"),
        EVENT_COMMANDS("event.commands", "Commands"),
        EVENT_COMMANDS_CLIENT("event.commands_client", "Client Commands"),
        EVENT_RECIPE_VIEWER_UPDATED("event.recipe_viewer_updated", "Recipe Viewer Updated"),
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
        public static final String EVENTS_BLOCK = "Events/Block";
        public static final String EVENTS_ENTITY = "Events/Entity";
        public static final String EVENTS_ITEM = "Events/Item";
        public static final String EVENTS_LEVEL = "Events/Level";
        public static final String EVENTS_NETWORK = "Events/Network";
        public static final String EVENTS_PLAYER = "Events/Player";
        public static final String EVENTS_COMMAND = "Events/Command";
        public static final String EVENTS_RECIPE = "Events/Recipe";
        public static final String EVENTS_CLIENT = "Events/Client";
        public static final String EVENTS_SERVER = "Events/Server";
        public static final String EVENTS_BUNDLE = "Events/Bundle";
        public static final String REGISTRY_ITEMS = "Registry/Items";
        public static final String REGISTRY_BLOCKS = "Registry/Blocks";
        public static final String REGISTRY_SOUNDS = "Registry/Sounds";
        public static final String VARIABLES = "Variables";
        public static final String UTILS = "Utilities";
        public static final String INPUTS = "Inputs";
        public static final String LOGIC = "Logic";
        public static final String MATH = "Math";
        public static final String STRINGS = "Strings";
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