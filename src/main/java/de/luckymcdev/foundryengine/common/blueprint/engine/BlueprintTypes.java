package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinShape;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;
import de.luckymcdev.foundryengine.common.blueprint.nodes.PinRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlueprintTypes {

    private static final Map<String, NodePinType<?>> TYPE_REGISTRY = new HashMap<>();

    // ======================== Primitives ========================
    public static final NodePinType<Boolean> BOOL = reg("Bool", NodePinShape.FILLED_CIRCLE,
            PinRenderer.boolPin());
    public static final NodePinType<Void> EXEC = reg("Exec", NodePinShape.FILLED_TRIANGLE);
    public static final NodePinType<Integer> INT = reg("Int", NodePinShape.FILLED_CIRCLE,
            PinRenderer.intPin());
    public static final NodePinType<Float> FLOAT = reg("Float", NodePinShape.FILLED_CIRCLE,
            PinRenderer.floatPin(0.1f));
    public static final NodePinType<String> STRING = reg("String", NodePinShape.FILLED_CIRCLE,
            PinRenderer.stringPin(256));
    public static final NodePinType<Object> ENTITY = reg("Entity", NodePinShape.FILLED_SQUARE, "Selector");
    public static final NodePinType<Object> OBJECT = reg("Object", NodePinShape.FILLED_SQUARE);

    // ======================== Minecraft Object Types ========================

    public static final NodePinType<Object> MINECRAFT_SERVER = reg("MinecraftServer");
    public static final NodePinType<Object> MINECRAFT = reg("Minecraft");
    public static final NodePinType<Object> ANY = reg("Any", NodePinShape.CIRCLE);
    public static final NodePinType<Object> PLAYER = reg("Player", NodePinShape.FILLED_SQUARE, "Entity", "Selector");
    public static final NodePinType<Object> LIVING_ENTITY = reg("LivingEntity", NodePinShape.FILLED_SQUARE, "Entity", "Selector");
    public static final NodePinType<Object> LEVEL = reg("Level");
    public static final NodePinType<Object> VEC3 = reg("Vec3", NodePinShape.FILLED_SQUARE, "Position", "Coordinate");
    public static final NodePinType<Object> VEC2 = reg("Vec2", NodePinShape.FILLED_SQUARE, "Position2D", "Coordinate2D");
    public static final NodePinType<Object> COMMAND_SOURCE = reg("CommandSource", NodePinShape.FILLED_SQUARE);
    public static final NodePinType<Object> BLOCK_STATE = reg("BlockState");
    public static final NodePinType<Object> DIRECTION = reg("Direction");
    public static final NodePinType<Object> ITEM_STACK = reg("ItemStack", NodePinShape.FILLED_SQUARE, "ItemEntity");
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
    public static final NodePinType<Object> ITEM_ENTITY = reg("ItemEntity", NodePinShape.FILLED_SQUARE, "ItemStack");

    // ======================== Groovy-Oriented Types ========================
    public static final NodePinType<Object> GSTRING = reg("GString",
            NodePinShape.FILLED_CIRCLE,
            PinRenderer.stringPin(4096), "GString", "String");
    public static final NodePinType<Object> CLOSURE = reg("Closure",
            NodePinShape.FILLED_SQUARE);
    public static final NodePinType<Object> LIST = reg("List",
            NodePinShape.FILLED_SQUARE);
    public static final NodePinType<Object> MAP = reg("Map",
            NodePinShape.FILLED_SQUARE);
    public static final NodePinType<Object> RANGE = reg("Range",
            NodePinShape.FILLED_CIRCLE, "Int", "Float");

    // ======================== Command Parameter Types ========================
    public static final NodePinType<String> SELECTOR = reg("Selector",
            List.of("@p", "@a", "@r", "@s", "@e"),
            "Entity", "Player", "LivingEntity");
    public static final NodePinType<Object> COORD = reg("Coordinate",
            NodePinShape.FILLED_CIRCLE,
            "Int", "Float");
    public static final NodePinType<String> NBT = reg("NBT", NodePinShape.FILLED_CIRCLE);

    // ======================== Enum Types (Combo Box) ========================
    public static final NodePinType<String> GAMEMODE = reg("GameMode",
            List.of("survival", "creative", "adventure", "spectator"));
    public static final NodePinType<String> ANCHOR = reg("Anchor",
            List.of("feet", "eyes"));
    public static final NodePinType<String> FILL_MODE = reg("Fill Mode",
            List.of("destroy", "hollow", "keep", "outline", "replace"));
    public static final NodePinType<String> CLONE_MODE = reg("Clone Mode",
            List.of("force", "move", "normal", "filtered"));
    public static final NodePinType<String> TITLE_TYPE = reg("Title Type",
            List.of("title", "subtitle", "actionbar"));
    public static final NodePinType<String> DIFFICULTY = reg("Difficulty",
            List.of("peaceful", "easy", "normal", "hard"));
    public static final NodePinType<String> SOUND_SOURCE = reg("Sound Source",
            List.of("master", "music", "record", "weather", "block", "voice", "neutral", "player", "ambient", "hostile"));
    public static final NodePinType<String> TIME_TYPE = reg("Time Type",
            List.of("daytime", "gametime", "day"));
    public static final NodePinType<String> WEATHER = reg("Weather",
            List.of("clear", "rain", "thunder"));
    public static final NodePinType<String> SORT_MODE = reg("Sort Mode",
            List.of("nearest", "furthest", "random", "arbitrary"));
    public static final NodePinType<String> TIME_UNIT = reg("Time Unit",
            List.of("s", "t", "d"));

    private BlueprintTypes() {
    }

    private static void addToRegistry(NodePinType<?> type, String... altNames) {
        TYPE_REGISTRY.put(type.displayName, type);
        for (String alt : altNames) TYPE_REGISTRY.put(alt, type);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> NodePinType<T> reg(String name, String... compatibleWith) {
        NodePinType<?> type = new NodePinType<>(name, NodePinShape.FILLED_SQUARE, compatibleWith);
        addToRegistry(type);
        return (NodePinType<T>) type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> NodePinType<T> reg(String name, NodePinShape shape, String... compatibleWith) {
        NodePinType<?> type = new NodePinType<>(name, shape, compatibleWith);
        addToRegistry(type);
        return (NodePinType<T>) type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> NodePinType<T> reg(String name, NodePinShape shape, PinRenderer renderer) {
        NodePinType<?> type = new NodePinType<>(name, shape, renderer);
        addToRegistry(type);
        return (NodePinType<T>) type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> NodePinType<T> reg(String name, NodePinShape shape, PinRenderer renderer, String... compatibleWith) {
        NodePinType<?> type = new NodePinType<>(name, shape, renderer, compatibleWith);
        addToRegistry(type, compatibleWith);
        return (NodePinType<T>) type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> NodePinType<T> reg(String name, NodePinShape shape, List<String> enumValues, String... compatibleWith) {
        NodePinType<?> type = new NodePinType<>(name, shape, enumValues, compatibleWith);
        addToRegistry(type);
        return (NodePinType<T>) type;
    }

    // Enum type with no explicit shape (defaults to FILLED_CIRCLE)
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> NodePinType<T> reg(String name, List<String> enumValues, String... compatibleWith) {
        NodePinType<?> type = new NodePinType<>(name, enumValues, compatibleWith);
        addToRegistry(type);
        return (NodePinType<T>) type;
    }

    public static @Nullable NodePinType<?> byName(String name) {
        return TYPE_REGISTRY.get(name);
    }

    public static Map<String, NodePinType<?>> getRegisteredTypes() {
        return TYPE_REGISTRY;
    }
}
