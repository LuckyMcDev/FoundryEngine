package de.luckymcdev.foundryengine.common.blueprint.event;

import org.jetbrains.annotations.Nullable;

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
    EVENT_DATA_GEN("event.data_gen", "Data Gen"),
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
