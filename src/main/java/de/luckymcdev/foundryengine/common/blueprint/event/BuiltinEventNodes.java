package de.luckymcdev.foundryengine.common.blueprint.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;

public final class BuiltinEventNodes {

    private BuiltinEventNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        setup(engine, BuiltinNodes.EVENT_BEGIN_PLAY.id, "BeginPlay");
        setup(engine, BuiltinNodes.EVENT_REGISTRY.id, "Registry");
        setup(engine, BuiltinNodes.EVENT_VANILLA_GAME.id, "Vanilla Game");
        setup(engine, BuiltinNodes.EVENT_COMMON_SETUP.id, "Common Setup");
        setup(engine, BuiltinNodes.EVENT_CLIENT_SETUP.id, "Client Setup");
        setup(engine, BuiltinNodes.EVENT_DEDICATED_SERVER_SETUP.id, "Dedicated Server Setup");
        setup(engine, BuiltinNodes.EVENT_POST_INIT.id, "Post Init");
        setup(engine, BuiltinNodes.EVENT_DATA_GEN.id, "Data Gen");
        setup(engine, BuiltinNodes.EVENT_RECIPE_VIEWER_UPDATED.id, "Recipe Viewer Updated");

        client(engine, BuiltinNodes.EVENT_CLIENT_TICK.id, "Client Tick", node -> {
            node.output(BlueprintTypes.MINECRAFT, "Client");
        });
        client(engine, BuiltinNodes.EVENT_CLIENT_STOPPED.id, "Client Stopped", node -> {
            node.output(BlueprintTypes.MINECRAFT, "Client");
        });
        client(engine, BuiltinNodes.EVENT_CLIENT_STOPPING.id, "Client Stopping", node -> {
            node.output(BlueprintTypes.MINECRAFT, "Client");
        });
        client(engine, BuiltinNodes.EVENT_CHAT_MESSAGE.id, "Chat Message", node -> {
            node.output(BlueprintTypes.STRING, "Message");
            node.output(BlueprintTypes.STRING, "OriginalMessage");
        });
        client(engine, BuiltinNodes.EVENT_RENDER_GUI.id, "Render GUI", node -> {
            node.output(BlueprintTypes.FLOAT, "PartialTicks");
        });
        client(engine, BuiltinNodes.EVENT_CLIENT_LOGGED_IN.id, "Client Logged In", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.CONNECTION, "Connection");
        });
        client(engine, BuiltinNodes.EVENT_CLIENT_LOGGED_OUT.id, "Client Logged Out", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
        });

        server(engine, BuiltinNodes.EVENT_SERVER_TICK.id, "Server Tick", node -> {
            node.output(BlueprintTypes.MINECRAFT_SERVER, "Server");
        });
        server(engine, BuiltinNodes.EVENT_SERVER_ABOUT_TO_START.id, "Server About To Start", node -> {
            node.output(BlueprintTypes.MINECRAFT_SERVER, "Server");
        });
        server(engine, BuiltinNodes.EVENT_SERVER_STARTED.id, "Server Started", node -> {
            node.output(BlueprintTypes.MINECRAFT_SERVER, "Server");
        });
        server(engine, BuiltinNodes.EVENT_SERVER_STARTING.id, "Server Starting", node -> {
            node.output(BlueprintTypes.MINECRAFT_SERVER, "Server");
        });
        server(engine, BuiltinNodes.EVENT_SERVER_STOPPED.id, "Server Stopped", node -> {
            node.output(BlueprintTypes.MINECRAFT_SERVER, "Server");
        });
        server(engine, BuiltinNodes.EVENT_SERVER_STOPPING.id, "Server Stopping", node -> {
            node.output(BlueprintTypes.MINECRAFT_SERVER, "Server");
        });
        server(engine, BuiltinNodes.EVENT_SERVER_TAGS.id, "Server Tags", node -> {
            node.output(BlueprintTypes.LOOKUP_PROVIDER, "LookupProvider");
            node.output(BlueprintTypes.UPDATE_CAUSE, "UpdateCause");
        });

        block(engine, BuiltinNodes.EVENT_BLOCK_BROKEN.id, "Block Broken", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.VEC3, "Pos");
            node.output(BlueprintTypes.BLOCK_STATE, "BlockState");
        });
        block(engine, BuiltinNodes.EVENT_BLOCK_PLACED.id, "Block Placed", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.VEC3, "Pos");
            node.output(BlueprintTypes.BLOCK_STATE, "BlockState");
        });
        block(engine, BuiltinNodes.EVENT_BLOCK_LEFT_CLICKED.id, "Block Left Clicked", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.VEC3, "Pos");
            node.output(BlueprintTypes.DIRECTION, "Direction");
            node.output(BlueprintTypes.OBJECT, "Action");
        });
        block(engine, BuiltinNodes.EVENT_BLOCK_RIGHT_CLICKED.id, "Block Right Clicked", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.VEC3, "Pos");
            node.output(BlueprintTypes.DIRECTION, "Direction");
        });
        block(engine, BuiltinNodes.EVENT_FARMLAND_TRAMPLED.id, "Farmland Trampled", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.VEC3, "Pos");
            node.output(BlueprintTypes.FLOAT, "FallDistance");
        });

        entity(engine, BuiltinNodes.EVENT_ENTITY_JOIN_LEVEL.id, "Entity Join Level", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.LEVEL, "Level");
        });
        entity(engine, BuiltinNodes.EVENT_LIVING_DEATH.id, "Living Death", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.DAMAGE_SOURCE, "DamageSource");
        });
        entity(engine, BuiltinNodes.EVENT_LIVING_DROPS.id, "Living Drops", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.DAMAGE_SOURCE, "DamageSource");
        });
        entity(engine, BuiltinNodes.EVENT_LIVING_HURT.id, "Living Hurt", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.DAMAGE_SOURCE, "DamageSource");
        });

        item(engine, BuiltinNodes.EVENT_ITEM_PICKUP.id, "Item Pickup", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.ITEM_STACK, "CurrentStack");
            node.output(BlueprintTypes.ITEM_STACK, "OriginalStack");
            node.output(BlueprintTypes.ITEM_ENTITY, "ItemEntity");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_DESTROY.id, "Item Destroy", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
            node.output(BlueprintTypes.INTERACTION_HAND, "Hand");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_RIGHT_CLICK.id, "Item Right Click", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.VEC3, "Pos");
            node.output(BlueprintTypes.INTERACTION_HAND, "Hand");
            node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_CRAFTED.id, "Item Crafted", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.ITEM_STACK, "Crafting");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_DROPPED.id, "Item Dropped", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.ITEM_ENTITY, "ItemEntity");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_FOOD_EATEN.id, "Item Food Eaten", node -> {
            node.output(BlueprintTypes.LIVING_ENTITY, "LivingEntity");
            node.output(BlueprintTypes.ITEM_STACK, "OriginalStack");
            node.output(BlueprintTypes.ITEM_STACK, "ResultStack");
            node.output(BlueprintTypes.INT, "Duration");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_SMELTED.id, "Item Smelted", node -> {
            node.output(BlueprintTypes.LIVING_ENTITY, "LivingEntity");
            node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
            node.output(BlueprintTypes.INT, "AmountRemoved");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_TOOLTIP.id, "Item Tooltip", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.CONTAINER, "TooltipContext");
            node.output(BlueprintTypes.TOOLTIP_FLAG, "TooltipFlag");
            node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_ENTITY_INTERACT.id, "Item Entity Interact", node -> {
            node.output(BlueprintTypes.ENTITY, "Entity");
            node.output(BlueprintTypes.ENTITY, "Target");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_FIRST_LEFT_CLICK.id, "Item First Left Click", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.INTERACTION_HAND, "Hand");
            node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
        });
        item(engine, BuiltinNodes.EVENT_ITEM_FIRST_RIGHT_CLICK.id, "Item First Right Click", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.INTERACTION_HAND, "Hand");
            node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
        });

        level(engine, BuiltinNodes.EVENT_LEVEL_LOAD.id, "Level Load", node -> {
            node.output(BlueprintTypes.LEVEL, "Level");
        });
        level(engine, BuiltinNodes.EVENT_LEVEL_UNLOAD.id, "Level Unload", node -> {
            node.output(BlueprintTypes.LEVEL, "Level");
        });
        level(engine, BuiltinNodes.EVENT_LEVEL_SAVE.id, "Level Save", node -> {
            node.output(BlueprintTypes.LEVEL, "Level");
        });
        level(engine, BuiltinNodes.EVENT_LEVEL_TICK.id, "Level Tick", node -> {
            node.output(BlueprintTypes.LEVEL, "Level");
        });

        level(engine, BuiltinNodes.EVENT_BEFORE_EXPLOSION.id, "Before Explosion", node -> {
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.EXPLOSION, "Explosion");
        });
        level(engine, BuiltinNodes.EVENT_AFTER_EXPLOSION.id, "After Explosion", node -> {
            node.output(BlueprintTypes.LEVEL, "Level");
            node.output(BlueprintTypes.EXPLOSION, "Explosion");
        });

        player(engine, BuiltinNodes.EVENT_PLAYER_LOGGED_IN.id, "Player Logged In", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
        });
        player(engine, BuiltinNodes.EVENT_PLAYER_LOGGED_OUT.id, "Player Logged Out", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
        });
        player(engine, BuiltinNodes.EVENT_PLAYER_TICK.id, "Player Tick", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
        });
        player(engine, BuiltinNodes.EVENT_PLAYER_CHAT.id, "Player Chat", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.STRING, "Username");
            node.output(BlueprintTypes.STRING, "Message");
            node.output(BlueprintTypes.STRING, "RawText");
        });
        player(engine, BuiltinNodes.EVENT_PLAYER_ADVANCEMENT.id, "Player Advancement", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.ADVANCEMENT, "Advancement");
        });
        player(engine, BuiltinNodes.EVENT_CHEST_CLOSED.id, "Chest Closed", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.CONTAINER, "Container");
        });
        player(engine, BuiltinNodes.EVENT_CHEST_OPENED.id, "Chest Opened", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
            node.output(BlueprintTypes.CONTAINER, "Container");
        });
        player(engine, BuiltinNodes.EVENT_PLAYER_RESPAWNED.id, "Player Respawned", node -> {
            node.output(BlueprintTypes.PLAYER, "Player");
        });

        misc(engine, BuiltinNodes.EVENT_DECORATE_CHAT.id, "Decorate Chat", node -> {
            node.output(BlueprintTypes.COMPONENT, "Message");
        });

        misc(engine, BuiltinNodes.EVENT_COMMANDS.id, "Commands", node -> {
            node.output(BlueprintTypes.COMMAND_DISPATCHER, "Dispatcher");
        });
        misc(engine, BuiltinNodes.EVENT_COMMANDS_CLIENT.id, "Client Commands", node -> {
            node.output(BlueprintTypes.COMMAND_DISPATCHER, "Dispatcher");
        });
    }

    private static void setup(BlueprintEngine engine, String id, String name) {
        event(engine, id, name, "Events/Setup", node -> {});
    }

    private static void setup(BlueprintEngine engine, String id, String name,
                               java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Setup", pinDefiner);
    }

    private static void client(BlueprintEngine engine, String id, String name,
                                java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Client", pinDefiner);
    }

    private static void server(BlueprintEngine engine, String id, String name,
                                java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Server", pinDefiner);
    }

    private static void block(BlueprintEngine engine, String id, String name,
                               java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Block", pinDefiner);
    }

    private static void entity(BlueprintEngine engine, String id, String name,
                                java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Entity", pinDefiner);
    }

    private static void item(BlueprintEngine engine, String id, String name,
                              java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Item", pinDefiner);
    }

    private static void level(BlueprintEngine engine, String id, String name,
                               java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Level", pinDefiner);
    }

    private static void player(BlueprintEngine engine, String id, String name,
                                java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Player", pinDefiner);
    }

    private static void misc(BlueprintEngine engine, String id, String name,
                              java.util.function.Consumer<BuiltinNode> pinDefiner) {
        event(engine, id, name, "Events/Misc", pinDefiner);
    }

    private static void event(BlueprintEngine engine, String id, String name, String category,
                               java.util.function.Consumer<BuiltinNode> pinDefiner) {
        engine.register(BuiltinNode.create(id, name, category, node -> {
            node.execOutput("Exec");
            pinDefiner.accept(node);
        },
                (node, eng, graph, ctx) -> {
                }));
    }
}
