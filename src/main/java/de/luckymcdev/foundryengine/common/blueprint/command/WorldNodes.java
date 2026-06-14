package de.luckymcdev.foundryengine.common.blueprint.command;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;

public final class WorldNodes {

    private WorldNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("cmd.time", "Set Time", "Commands/World",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.INT, "Time", 0);
                    node.input(BlueprintTypes.TIME_TYPE, "Type", "daytime");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var time = ctx.resolvePinAs(n.inputPin("Time"), Integer.class, 0);
                    var type = ctx.resolvePinAs(n.inputPin("Type"), String.class, "daytime");
                    src.getServer().getCommands().performPrefixedCommand(src, "time set " + time);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.weather", "Set Weather", "Commands/World",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.WEATHER, "Weather", "clear");
                    node.input(BlueprintTypes.INT, "Duration (s)", -1);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var weather = ctx.resolvePinAs(n.inputPin("Weather"), String.class, "clear");
                    var duration = ctx.resolvePinAs(n.inputPin("Duration (s)"), Integer.class, -1);
                    String cmd = "weather " + weather;
                    if (duration > 0) cmd += " " + duration;
                    src.getServer().getCommands().performPrefixedCommand(src, cmd);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.difficulty", "Difficulty", "Commands/World",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.DIFFICULTY, "Difficulty", "easy");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var diff = ctx.resolvePinAs(n.inputPin("Difficulty"), String.class, "easy");
                    src.getServer().getCommands().performPrefixedCommand(src, "difficulty " + diff);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.fill", "Fill", "Commands/World",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.VEC3, "From");
                    node.input(BlueprintTypes.VEC3, "To");
                    node.input(BlueprintTypes.BLOCK_STATE, "Block");
                    node.input(BlueprintTypes.FILL_MODE, "Mode", "replace");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var from = ctx.resolvePinAs(n.inputPin("From"), net.minecraft.world.phys.Vec3.class, null);
                    var to = ctx.resolvePinAs(n.inputPin("To"), net.minecraft.world.phys.Vec3.class, null);
                    var block = ctx.resolvePinAs(n.inputPin("Block"), net.minecraft.world.level.block.state.BlockState.class, null);
                    if (from == null || to == null || block == null) { e.continueChain(n, g, ctx); return; }
                    var blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block.getBlock());
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "fill " + from.x + " " + from.y + " " + from.z + " "
                            + to.x + " " + to.y + " " + to.z + " " + blockId);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.setblock", "Set Block", "Commands/World",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.VEC3, "Position");
                    node.input(BlueprintTypes.BLOCK_STATE, "Block");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var pos = ctx.resolvePinAs(n.inputPin("Position"), net.minecraft.world.phys.Vec3.class, null);
                    var block = ctx.resolvePinAs(n.inputPin("Block"), net.minecraft.world.level.block.state.BlockState.class, null);
                    if (pos == null || block == null) { e.continueChain(n, g, ctx); return; }
                    var blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block.getBlock());
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "setblock " + pos.x + " " + pos.y + " " + pos.z + " " + blockId);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.clone", "Clone", "Commands/World",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.VEC3, "From");
                    node.input(BlueprintTypes.VEC3, "To");
                    node.input(BlueprintTypes.VEC3, "Destination");
                    node.input(BlueprintTypes.CLONE_MODE, "Mode", "normal");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var from = ctx.resolvePinAs(n.inputPin("From"), net.minecraft.world.phys.Vec3.class, null);
                    var to = ctx.resolvePinAs(n.inputPin("To"), net.minecraft.world.phys.Vec3.class, null);
                    var dest = ctx.resolvePinAs(n.inputPin("Destination"), net.minecraft.world.phys.Vec3.class, null);
                    if (from == null || to == null || dest == null) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "clone " + from.x + " " + from.y + " " + from.z + " "
                            + to.x + " " + to.y + " " + to.z + " "
                            + dest.x + " " + dest.y + " " + dest.z);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.particle", "Particle", "Commands/World",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.PARTICLE, "Particle");
                    node.input(BlueprintTypes.VEC3, "Position");
                    node.input(BlueprintTypes.VEC3, "Delta");
                    node.input(BlueprintTypes.FLOAT, "Speed", 0f);
                    node.input(BlueprintTypes.INT, "Count", 1);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var particle = ctx.resolvePinAs(n.inputPin("Particle"), net.minecraft.core.particles.ParticleOptions.class, null);
                    var pos = ctx.resolvePinAs(n.inputPin("Position"), net.minecraft.world.phys.Vec3.class, null);
                    var delta = ctx.resolvePinAs(n.inputPin("Delta"), net.minecraft.world.phys.Vec3.class, null);
                    var speed = ctx.resolvePinAs(n.inputPin("Speed"), Float.class, 0f);
                    var count = ctx.resolvePinAs(n.inputPin("Count"), Integer.class, 1);
                    if (particle == null || pos == null || delta == null) { e.continueChain(n, g, ctx); return; }
                    var particleId = net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(particle.getType());
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "particle " + particleId + " " + pos.x + " " + pos.y + " " + pos.z + " "
                            + delta.x + " " + delta.y + " " + delta.z + " " + speed + " " + count);
                    e.continueChain(n, g, ctx);
                }));
    }
}
