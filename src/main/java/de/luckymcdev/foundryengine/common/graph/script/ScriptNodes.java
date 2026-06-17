package de.luckymcdev.foundryengine.common.graph.script;

import com.google.gson.JsonObject;
import de.luckymcdev.foundryengine.common.graph.ScriptDomain;
import de.luckymcdev.foundryengine.common.graph.domain.ScriptDataHandler;
import de.luckymcdev.foundryengine.common.graph.domain.ScriptExecHandler;
import de.luckymcdev.foundryengine.common.graph.domain.ScriptRuntimeContext;
import de.luckymcdev.foundryengine.common.graph.model.NodeModel;
import de.luckymcdev.foundryengine.common.graph.registry.NodeDefinition;
import de.luckymcdev.foundryengine.common.graph.registry.NodeRegistry;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class ScriptNodes {

    private ScriptNodes() {}

    public static void register(ScriptDomain domain) {
        registerEventSources(domain);
        registerExecNodes(domain);
        registerDataNodes(domain);
    }

    // ── Event Source Nodes ──────────────────────────────────────────────

    private static void registerEventSources(ScriptDomain domain) {
        // Player Join Event
        var playerJoinDef = new NodeDefinition(
                id("player_join"),
                "Player Join",
                "Events/Player",
                List.of(),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("player", PinType.ANY, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(playerJoinDef);
        domain.registerExecHandler(playerJoinDef.id(), execHandler((node, ctx) -> {
            ctx.walkAllExecOutputs(node);
        }, PlayerEvent.PlayerLoggedInEvent.class));

        // Player Chat Event
        var playerChatDef = new NodeDefinition(
                id("player_chat"),
                "Player Chat",
                "Events/Player",
                List.of(),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("player", PinType.ANY, true),
                        new NodeDefinition.PinDef("message", PinType.STRING, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(playerChatDef);
        domain.registerExecHandler(playerChatDef.id(), execHandler((node, ctx) -> {
            ctx.walkAllExecOutputs(node);
        }, ServerChatEvent.class));

        // Entity Death Event
        var entityDeathDef = new NodeDefinition(
                id("entity_death"),
                "Entity Death",
                "Events/Entity",
                List.of(),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("entity", PinType.ANY, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(entityDeathDef);
        domain.registerExecHandler(entityDeathDef.id(), execHandler((node, ctx) -> {
            ctx.walkAllExecOutputs(node);
        }, LivingDeathEvent.class));

        // Block Broken Event
        var blockBrokenDef = new NodeDefinition(
                id("block_broken"),
                "Block Broken",
                "Events/Block",
                List.of(),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("player", PinType.ANY, true),
                        new NodeDefinition.PinDef("pos", PinType.ANY, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(blockBrokenDef);
        domain.registerExecHandler(blockBrokenDef.id(), execHandler((node, ctx) -> {
            ctx.walkAllExecOutputs(node);
        }, BreakBlockEvent.class));

        // Server Tick Event
        var serverTickDef = new NodeDefinition(
                id("server_tick"),
                "Server Tick",
                "Events/Server",
                List.of(),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("server", PinType.ANY, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(serverTickDef);
        domain.registerExecHandler(serverTickDef.id(), execHandler((node, ctx) -> {
            ctx.walkAllExecOutputs(node);
        }, ServerTickEvent.Post.class));
    }

    // ── Exec Nodes ──────────────────────────────────────────────────────

    private static void registerExecNodes(ScriptDomain domain) {
        // Send Message
        var sendMsgDef = new NodeDefinition(
                id("send_message"),
                "Send Message",
                "Actions",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("message", PinType.STRING, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(sendMsgDef);
        domain.registerExecHandler(sendMsgDef.id(), execHandler((node, ctx) -> {
            var msg = ctx.resolve(firstInputPin(node));
            if (msg != null && ctx.getEvent() instanceof PlayerEvent pe && pe.getEntity() instanceof Player player) {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(msg.toString())
                );
            }
            ctx.walkAllExecOutputs(node);
        }, null));

        // Run Command
        var runCmdDef = new NodeDefinition(
                id("run_command"),
                "Run Command",
                "Actions",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("command", PinType.STRING, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(runCmdDef);
        domain.registerExecHandler(runCmdDef.id(), execHandler((node, ctx) -> {
            var cmd = ctx.resolve(firstInputPin(node));
            if (cmd != null) {
                var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(), cmd.toString()
                    );
                }
            }
            ctx.walkAllExecOutputs(node);
        }, null));

        // Log Message
        var logMsgDef = new NodeDefinition(
                id("log_message"),
                "Log Message",
                "Actions",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("message", PinType.STRING, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(logMsgDef);
        domain.registerExecHandler(logMsgDef.id(), execHandler((node, ctx) -> {
            var msg = ctx.resolve(firstInputPin(node));
            if (msg != null) {
                com.mojang.logging.LogUtils.getLogger().info(msg.toString());
            }
            ctx.walkAllExecOutputs(node);
        }, null));

        // Set Variable
        var setVarDef = new NodeDefinition(
                id("set_variable"),
                "Set Variable",
                "Actions",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("name", PinType.STRING, true),
                        new NodeDefinition.PinDef("value", PinType.ANY, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(setVarDef);
        domain.registerExecHandler(setVarDef.id(), execHandler((node, ctx) -> {
            var name = ctx.resolve(pinByLabel(node, "name"));
            var value = ctx.resolve(pinByLabel(node, "value"));
            if (name != null) {
                ctx.setVariable(name.toString(), value);
            }
            ctx.walkAllExecOutputs(node);
        }, null));

        // Branch (If/Else)
        var branchDef = new NodeDefinition(
                id("branch"),
                "Branch",
                "Control",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("condition", PinType.BOOL, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("true", PinType.EXEC, true),
                        new NodeDefinition.PinDef("false", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(branchDef);
        domain.registerExecHandler(branchDef.id(), new ScriptExecHandler() {
            @Override
            public void execute(NodeModel node, ScriptRuntimeContext ctx) {
                var cond = ctx.resolve(pinByLabel(node, "condition"));
                var label = (cond instanceof Boolean b && b) ? "true" : "false";
                var pinId = outputPinByLabel(node, label);
                if (pinId != null) {
                    ctx.walkExecFrom(pinId);
                }
            }
            @Override
            public Class<?> eventClass() { return null; }
        });

        // Give Item
        var giveItemDef = new NodeDefinition(
                id("give_item"),
                "Give Item",
                "Actions",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("item", PinType.STRING, true),
                        new NodeDefinition.PinDef("count", PinType.INT, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(giveItemDef);
        domain.registerExecHandler(giveItemDef.id(), execHandler((node, ctx) -> {
            var itemId = ctx.resolve(pinByLabel(node, "item"));
            var countVal = ctx.resolve(pinByLabel(node, "count"));
            if (itemId != null && ctx.getEvent() instanceof PlayerEvent pe && pe.getEntity() instanceof Player player) {
                var count = countVal instanceof Number n ? n.intValue() : 1;
                var id = net.minecraft.resources.Identifier.parse(itemId.toString());
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id).orElse(null);
                if (item != null) {
                    var stack = new net.minecraft.world.item.ItemStack(item, count);
                    player.getInventory().add(stack);
                }
            }
            ctx.walkAllExecOutputs(node);
        }, null));

        // Teleport Player
        var tpDef = new NodeDefinition(
                id("teleport"),
                "Teleport",
                "Actions",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("x", PinType.FLOAT, true),
                        new NodeDefinition.PinDef("y", PinType.FLOAT, true),
                        new NodeDefinition.PinDef("z", PinType.FLOAT, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(tpDef);
        domain.registerExecHandler(tpDef.id(), execHandler((node, ctx) -> {
            if (ctx.getEvent() instanceof PlayerEvent pe && pe.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
                var x = (double) toFloat(ctx.resolve(pinByLabel(node, "x")), (float) sp.getX());
                var y = (double) toFloat(ctx.resolve(pinByLabel(node, "y")), (float) sp.getY());
                var z = (double) toFloat(ctx.resolve(pinByLabel(node, "z")), (float) sp.getZ());
                sp.teleportTo((net.minecraft.server.level.ServerLevel) sp.level(), x, y, z, java.util.Set.of(), sp.getYRot(), sp.getXRot(), true);
            }
            ctx.walkAllExecOutputs(node);
        }, null));

        // Set Health
        var setHealthDef = new NodeDefinition(
                id("set_health"),
                "Set Health",
                "Actions",
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true),
                        new NodeDefinition.PinDef("health", PinType.FLOAT, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("exec", PinType.EXEC, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(setHealthDef);
        domain.registerExecHandler(setHealthDef.id(), execHandler((node, ctx) -> {
            var healthVal = ctx.resolve(pinByLabel(node, "health"));
            if (ctx.getEvent() instanceof net.neoforged.neoforge.event.entity.living.LivingDeathEvent) {
                // health only settable on living entities
            }
            if (ctx.getEvent() instanceof PlayerEvent pe && pe.getEntity() instanceof net.minecraft.world.entity.LivingEntity le) {
                le.setHealth(toFloat(healthVal, le.getMaxHealth()));
            }
            ctx.walkAllExecOutputs(node);
        }, null));
    }

    // ── Data Nodes ──────────────────────────────────────────────────────

    private static void registerDataNodes(ScriptDomain domain) {
        // Player Name
        var playerNameDef = new NodeDefinition(
                id("player_name"),
                "Player Name",
                "Data/Player",
                List.of(),
                List.of(
                        new NodeDefinition.PinDef("name", PinType.STRING, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(playerNameDef);
        domain.registerDataHandler(playerNameDef.id(), (node, ctx) -> {
            var event = ctx.getEvent();
            if (event instanceof PlayerEvent pe) {
                return pe.getEntity().getName().getString();
            }
            return "unknown";
        });

        // Random Number
        var randomDef = new NodeDefinition(
                id("random_number"),
                "Random Number",
                "Data/Math",
                List.of(
                        new NodeDefinition.PinDef("min", PinType.FLOAT, true),
                        new NodeDefinition.PinDef("max", PinType.FLOAT, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("result", PinType.FLOAT, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(randomDef);
        domain.registerDataHandler(randomDef.id(), (node, ctx) -> {
            float min = 0F, max = 1F;
            var minVal = ctx.resolve(pinByLabel(node, "min"));
            var maxVal = ctx.resolve(pinByLabel(node, "max"));
            if (minVal instanceof Number n) min = n.floatValue();
            if (maxVal instanceof Number n) max = n.floatValue();
            return (float) (min + Math.random() * (max - min));
        });

        // Math Add
        var mathAddDef = new NodeDefinition(
                id("math_add"),
                "Add",
                "Data/Math",
                List.of(
                        new NodeDefinition.PinDef("a", PinType.FLOAT, true),
                        new NodeDefinition.PinDef("b", PinType.FLOAT, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("result", PinType.FLOAT, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(mathAddDef);
        domain.registerDataHandler(mathAddDef.id(), (node, ctx) -> {
            float a = 0F, b = 0F;
            var aVal = ctx.resolve(pinByLabel(node, "a"));
            var bVal = ctx.resolve(pinByLabel(node, "b"));
            if (aVal instanceof Number n) a = n.floatValue();
            if (bVal instanceof Number n) b = n.floatValue();
            return a + b;
        });

        // String Concat
        var concatDef = new NodeDefinition(
                id("string_concat"),
                "Concat",
                "Data/String",
                List.of(
                        new NodeDefinition.PinDef("a", PinType.STRING, true),
                        new NodeDefinition.PinDef("b", PinType.STRING, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("result", PinType.STRING, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(concatDef);
        domain.registerDataHandler(concatDef.id(), (node, ctx) -> {
            var a = ctx.resolve(pinByLabel(node, "a"));
            var b = ctx.resolve(pinByLabel(node, "b"));
            return String.valueOf(a) + String.valueOf(b);
        });

        // Compare Equals
        var cmpEqDef = new NodeDefinition(
                id("compare_eq"),
                "Equals",
                "Data/Math",
                List.of(
                        new NodeDefinition.PinDef("a", PinType.ANY, true),
                        new NodeDefinition.PinDef("b", PinType.ANY, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("result", PinType.BOOL, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(cmpEqDef);
        domain.registerDataHandler(cmpEqDef.id(), (node, ctx) -> {
            var a = ctx.resolve(pinByLabel(node, "a"));
            var b = ctx.resolve(pinByLabel(node, "b"));
            if (a == null) return b == null;
            if (a instanceof Number na && b instanceof Number nb) return na.floatValue() == nb.floatValue();
            return a.equals(b);
        });

        // Greater Than
        var gtDef = new NodeDefinition(
                id("greater_than"),
                "Greater Than",
                "Data/Math",
                List.of(
                        new NodeDefinition.PinDef("a", PinType.FLOAT, true),
                        new NodeDefinition.PinDef("b", PinType.FLOAT, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("result", PinType.BOOL, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(gtDef);
        domain.registerDataHandler(gtDef.id(), (node, ctx) -> {
            var a = toFloat(ctx.resolve(pinByLabel(node, "a")), 0F);
            var b = toFloat(ctx.resolve(pinByLabel(node, "b")), 0F);
            return a > b;
        });

        // Less Than
        var ltDef = new NodeDefinition(
                id("less_than"),
                "Less Than",
                "Data/Math",
                List.of(
                        new NodeDefinition.PinDef("a", PinType.FLOAT, true),
                        new NodeDefinition.PinDef("b", PinType.FLOAT, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("result", PinType.BOOL, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(ltDef);
        domain.registerDataHandler(ltDef.id(), (node, ctx) -> {
            var a = toFloat(ctx.resolve(pinByLabel(node, "a")), 0F);
            var b = toFloat(ctx.resolve(pinByLabel(node, "b")), 0F);
            return a < b;
        });

        // String Equals
        var strEqDef = new NodeDefinition(
                id("string_equals"),
                "String Equals",
                "Data/String",
                List.of(
                        new NodeDefinition.PinDef("a", PinType.STRING, true),
                        new NodeDefinition.PinDef("b", PinType.STRING, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("result", PinType.BOOL, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(strEqDef);
        domain.registerDataHandler(strEqDef.id(), (node, ctx) -> {
            var a = ctx.resolve(pinByLabel(node, "a"));
            var b = ctx.resolve(pinByLabel(node, "b"));
            return String.valueOf(a).equals(String.valueOf(b));
        });

        // Get Event Player
        var getEventPlayerDef = new NodeDefinition(
                id("get_event_player"),
                "Get Event Player",
                "Data/Player",
                List.of(),
                List.of(
                        new NodeDefinition.PinDef("player", PinType.ANY, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(getEventPlayerDef);
        domain.registerDataHandler(getEventPlayerDef.id(), (node, ctx) -> {
            var event = ctx.getEvent();
            if (event instanceof PlayerEvent pe) return pe.getEntity();
            return null;
        });

        // Get Variable
        var getVarDef = new NodeDefinition(
                id("get_variable"),
                "Get Variable",
                "Data/Variable",
                List.of(
                        new NodeDefinition.PinDef("name", PinType.STRING, true)
                ),
                List.of(
                        new NodeDefinition.PinDef("value", PinType.ANY, true)
                ),
                data()
        );
        NodeRegistry.INSTANCE.register(getVarDef);
        domain.registerDataHandler(getVarDef.id(), (node, ctx) -> {
            var name = ctx.resolve(pinByLabel(node, "name"));
            return name != null ? ctx.getVariable(name.toString()) : null;
        });
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("foundryengine", path);
    }

    private static JsonObject data() {
        return new JsonObject();
    }

    private static UUID firstInputPin(NodeModel node) {
        return node.inputPins().isEmpty() ? null : node.inputPins().get(0).id();
    }

    private static float toFloat(Object val, float fallback) {
        return val instanceof Number n ? n.floatValue() : fallback;
    }

    private static int toInt(Object val, int fallback) {
        return val instanceof Number n ? n.intValue() : fallback;
    }

    private static UUID pinByLabel(NodeModel node, String label) {
        return node.inputPins().stream()
                .filter(p -> p.label().equals(label))
                .map(p -> p.id())
                .findFirst()
                .orElse(null);
    }

    private static UUID outputPinByLabel(NodeModel node, String label) {
        return node.outputPins().stream()
                .filter(p -> p.label().equals(label))
                .map(p -> p.id())
                .findFirst()
                .orElse(null);
    }

    private static ScriptExecHandler execHandler(BiConsumer<NodeModel, ScriptRuntimeContext> action, Class<?> eventClass) {
        return new ScriptExecHandler() {
            @Override
            public void execute(NodeModel node, ScriptRuntimeContext ctx) {
                action.accept(node, ctx);
            }
            @Override
            public Class<?> eventClass() { return eventClass; }
        };
    }
}
