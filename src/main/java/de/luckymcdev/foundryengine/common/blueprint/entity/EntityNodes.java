package de.luckymcdev.foundryengine.common.blueprint.entity;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class EntityNodes {

    private EntityNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerGetHealth(engine);
        registerGetMaxHealth(engine);
        registerSetHealth(engine);
        registerGetItemInHand(engine);
        registerGetMainHandItem(engine);
        registerGetPosition(engine);
        registerGetName(engine);
        registerIsAlive(engine);
        registerIsOnGround(engine);
        registerGetLevel(engine);
    }

    private static void registerGetHealth(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.health", "Get Health", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.FLOAT, "Health");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    float health = 0f;
                    if (obj instanceof LivingEntity le) health = le.getHealth();
                    n.setOutput("Health", health);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetMaxHealth(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.max_health", "Get Max Health", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.FLOAT, "MaxHealth");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    float max = 0f;
                    if (obj instanceof LivingEntity le) max = le.getMaxHealth();
                    n.setOutput("MaxHealth", max);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerSetHealth(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.set_health", "Set Health", "Entity",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.input(BlueprintTypes.FLOAT, "Health", 20f);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    float health = ctx.resolvePinAs(n.inputPin("Health"), Float.class, 20f);
                    if (obj instanceof LivingEntity le) le.setHealth(health);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetItemInHand(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.item_in_hand", "Get Item In Hand", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    ItemStack stack = ItemStack.EMPTY;
                    if (obj instanceof Player p) stack = p.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
                    else if (obj instanceof LivingEntity le) stack = le.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
                    n.setOutput("ItemStack", stack);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetMainHandItem(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.main_hand", "Get Main Hand Item", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.ITEM_STACK, "ItemStack");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    ItemStack stack = ItemStack.EMPTY;
                    if (obj instanceof LivingEntity le) stack = le.getMainHandItem();
                    n.setOutput("ItemStack", stack);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetPosition(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.position", "Get Position", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.VEC3, "Position");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    Object pos = net.minecraft.world.phys.Vec3.ZERO;
                    if (obj instanceof Entity ent) pos = ent.position();
                    n.setOutput("Position", pos);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetName(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.name", "Get Name", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.STRING, "Name");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    String name = "";
                    if (obj instanceof Entity ent) name = ent.getName().getString();
                    else if (obj != null) name = obj.toString();
                    n.setOutput("Name", name);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerIsAlive(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.is_alive", "Is Alive", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    boolean alive = obj instanceof LivingEntity le && le.isAlive();
                    n.setOutput("Result", alive);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerIsOnGround(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.on_ground", "Is On Ground", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    boolean onGround = obj instanceof Entity ent && ent.onGround();
                    n.setOutput("Result", onGround);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetLevel(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("entity.level", "Get Entity Level", "Entity",
                node -> {
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.output(BlueprintTypes.LEVEL, "Level");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Entity"));
                    Object level = null;
                    if (obj instanceof Entity ent) level = ent.level();
                    n.setOutput("Level", level);
                    e.continueChain(n, g, ctx);
                }));
    }
}
