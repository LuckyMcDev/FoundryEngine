package de.luckymcdev.foundryengine.common.blueprint.engine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinInfo;
import de.luckymcdev.foundryengine.common.blueprint.nodes.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class BlueprintEngine {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String CTX_REGISTRY_EVENT = "_registry_event";

    private final List<BuiltinNode> builtinNodes = new ArrayList<>();
    private final Map<String, BuiltinNode> builtinById = new HashMap<>();
    private final Map<String, Integer> categoryColors = new HashMap<>();

    public void register(BuiltinNode node) {
        builtinNodes.add(node);
        builtinById.put(node.identifier, node);
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

    public List<BuiltinNode> getBuiltinNodes() {
        return Collections.unmodifiableList(builtinNodes);
    }

    public @Nullable BuiltinNode getById(String identifier) {
        return builtinById.get(identifier);
    }

    public BlueprintNode createNode(BuiltinNode builtin) {
        return builtin.createNode();
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
                        if (out.pin.type() == BlueprintTypes.EXEC) continue;
                        Object v = payload.get(out.pin.label());
                        if (v != null) node.setOutput(out.pin.label(), v);
                    }
                }
                executeNext(node, graph, ctx);
            }
        }
    }

    public void executeNext(BlueprintNode node, BlueprintGraph graph, BlueprintContext ctx) {
        BuiltinNode builtin = builtinById.get(node.identifier);
        if (builtin != null) {
            builtin.execute(node, this, graph, ctx);
            if (Categories.LOGIC.equals(node.category)) return;
        }
        for (var pin : node.outputPins) {
            if (pin.pin.type() == BlueprintTypes.EXEC) {
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

        // Bundle events
        register(new EventNodes.BeginPlay());
        register(new EventNodes.Registry());
        register(new EventNodes.VanillaGame());
        register(new EventNodes.CommonSetup());
        register(new EventNodes.ClientSetup());
        register(new EventNodes.DedicatedServerSetup());
        register(new EventNodes.PostInit());

        // Client events
        register(new EventNodes.ClientTick());
        register(new EventNodes.ClientStopped());
        register(new EventNodes.ClientStopping());
        register(new EventNodes.ChatMessage());
        register(new EventNodes.RenderGui());
        register(new EventNodes.ClientLoggedIn());
        register(new EventNodes.ClientLoggedOut());

        // Server events
        register(new EventNodes.ServerTick());
        register(new EventNodes.ServerAboutToStart());
        register(new EventNodes.ServerStarted());
        register(new EventNodes.ServerStarting());
        register(new EventNodes.ServerStopped());
        register(new EventNodes.ServerStopping());
        register(new EventNodes.ServerTags());

        // Block events
        register(new EventNodes.BlockBroken());
        register(new EventNodes.BlockPlaced());
        register(new EventNodes.BlockLeftClicked());
        register(new EventNodes.BlockRightClicked());
        register(new EventNodes.FarmlandTrampled());

        // Entity events
        register(new EventNodes.EntityJoinLevel());
        register(new EventNodes.LivingDeath());
        register(new EventNodes.LivingDrops());
        register(new EventNodes.LivingHurt());

        // Item events
        register(new EventNodes.ItemPickup());
        register(new EventNodes.ItemDestroy());
        register(new EventNodes.ItemRightClick());
        register(new EventNodes.ItemCrafted());
        register(new EventNodes.ItemDropped());
        register(new EventNodes.ItemFoodEaten());
        register(new EventNodes.ItemSmelted());
        register(new EventNodes.ItemTooltip());
        register(new EventNodes.ItemEntityInteract());
        register(new EventNodes.ItemFirstLeftClick());
        register(new EventNodes.ItemFirstRightClick());

        // Level events
        register(new EventNodes.LevelLoad());
        register(new EventNodes.LevelUnload());
        register(new EventNodes.LevelSave());
        register(new EventNodes.LevelTick());
        register(new EventNodes.BeforeExplosion());
        register(new EventNodes.AfterExplosion());

        // Network events
        register(new EventNodes.NetworkLogin());
        register(new EventNodes.NetworkLogout());

        // Player events
        register(new EventNodes.PlayerLoggedIn());
        register(new EventNodes.PlayerLoggedOut());
        register(new EventNodes.PlayerTick());
        register(new EventNodes.PlayerChat());
        register(new EventNodes.PlayerAdvancement());
        register(new EventNodes.ChestClosed());
        register(new EventNodes.ChestOpened());
        register(new EventNodes.PlayerRespawned());
        register(new EventNodes.DecorateChat());

        // Command events
        register(new EventNodes.Commands());
        register(new EventNodes.ClientCommands());

        // Recipe events
        register(new EventNodes.RecipeViewerUpdated());

        // Logic nodes
        register(new LogicNodes.If());
        register(new LogicNodes.IfElse());
        register(new LogicNodes.Repeat());
        register(new LogicNodes.RepeatUntil());
        register(new LogicNodes.ForRange());
        register(new LogicNodes.Sequence());
        register(new LogicNodes.RerouteExec());
        register(new LogicNodes.RerouteAny());
        register(new LogicNodes.Not());
        register(new LogicNodes.And());
        register(new LogicNodes.Or());

        // Math nodes
        register(new MathNodes.IntAdd());
        register(new MathNodes.IntSub());
        register(new MathNodes.IntMul());
        register(new MathNodes.IntEquals());
        register(new MathNodes.IntMod());
        register(new MathNodes.FloatAdd());
        register(new MathNodes.FloatSub());
        register(new MathNodes.FloatMul());
        register(new MathNodes.FloatDiv());

        // Variable nodes
        register(new VariableNodes.SetVariable());
        register(new VariableNodes.GetVariable());

        // Input nodes
        register(new InputNodes.StringInput());
        register(new InputNodes.IntegerInput());
        register(new InputNodes.BooleanInput());

        // Utility nodes
        register(new UtilityNodes.PrintString());
        register(new UtilityNodes.Tell());

        // Registry nodes
        register(new RegistryNodes.RegisterItem());
        register(new RegistryNodes.RegisterBlock());
        register(new RegistryNodes.RegisterSound());

        // String nodes
        register(new StringNodes.Concat());
        register(new StringNodes.Equals());
        register(new StringNodes.IsEmpty());
        register(new StringNodes.Length());

        // Editor nodes
        register(new CommentNode());
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

}