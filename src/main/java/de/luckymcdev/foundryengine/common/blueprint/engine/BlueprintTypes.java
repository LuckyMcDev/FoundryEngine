package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinShape;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;
import de.luckymcdev.foundryengine.common.blueprint.nodes.PinRenderer;

import java.util.List;

public final class BlueprintTypes {

    public static final NodePinType<Boolean> BOOL = new NodePinType<>("Bool", NodePinShape.FILLED_CIRCLE,
            PinRenderer.boolPin());

    // ======================== Primitives ========================

    public static final NodePinType<Void> EXEC = new NodePinType<>("Exec", NodePinShape.FILLED_TRIANGLE);
    public static final NodePinType<Integer> INT = new NodePinType<>("Int", NodePinShape.FILLED_CIRCLE,
            PinRenderer.intPin());
    public static final NodePinType<Float> FLOAT = new NodePinType<>("Float", NodePinShape.FILLED_CIRCLE,
            PinRenderer.floatPin(0.1f));
    public static final NodePinType<String> STRING = new NodePinType<>("String", NodePinShape.FILLED_CIRCLE,
            PinRenderer.stringPin(256));
    public static final NodePinType<Object> ENTITY = new NodePinType<>("Entity", NodePinShape.FILLED_SQUARE, "Selector");
    public static final NodePinType<Object> OBJECT = new NodePinType<>("Object", NodePinShape.FILLED_SQUARE);
    public static final NodePinType<Object> ANY = new NodePinType<>("Any", NodePinShape.CIRCLE);

    // ======================== Minecraft Object Types ========================
    // These carry actual Java objects (Entity, Player, etc.)

    public static final NodePinType<Object> MINECRAFT_SERVER = reg("MinecraftServer");
    public static final NodePinType<Object> MINECRAFT = reg("Minecraft");
    public static final NodePinType<Object> PLAYER = new NodePinType<>("Player", NodePinShape.FILLED_SQUARE, "Entity", "Selector");
    public static final NodePinType<Object> LIVING_ENTITY = new NodePinType<>("LivingEntity", NodePinShape.FILLED_SQUARE, "Entity", "Selector");
    public static final NodePinType<Object> VEC3 = new NodePinType<>("Vec3", NodePinShape.FILLED_SQUARE, "Position", "Coordinate");
    public static final NodePinType<Object> LEVEL = reg("Level");
    public static final NodePinType<Object> ITEM_STACK = new NodePinType<>("ItemStack", NodePinShape.FILLED_SQUARE, "ItemEntity");
    public static final NodePinType<Object> BLOCK_STATE = reg("BlockState");
    public static final NodePinType<Object> DIRECTION = reg("Direction");
    public static final NodePinType<Object> ITEM_ENTITY = new NodePinType<>("ItemEntity", NodePinShape.FILLED_SQUARE, "ItemStack");
    public static final NodePinType<Object> ENTITY_TYPE = reg("EntityType");
    public static final NodePinType<Object> COMPONENT = reg("Component");
    public static final NodePinType<Object> ADVANCEMENT = reg("Advancement");
    public static final NodePinType<Object> CONTAINER = reg("Container");
    public static final NodePinType<Object> EXPLOSION = reg("Explosion");
    public static final NodePinType<Object> DAMAGE_SOURCE = reg("DamageSource");
    public static final NodePinType<Object> CONNECTION = reg("Connection");
    public static final NodePinType<Object> INTERACTION_HAND = reg("InteractionHand");
    public static final NodePinType<Object> PLAYER_ACTION = reg("PlayerAction");
    public static final NodePinType<Object> UPDATE_CAUSE = reg("UpdateCause");
    public static final NodePinType<Object> LOOKUP_PROVIDER = reg("LookupProvider");
    public static final NodePinType<Object> TOOLTIP_FLAG = reg("TooltipFlag");
    public static final NodePinType<Object> COMMAND_DISPATCHER = reg("CommandDispatcher");
    public static final NodePinType<Object> EFFECT = reg("Effect");
    public static final NodePinType<Object> ENCHANTMENT = reg("Enchantment");
    public static final NodePinType<Object> PARTICLE = reg("Particle");
    public static final NodePinType<Object> SOUND_EVENT = reg("SoundEvent");
    public static final NodePinType<Object> RECIPE = reg("Recipe");
    public static final NodePinType<Object> COMMAND_CONTEXT = reg("CommandContext");
    /**
     * Entity selector string (@p, @a, @e[...], UUID, player name).
     * Compatible with: Entity, Player, LivingEntity outputs.
     */
    public static final NodePinType<String> SELECTOR = new NodePinType<>("Selector",
            List.of("@p", "@a", "@r", "@s", "@e"),
            "Entity", "Player", "LivingEntity");

    // ======================== Command Parameter Types ========================
    // These are string-based representations of game objects.
    // They are compatible with their object counterparts for easy wiring.
    /**
     * Single coordinate axis value.
     */
    public static final NodePinType<Object> COORD = new NodePinType<>("Coordinate",
            NodePinShape.FILLED_CIRCLE,
            "Int", "Float");
    /**
     * NBT data as string.
     */
    public static final NodePinType<String> NBT = new NodePinType<>("NBT", NodePinShape.FILLED_CIRCLE);
    public static final NodePinType<String> GAMEMODE = new NodePinType<>("GameMode",
            List.of("survival", "creative", "adventure", "spectator"));

    // ======================== Enum Types (Combo Box) ========================
    public static final NodePinType<String> ANCHOR = new NodePinType<>("Anchor",
            List.of("feet", "eyes"));
    public static final NodePinType<String> FILL_MODE = new NodePinType<>("Fill Mode",
            List.of("destroy", "hollow", "keep", "outline", "replace"));
    public static final NodePinType<String> CLONE_MODE = new NodePinType<>("Clone Mode",
            List.of("force", "move", "normal", "filtered"));
    public static final NodePinType<String> TITLE_TYPE = new NodePinType<>("Title Type",
            List.of("title", "subtitle", "actionbar"));
    public static final NodePinType<String> DIFFICULTY = new NodePinType<>("Difficulty",
            List.of("peaceful", "easy", "normal", "hard"));
    public static final NodePinType<String> SOUND_SOURCE = new NodePinType<>("Sound Source",
            List.of("master", "music", "record", "weather", "block", "voice", "neutral", "player", "ambient", "hostile"));
    public static final NodePinType<String> TIME_TYPE = new NodePinType<>("Time Type",
            List.of("daytime", "gametime", "day"));
    /**
     * Weather type preset.
     */
    public static final NodePinType<String> WEATHER = new NodePinType<>("Weather",
            List.of("clear", "rain", "thunder"));
    /**
     * Sort mode for entity selectors.
     */
    public static final NodePinType<String> SORT_MODE = new NodePinType<>("Sort Mode",
            List.of("nearest", "furthest", "random", "arbitrary"));
    /**
     * Time unit for commands.
     */
    public static final NodePinType<String> TIME_UNIT = new NodePinType<>("Time Unit",
            List.of("s", "t", "d"));

    private BlueprintTypes() {
    }

    // ======================== Helper ========================

    private static NodePinType<Object> reg(String name, String... compatibleWith) {
        return new NodePinType<>(name, NodePinShape.FILLED_SQUARE, compatibleWith);
    }
}
