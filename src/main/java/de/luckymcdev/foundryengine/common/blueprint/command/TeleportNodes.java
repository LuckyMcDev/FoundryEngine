package de.luckymcdev.foundryengine.common.blueprint.command;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class TeleportNodes {

    private TeleportNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("cmd.tp", "Teleport", "Commands/Teleport",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.VEC3, "Position");
                    node.input(BlueprintTypes.VEC3, "Rotation");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var pos = ctx.resolvePinAs(n.inputPin("Position"), Vec3.class, null);
                    var rot = ctx.resolvePinAs(n.inputPin("Rotation"), Vec2.class, null);
                    StringBuilder cmd = new StringBuilder("tp ");
                    if (target != null) cmd.append(target.getUUID());
                    if (pos != null) cmd.append(" ").append(pos.x).append(" ").append(pos.y).append(" ").append(pos.z);
                    if (rot != null) cmd.append(" ").append(rot.x).append(" ").append(rot.y);
                    src.getServer().getCommands().performPrefixedCommand(src, cmd.toString());
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.summon", "Summon", "Commands/Teleport",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY_TYPE, "Entity Type");
                    node.input(BlueprintTypes.VEC3, "Position");
                    node.input(BlueprintTypes.NBT, "NBT", "{}");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var type = ctx.resolvePinAs(n.inputPin("Entity Type"), net.minecraft.world.entity.EntityType.class, null);
                    var pos = ctx.resolvePinAs(n.inputPin("Position"), Vec3.class, null);
                    var nbt = ctx.resolvePinAs(n.inputPin("NBT"), String.class, "{}");
                    if (type == null) { e.continueChain(n, g, ctx); return; }
                    var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    if (pos != null) {
                        src.getServer().getCommands().performPrefixedCommand(src,
                                "summon " + id + " " + pos.x + " " + pos.y + " " + pos.z + " " + nbt);
                    } else {
                        src.getServer().getCommands().performPrefixedCommand(src, "summon " + id + " " + nbt);
                    }
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.spreadplayers", "Spread Players", "Commands/Teleport",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.FLOAT, "Spread Distance", 0f);
                    node.input(BlueprintTypes.FLOAT, "Max Range", 0f);
                    node.input(BlueprintTypes.BOOL, "Respect Teams", false);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var spread = ctx.resolvePinAs(n.inputPin("Spread Distance"), Float.class, 0f);
                    var maxRange = ctx.resolvePinAs(n.inputPin("Max Range"), Float.class, 0f);
                    var respectTeams = ctx.resolvePinAs(n.inputPin("Respect Teams"), Boolean.class, false);
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "spreadplayers @a @a " + spread + " " + maxRange + " " + respectTeams);
                    e.continueChain(n, g, ctx);
                }));
    }
}
