package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinShape;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;

/**
 * Central catalogue of built-in {@link NodePinType} instances that mirror
 * Unreal Engine 5's visual language.
 */
public final class BlueprintTypes {
    public static final NodePinType<Void> EXEC = new NodePinType<>("Exec", NodePinShape.FILLED_TRIANGLE);
    public static final NodePinType<Boolean> BOOL = new NodePinType<>("Bool", NodePinShape.FILLED_CIRCLE);
    public static final NodePinType<Integer> INT = new NodePinType<>("Int", NodePinShape.FILLED_CIRCLE);
    public static final NodePinType<Float> FLOAT = new NodePinType<>("Float", NodePinShape.FILLED_CIRCLE);
    public static final NodePinType<String> STRING = new NodePinType<>("String", NodePinShape.FILLED_CIRCLE);
    public static final NodePinType<Object> OBJECT = new NodePinType<>("Object", NodePinShape.FILLED_SQUARE);
    public static final NodePinType<Object> ANY = new NodePinType<>("Any", NodePinShape.CIRCLE);
    public static final NodePinType<Object> MINECRAFT_SERVER = reg("MinecraftServer");
    public static final NodePinType<Object> MINECRAFT = reg("Minecraft");
    public static final NodePinType<Object> PLAYER = reg("Player");
    public static final NodePinType<Object> ENTITY = reg("Entity");
    public static final NodePinType<Object> LIVING_ENTITY = reg("LivingEntity");
    public static final NodePinType<Object> LEVEL = reg("Level");
    public static final NodePinType<Object> BLOCK_POS = reg("BlockPos");
    public static final NodePinType<Object> BLOCK_STATE = reg("BlockState");
    public static final NodePinType<Object> DIRECTION = reg("Direction");
    public static final NodePinType<Object> ITEM_STACK = reg("ItemStack");
    public static final NodePinType<Object> ITEM_ENTITY = reg("ItemEntity");
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

    private static NodePinType<Object> reg(String name) {
        return new NodePinType<>(name, NodePinShape.FILLED_SQUARE);
    }
}