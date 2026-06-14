package de.luckymcdev.foundryengine.common.blueprint.command;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;

public final class ExecuteModifierNodes {

    private ExecuteModifierNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("cmd.execute.as", "As Entity", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    var entity = ctx.resolvePinAs(n.inputPin("Entity"), net.minecraft.world.entity.Entity.class, null);
                    if (src != null && entity != null) {
                        e.continueChain(n, g, ctx.withCommandSource(src.withEntity(entity)));
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));

        engine.register(BuiltinNode.create("cmd.execute.at", "At Position", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src != null && src.getEntity() != null) {
                        var entity = src.getEntity();
                        e.continueChain(n, g, ctx.withCommandSource(
                                src.withPosition(entity.position()).withRotation(entity.getRotationVector())));
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));

        engine.register(BuiltinNode.create("cmd.execute.positioned", "Positioned", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.VEC3, "Position");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src != null) {
                        var pos = ctx.resolvePinAs(n.inputPin("Position"), net.minecraft.world.phys.Vec3.class, null);
                        if (pos != null) {
                            e.continueChain(n, g, ctx.withCommandSource(src.withPosition(pos)));
                        } else {
                            e.continueChain(n, g, ctx);
                        }
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));

        engine.register(BuiltinNode.create("cmd.execute.rotated", "Rotated", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.VEC3, "Rotation");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src != null) {
                        var rot = ctx.resolvePinAs(n.inputPin("Rotation"), net.minecraft.world.phys.Vec2.class, null);
                        if (rot != null) {
                            e.continueChain(n, g, ctx.withCommandSource(src.withRotation(rot)));
                        } else {
                            e.continueChain(n, g, ctx);
                        }
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));

        engine.register(BuiltinNode.create("cmd.execute.anchored", "Anchored", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ANCHOR, "Anchor", "feet");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.execute.align", "Align", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Axes", "xyz");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.execute.facing", "Facing", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Entity");
                    node.input(BlueprintTypes.ANCHOR, "Anchor", "feet");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src != null) {
                        var entity = ctx.resolvePinAs(n.inputPin("Entity"), net.minecraft.world.entity.Entity.class, null);
                        if (entity != null) {
                            e.continueChain(n, g, ctx.withCommandSource(
                                    src.withRotation(entity.getRotationVector())));
                        } else {
                            e.continueChain(n, g, ctx);
                        }
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));

        engine.register(BuiltinNode.create("cmd.execute.in", "In Dimension", "Execute",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.LEVEL, "Level");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src != null) {
                        var level = ctx.resolvePinAs(n.inputPin("Level"), net.minecraft.server.level.ServerLevel.class, null);
                        if (level != null) {
                            e.continueChain(n, g, ctx.withCommandSource(src.withLevel(level)));
                        } else {
                            e.continueChain(n, g, ctx);
                        }
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));
    }
}
