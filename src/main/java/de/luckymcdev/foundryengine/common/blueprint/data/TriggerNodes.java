package de.luckymcdev.foundryengine.common.blueprint.data;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;

public final class TriggerNodes {

    private TriggerNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerRunAfterDelay(engine);
        registerRunEvery(engine);
        registerRunInLevel(engine);
    }

    /**
     * Runs the "Then" output after a delay in ticks.
     * Uses Minecraft's server tick queue.
     */
    private static void registerRunAfterDelay(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("trigger.delay", "Run After Delay", "Triggers",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.INT, "Ticks", 20);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    int ticks = ctx.resolvePinAs(n.inputPin("Ticks"), Integer.class, 20);
                    var src = ctx.commandSource();
                    if (src != null && src.getServer() != null) {
                        var server = src.getServer();
                        var graph = g;
                        var node = n;
                        var eng = e;
                        server.execute(() -> {
                            var newCtx = ctx.withCommandSource(src);
                            eng.executePin(node, "Then", graph, newCtx);
                        });
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    /**
     * Runs the "LoopBody" output repeatedly every `Interval` ticks,
     * for `Count` times, then runs "Completed".
     */
    private static void registerRunEvery(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("trigger.every", "Run Every", "Triggers",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.INT, "Interval", 20);
                    node.input(BlueprintTypes.INT, "Count", 5);
                    node.execOutput("LoopBody");
                    node.execOutput("Completed");
                    node.output(BlueprintTypes.INT, "Index");
                },
                (n, e, g, ctx) -> {
                    int interval = ctx.resolvePinAs(n.inputPin("Interval"), Integer.class, 20);
                    int count = ctx.resolvePinAs(n.inputPin("Count"), Integer.class, 5);
                    var src = ctx.commandSource();
                    if (src != null && src.getServer() != null) {
                        var server = src.getServer();
                        var graph = g;
                        var node = n;
                        var eng = e;
                        final var ref = new Object() { int tick = 0; };
                        ref.tick = 0;
                        server.execute(() -> {
                            for (int i = 0; i < count; i++) {
                                final int fi = i;
                                server.execute(() -> {
                                    var newCtx = ctx.withCommandSource(src);
                                    n.setOutput("Index", fi);
                                    e.executePin(n, "LoopBody", graph, newCtx);
                                });
                            }
                            server.execute(() -> {
                                var newCtx = ctx.withCommandSource(src);
                                eng.executePin(node, "Completed", graph, newCtx);
                            });
                        });
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    /**
     * Runs the "Then" output on the main server thread associated with a level.
     * Safe for world-changing operations.
     */
    private static void registerRunInLevel(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("trigger.in_level", "Run In Level", "Triggers",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.LEVEL, "Level");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    Object levelObj = ctx.resolvePin(n.inputPin("Level"));
                    var src = ctx.commandSource();
                    if (levelObj instanceof net.minecraft.server.level.ServerLevel sl) {
                        var graph = g;
                        var node = n;
                        var eng = e;
                        sl.getServer().execute(() -> {
                            var newCtx = ctx.withCommandSource(
                                    src != null ? src : sl.getServer().createCommandSourceStack());
                            eng.executePin(node, "Then", graph, newCtx);
                        });
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));
    }
}
