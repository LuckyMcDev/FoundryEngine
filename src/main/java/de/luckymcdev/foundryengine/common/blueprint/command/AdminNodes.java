package de.luckymcdev.foundryengine.common.blueprint.command;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import net.minecraft.world.entity.Entity;

public final class AdminNodes {

    private AdminNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("cmd.gamemode", "Game Mode", "Commands/Admin",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.GAMEMODE, "Mode", "survival");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var mode = ctx.resolvePinAs(n.inputPin("Mode"), String.class, "survival");
                    if (target == null) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src, "gamemode " + mode + " " + target.getUUID());
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.kill", "Kill", "Commands/Admin",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    if (target == null) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src, "kill " + target.getUUID());
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.scoreboard", "Scoreboard", "Commands/Admin",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Objective");
                    node.input(BlueprintTypes.STRING, "Criteria", "dummy");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var objective = ctx.resolvePinAs(n.inputPin("Objective"), String.class, "");
                    var criteria = ctx.resolvePinAs(n.inputPin("Criteria"), String.class, "dummy");
                    if (objective.isEmpty()) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "scoreboard objectives add " + objective + " " + criteria);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.team", "Team", "Commands/Admin",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Action", "add");
                    node.input(BlueprintTypes.STRING, "Team");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var action = ctx.resolvePinAs(n.inputPin("Action"), String.class, "add");
                    var teamName = ctx.resolvePinAs(n.inputPin("Team"), String.class, "");
                    if (teamName.isEmpty()) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src, "team " + action + " " + teamName);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.tag", "Tag", "Commands/Admin",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.STRING, "Action", "add");
                    node.input(BlueprintTypes.STRING, "Tag");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var action = ctx.resolvePinAs(n.inputPin("Action"), String.class, "add");
                    var tag = ctx.resolvePinAs(n.inputPin("Tag"), String.class, "");
                    if (target == null || tag.isEmpty()) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src, "tag " + target.getUUID() + " " + action + " " + tag);
                    e.continueChain(n, g, ctx);
                }));
    }
}
