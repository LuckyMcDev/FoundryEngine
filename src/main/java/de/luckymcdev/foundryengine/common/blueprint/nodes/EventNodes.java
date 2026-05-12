package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.FLOAT;
import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.INT;

public final class EventNodes {

    private EventNodes() {
    }

    // --- Bundle events ---

    public static final class BeginPlay extends BuiltinNode {
        public BeginPlay() {
            super(BlueprintEngine.BuiltinNodes.EVENT_BEGIN_PLAY.id, "BeginPlay", BlueprintEngine.Categories.EVENTS_BUNDLE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class Registry extends BuiltinNode {
        public Registry() {
            super(BlueprintEngine.BuiltinNodes.EVENT_REGISTRY.id, "Registry", BlueprintEngine.Categories.EVENTS_BUNDLE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class VanillaGame extends BuiltinNode {
        public VanillaGame() {
            super(BlueprintEngine.BuiltinNodes.EVENT_VANILLA_GAME.id, "Vanilla Game", BlueprintEngine.Categories.EVENTS_BUNDLE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class CommonSetup extends BuiltinNode {
        public CommonSetup() {
            super(BlueprintEngine.BuiltinNodes.EVENT_COMMON_SETUP.id, "Common Setup", BlueprintEngine.Categories.EVENTS_BUNDLE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ClientSetup extends BuiltinNode {
        public ClientSetup() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_SETUP.id, "Client Setup", BlueprintEngine.Categories.EVENTS_BUNDLE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class DedicatedServerSetup extends BuiltinNode {
        public DedicatedServerSetup() {
            super(BlueprintEngine.BuiltinNodes.EVENT_DEDICATED_SERVER_SETUP.id, "Dedicated Server Setup", BlueprintEngine.Categories.EVENTS_BUNDLE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class PostInit extends BuiltinNode {
        public PostInit() {
            super(BlueprintEngine.BuiltinNodes.EVENT_POST_INIT.id, "Post Init", BlueprintEngine.Categories.EVENTS_BUNDLE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Client events ---

    public static final class ClientTick extends BuiltinNode {
        public ClientTick() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_TICK.id, "Client Tick", BlueprintEngine.Categories.EVENTS_CLIENT);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
            output(INT, "Tick");
            output(FLOAT, "DeltaSeconds");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ClientStopped extends BuiltinNode {
        public ClientStopped() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_STOPPED.id, "Client Stopped", BlueprintEngine.Categories.EVENTS_CLIENT);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ClientStopping extends BuiltinNode {
        public ClientStopping() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_STOPPING.id, "Client Stopping", BlueprintEngine.Categories.EVENTS_CLIENT);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ChatMessage extends BuiltinNode {
        public ChatMessage() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CHAT_MESSAGE.id, "Chat Message", BlueprintEngine.Categories.EVENTS_CLIENT);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class RenderGui extends BuiltinNode {
        public RenderGui() {
            super(BlueprintEngine.BuiltinNodes.EVENT_RENDER_GUI.id, "Render GUI", BlueprintEngine.Categories.EVENTS_CLIENT);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ClientLoggedIn extends BuiltinNode {
        public ClientLoggedIn() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_LOGGED_IN.id, "Client Logged In", BlueprintEngine.Categories.EVENTS_CLIENT);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ClientLoggedOut extends BuiltinNode {
        public ClientLoggedOut() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_LOGGED_OUT.id, "Client Logged Out", BlueprintEngine.Categories.EVENTS_CLIENT);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Server events ---

    public static final class ServerTick extends BuiltinNode {
        public ServerTick() {
            super(BlueprintEngine.BuiltinNodes.EVENT_SERVER_TICK.id, "Server Tick", BlueprintEngine.Categories.EVENTS_SERVER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
            output(INT, "Tick");
            output(FLOAT, "DeltaSeconds");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ServerAboutToStart extends BuiltinNode {
        public ServerAboutToStart() {
            super(BlueprintEngine.BuiltinNodes.EVENT_SERVER_ABOUT_TO_START.id, "Server About To Start", BlueprintEngine.Categories.EVENTS_SERVER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ServerStarted extends BuiltinNode {
        public ServerStarted() {
            super(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTED.id, "Server Started", BlueprintEngine.Categories.EVENTS_SERVER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ServerStarting extends BuiltinNode {
        public ServerStarting() {
            super(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTING.id, "Server Starting", BlueprintEngine.Categories.EVENTS_SERVER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ServerStopped extends BuiltinNode {
        public ServerStopped() {
            super(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPED.id, "Server Stopped", BlueprintEngine.Categories.EVENTS_SERVER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ServerStopping extends BuiltinNode {
        public ServerStopping() {
            super(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPING.id, "Server Stopping", BlueprintEngine.Categories.EVENTS_SERVER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ServerTags extends BuiltinNode {
        public ServerTags() {
            super(BlueprintEngine.BuiltinNodes.EVENT_SERVER_TAGS.id, "Server Tags", BlueprintEngine.Categories.EVENTS_SERVER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Block events ---

    public static final class BlockBroken extends BuiltinNode {
        public BlockBroken() {
            super(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_BROKEN.id, "Block Broken", BlueprintEngine.Categories.EVENTS_BLOCK);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class BlockPlaced extends BuiltinNode {
        public BlockPlaced() {
            super(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_PLACED.id, "Block Placed", BlueprintEngine.Categories.EVENTS_BLOCK);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class BlockLeftClicked extends BuiltinNode {
        public BlockLeftClicked() {
            super(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_LEFT_CLICKED.id, "Block Left Clicked", BlueprintEngine.Categories.EVENTS_BLOCK);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class BlockRightClicked extends BuiltinNode {
        public BlockRightClicked() {
            super(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_RIGHT_CLICKED.id, "Block Right Clicked", BlueprintEngine.Categories.EVENTS_BLOCK);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class FarmlandTrampled extends BuiltinNode {
        public FarmlandTrampled() {
            super(BlueprintEngine.BuiltinNodes.EVENT_FARMLAND_TRAMPLED.id, "Farmland Trampled", BlueprintEngine.Categories.EVENTS_BLOCK);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Entity events ---

    public static final class EntityJoinLevel extends BuiltinNode {
        public EntityJoinLevel() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ENTITY_JOIN_LEVEL.id, "Entity Join Level", BlueprintEngine.Categories.EVENTS_ENTITY);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class LivingDeath extends BuiltinNode {
        public LivingDeath() {
            super(BlueprintEngine.BuiltinNodes.EVENT_LIVING_DEATH.id, "Living Death", BlueprintEngine.Categories.EVENTS_ENTITY);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class LivingDrops extends BuiltinNode {
        public LivingDrops() {
            super(BlueprintEngine.BuiltinNodes.EVENT_LIVING_DROPS.id, "Living Drops", BlueprintEngine.Categories.EVENTS_ENTITY);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class LivingHurt extends BuiltinNode {
        public LivingHurt() {
            super(BlueprintEngine.BuiltinNodes.EVENT_LIVING_HURT.id, "Living Hurt", BlueprintEngine.Categories.EVENTS_ENTITY);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Item events ---

    public static final class ItemPickup extends BuiltinNode {
        public ItemPickup() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_PICKUP.id, "Item Pickup", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemDestroy extends BuiltinNode {
        public ItemDestroy() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_DESTROY.id, "Item Destroy", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemRightClick extends BuiltinNode {
        public ItemRightClick() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_RIGHT_CLICK.id, "Item Right Click", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemCrafted extends BuiltinNode {
        public ItemCrafted() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_CRAFTED.id, "Item Crafted", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemDropped extends BuiltinNode {
        public ItemDropped() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_DROPPED.id, "Item Dropped", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemFoodEaten extends BuiltinNode {
        public ItemFoodEaten() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_FOOD_EATEN.id, "Item Food Eaten", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemSmelted extends BuiltinNode {
        public ItemSmelted() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_SMELTED.id, "Item Smelted", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemTooltip extends BuiltinNode {
        public ItemTooltip() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_TOOLTIP.id, "Item Tooltip", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemEntityInteract extends BuiltinNode {
        public ItemEntityInteract() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_ENTITY_INTERACT.id, "Item Entity Interact", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemFirstLeftClick extends BuiltinNode {
        public ItemFirstLeftClick() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_FIRST_LEFT_CLICK.id, "Item First Left Click", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ItemFirstRightClick extends BuiltinNode {
        public ItemFirstRightClick() {
            super(BlueprintEngine.BuiltinNodes.EVENT_ITEM_FIRST_RIGHT_CLICK.id, "Item First Right Click", BlueprintEngine.Categories.EVENTS_ITEM);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Level events ---

    public static final class LevelLoad extends BuiltinNode {
        public LevelLoad() {
            super(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_LOAD.id, "Level Load", BlueprintEngine.Categories.EVENTS_LEVEL);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class LevelUnload extends BuiltinNode {
        public LevelUnload() {
            super(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_UNLOAD.id, "Level Unload", BlueprintEngine.Categories.EVENTS_LEVEL);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class LevelSave extends BuiltinNode {
        public LevelSave() {
            super(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_SAVE.id, "Level Save", BlueprintEngine.Categories.EVENTS_LEVEL);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class LevelTick extends BuiltinNode {
        public LevelTick() {
            super(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_TICK.id, "Level Tick", BlueprintEngine.Categories.EVENTS_LEVEL);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class BeforeExplosion extends BuiltinNode {
        public BeforeExplosion() {
            super(BlueprintEngine.BuiltinNodes.EVENT_BEFORE_EXPLOSION.id, "Before Explosion", BlueprintEngine.Categories.EVENTS_LEVEL);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class AfterExplosion extends BuiltinNode {
        public AfterExplosion() {
            super(BlueprintEngine.BuiltinNodes.EVENT_AFTER_EXPLOSION.id, "After Explosion", BlueprintEngine.Categories.EVENTS_LEVEL);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Network events ---

    public static final class NetworkLogin extends BuiltinNode {
        public NetworkLogin() {
            super(BlueprintEngine.BuiltinNodes.EVENT_NETWORK_LOGIN.id, "Network Login", BlueprintEngine.Categories.EVENTS_NETWORK);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class NetworkLogout extends BuiltinNode {
        public NetworkLogout() {
            super(BlueprintEngine.BuiltinNodes.EVENT_NETWORK_LOGOUT.id, "Network Logout", BlueprintEngine.Categories.EVENTS_NETWORK);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Player events ---

    public static final class PlayerLoggedIn extends BuiltinNode {
        public PlayerLoggedIn() {
            super(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_LOGGED_IN.id, "Player Logged In", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class PlayerLoggedOut extends BuiltinNode {
        public PlayerLoggedOut() {
            super(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_LOGGED_OUT.id, "Player Logged Out", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class PlayerTick extends BuiltinNode {
        public PlayerTick() {
            super(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_TICK.id, "Player Tick", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class PlayerChat extends BuiltinNode {
        public PlayerChat() {
            super(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_CHAT.id, "Player Chat", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class PlayerAdvancement extends BuiltinNode {
        public PlayerAdvancement() {
            super(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_ADVANCEMENT.id, "Player Advancement", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ChestClosed extends BuiltinNode {
        public ChestClosed() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CHEST_CLOSED.id, "Chest Closed", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ChestOpened extends BuiltinNode {
        public ChestOpened() {
            super(BlueprintEngine.BuiltinNodes.EVENT_CHEST_OPENED.id, "Chest Opened", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class PlayerRespawned extends BuiltinNode {
        public PlayerRespawned() {
            super(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_RESPAWNED.id, "Player Respawned", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class DecorateChat extends BuiltinNode {
        public DecorateChat() {
            super(BlueprintEngine.BuiltinNodes.EVENT_DECORATE_CHAT.id, "Decorate Chat", BlueprintEngine.Categories.EVENTS_PLAYER);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Command events ---

    public static final class Commands extends BuiltinNode {
        public Commands() {
            super(BlueprintEngine.BuiltinNodes.EVENT_COMMANDS.id, "Commands", BlueprintEngine.Categories.EVENTS_COMMAND);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    public static final class ClientCommands extends BuiltinNode {
        public ClientCommands() {
            super(BlueprintEngine.BuiltinNodes.EVENT_COMMANDS_CLIENT.id, "Client Commands", BlueprintEngine.Categories.EVENTS_COMMAND);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }

    // --- Recipe events ---

    public static final class RecipeViewerUpdated extends BuiltinNode {
        public RecipeViewerUpdated() {
            super(BlueprintEngine.BuiltinNodes.EVENT_RECIPE_VIEWER_UPDATED.id, "Recipe Viewer Updated", BlueprintEngine.Categories.EVENTS_RECIPE);
        }

        @Override
        protected void initPins() {
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        }
    }
}
