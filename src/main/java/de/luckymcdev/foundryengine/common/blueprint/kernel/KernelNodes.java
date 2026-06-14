package de.luckymcdev.foundryengine.common.blueprint.kernel;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class KernelNodes {

    private KernelNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerFlowControl(engine);
        registerMath(engine);
        registerString(engine);
        registerVariables(engine);
        registerComparison(engine);
        registerUtility(engine);
    }

    // ======================== Flow Control ========================

    private static void registerFlowControl(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("logic.branch", "Branch", "Flow",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.BOOL, "Condition");
                    node.execOutput("True");
                    node.execOutput("False");
                },
                (n, e, g, ctx) -> {
                    boolean cond = ctx.resolvePinAs(n.inputPin("Condition"), Boolean.class, false);
                    e.executePin(n, cond ? "True" : "False", g, ctx);
                }));

        engine.register(BuiltinNode.create("logic.sequence", "Sequence", "Flow",
                node -> {
                    node.execInput("Exec");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> e.continueChain(n, g, ctx)));

        engine.register(BuiltinNode.create("logic.for_loop", "For Loop", "Flow",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.INT, "StartIndex", 0);
                    node.input(BlueprintTypes.INT, "EndIndex", 10);
                    node.execOutput("LoopBody");
                    node.execOutput("Completed");
                    node.output(BlueprintTypes.INT, "Index");
                },
                (n, e, g, ctx) -> {
                    int start = ctx.resolvePinAs(n.inputPin("StartIndex"), Integer.class, 0);
                    int end = ctx.resolvePinAs(n.inputPin("EndIndex"), Integer.class, 10);
                    for (int i = start; i < end; i++) {
                        n.setOutput("Index", i);
                        e.executePin(n, "LoopBody", g, ctx);
                    }
                    e.executePin(n, "Completed", g, ctx);
                }));

        engine.register(BuiltinNode.create("logic.while_loop", "While Loop", "Flow",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.BOOL, "Condition");
                    node.execOutput("LoopBody");
                    node.execOutput("Completed");
                },
                (n, e, g, ctx) -> {
                    while (ctx.resolvePinAs(n.inputPin("Condition"), Boolean.class, false)) {
                        e.executePin(n, "LoopBody", g, ctx);
                    }
                    e.executePin(n, "Completed", g, ctx);
                }));

        engine.register(BuiltinNode.create("logic.flipflop", "FlipFlop", "Flow",
                node -> {
                    node.execInput("Exec");
                    node.execOutput("A");
                    node.execOutput("B");
                    node.output(BlueprintTypes.INT, "State");
                },
                (n, e, g, ctx) -> {
                    int state = ctx.getVar("_ff_" + n.id, Integer.class) != null ? ctx.getVar("_ff_" + n.id, Integer.class) : 0;
                    state = 1 - state;
                    ctx.setVar("_ff_" + n.id, state);
                    n.setOutput("State", state);
                    e.executePin(n, state == 0 ? "A" : "B", g, ctx);
                }));
    }

    // ======================== Math ========================

    private static void registerMath(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("math.add", "Add", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "A", 0f);
                    node.input(BlueprintTypes.FLOAT, "B", 0f);
                    node.output(BlueprintTypes.FLOAT, "Result");
                },
                (n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a + b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("math.subtract", "Subtract", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "A", 0f);
                    node.input(BlueprintTypes.FLOAT, "B", 0f);
                    node.output(BlueprintTypes.FLOAT, "Result");
                },
                (n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a - b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("math.multiply", "Multiply", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "A", 0f);
                    node.input(BlueprintTypes.FLOAT, "B", 0f);
                    node.output(BlueprintTypes.FLOAT, "Result");
                },
                (n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a * b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("math.divide", "Divide", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "A", 0f);
                    node.input(BlueprintTypes.FLOAT, "B", 1f);
                    node.output(BlueprintTypes.FLOAT, "Result");
                },
                (n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 1f);
                    n.setOutput("Result", b != 0 ? a / b : 0f);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("math.negate", "Negate", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "Value", 0f);
                    node.output(BlueprintTypes.FLOAT, "Result");
                },
                (n, e, g, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    n.setOutput("Result", -v);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("math.random", "Random Float", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "Min", 0f);
                    node.input(BlueprintTypes.FLOAT, "Max", 1f);
                    node.output(BlueprintTypes.FLOAT, "Result");
                },
                (n, e, g, ctx) -> {
                    float min = ctx.resolvePinAs(n.inputPin("Min"), Float.class, 0f);
                    float max = ctx.resolvePinAs(n.inputPin("Max"), Float.class, 1f);
                    n.setOutput("Result", (float) (min + ThreadLocalRandom.current().nextDouble() * (max - min)));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("math.floor", "Floor", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "Value", 0f);
                    node.output(BlueprintTypes.INT, "Result");
                },
                (n, e, g, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    n.setOutput("Result", (int) Math.floor(v));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("math.ceil", "Ceil", "Math",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "Value", 0f);
                    node.output(BlueprintTypes.INT, "Result");
                },
                (n, e, g, ctx) -> {
                    float v = ctx.resolvePinAs(n.inputPin("Value"), Float.class, 0f);
                    n.setOutput("Result", (int) Math.ceil(v));
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== String ========================

    private static void registerString(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("string.concat", "Concat", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "A", "");
                    node.input(BlueprintTypes.STRING, "B", "");
                    node.output(BlueprintTypes.STRING, "Result");
                },
                (n, e, g, ctx) -> {
                    String a = ctx.resolvePinAs(n.inputPin("A"), String.class, "");
                    String b = ctx.resolvePinAs(n.inputPin("B"), String.class, "");
                    n.setOutput("Result", a + b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("string.split", "Split", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "");
                    node.input(BlueprintTypes.STRING, "Delimiter", ",");
                    node.output(BlueprintTypes.LIST, "Result");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "");
                    String delim = ctx.resolvePinAs(n.inputPin("Delimiter"), String.class, ",");
                    n.setOutput("Result", List.of(input.split(delim)));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("string.contains", "Contains", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "");
                    node.input(BlueprintTypes.STRING, "Substring", "");
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "");
                    String sub = ctx.resolvePinAs(n.inputPin("Substring"), String.class, "");
                    n.setOutput("Result", input.contains(sub));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("string.replace", "Replace", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "");
                    node.input(BlueprintTypes.STRING, "Old", "");
                    node.input(BlueprintTypes.STRING, "New", "");
                    node.output(BlueprintTypes.STRING, "Result");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "");
                    String oldStr = ctx.resolvePinAs(n.inputPin("Old"), String.class, "");
                    String newStr = ctx.resolvePinAs(n.inputPin("New"), String.class, "");
                    n.setOutput("Result", input.replace(oldStr, newStr));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("string.length", "Length", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "");
                    node.output(BlueprintTypes.INT, "Result");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "");
                    n.setOutput("Result", input.length());
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("string.substring", "Substring", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "");
                    node.input(BlueprintTypes.INT, "Start", 0);
                    node.input(BlueprintTypes.INT, "End", 0);
                    node.output(BlueprintTypes.STRING, "Result");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "");
                    int start = ctx.resolvePinAs(n.inputPin("Start"), Integer.class, 0);
                    int end = ctx.resolvePinAs(n.inputPin("End"), Integer.class, input.length());
                    n.setOutput("Result", input.substring(start, Math.min(end, input.length())));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("string.upper", "To Upper Case", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "");
                    node.output(BlueprintTypes.STRING, "Result");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "");
                    n.setOutput("Result", input.toUpperCase());
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("string.lower", "To Lower Case", "String",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "");
                    node.output(BlueprintTypes.STRING, "Result");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "");
                    n.setOutput("Result", input.toLowerCase());
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Variables ========================

    private static void registerVariables(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("var.get", "Get Variable", "Variables",
                node -> {
                    node.input(BlueprintTypes.STRING, "Name", "");
                    node.output(BlueprintTypes.OBJECT, "Value");
                },
                (n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    n.setOutput("Value", ctx.getVar(name, Object.class));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("var.set", "Set Variable", "Variables",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Name", "");
                    node.input(BlueprintTypes.OBJECT, "Value");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    ctx.setVar(name, value);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("var.has", "Has Variable", "Variables",
                node -> {
                    node.input(BlueprintTypes.STRING, "Name", "");
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    n.setOutput("Result", ctx.hasVar(name));
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Comparison ========================

    private static void registerComparison(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("cmp.equals", "Equals", "Comparison",
                node -> {
                    node.input(BlueprintTypes.OBJECT, "A");
                    node.input(BlueprintTypes.OBJECT, "B");
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    Object a = ctx.resolvePin(n.inputPin("A"));
                    Object b = ctx.resolvePin(n.inputPin("B"));
                    n.setOutput("Result", a != null && a.equals(b));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmp.not_equals", "Not Equals", "Comparison",
                node -> {
                    node.input(BlueprintTypes.OBJECT, "A");
                    node.input(BlueprintTypes.OBJECT, "B");
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    Object a = ctx.resolvePin(n.inputPin("A"));
                    Object b = ctx.resolvePin(n.inputPin("B"));
                    n.setOutput("Result", !Objects.equals(a, b));
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmp.greater", "Greater Than", "Comparison",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "A", 0f);
                    node.input(BlueprintTypes.FLOAT, "B", 0f);
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a > b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmp.less", "Less Than", "Comparison",
                node -> {
                    node.input(BlueprintTypes.FLOAT, "A", 0f);
                    node.input(BlueprintTypes.FLOAT, "B", 0f);
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    float a = ctx.resolvePinAs(n.inputPin("A"), Float.class, 0f);
                    float b = ctx.resolvePinAs(n.inputPin("B"), Float.class, 0f);
                    n.setOutput("Result", a < b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmp.and", "And", "Comparison",
                node -> {
                    node.input(BlueprintTypes.BOOL, "A", false);
                    node.input(BlueprintTypes.BOOL, "B", false);
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    boolean a = ctx.resolvePinAs(n.inputPin("A"), Boolean.class, false);
                    boolean b = ctx.resolvePinAs(n.inputPin("B"), Boolean.class, false);
                    n.setOutput("Result", a && b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmp.or", "Or", "Comparison",
                node -> {
                    node.input(BlueprintTypes.BOOL, "A", false);
                    node.input(BlueprintTypes.BOOL, "B", false);
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    boolean a = ctx.resolvePinAs(n.inputPin("A"), Boolean.class, false);
                    boolean b = ctx.resolvePinAs(n.inputPin("B"), Boolean.class, false);
                    n.setOutput("Result", a || b);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmp.not", "Not", "Comparison",
                node -> {
                    node.input(BlueprintTypes.BOOL, "Value", false);
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    boolean v = ctx.resolvePinAs(n.inputPin("Value"), Boolean.class, false);
                    n.setOutput("Result", !v);
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Utility ========================

    private static void registerUtility(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("util.print", "Print", "Utility",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Message", "");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    String msg = ctx.resolvePinAs(n.inputPin("Message"), String.class, "");
                    com.mojang.logging.LogUtils.getLogger().info("[Blueprint] {}", msg);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("util.run_command", "Run Command", "Utility",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Command", "/say hello");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    String cmd = ctx.resolvePinAs(n.inputPin("Command"), String.class, "/say hello");
                    if (src != null && src.getServer() != null) {
                        src.getServer().getCommands().performPrefixedCommand(src, cmd);
                    }
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("util.delay", "Delay", "Utility",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.INT, "Ticks", 20);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    int ticks = ctx.resolvePinAs(n.inputPin("Ticks"), Integer.class, 20);
                    var src = ctx.commandSource();
                    if (src != null && src.getServer() != null) {
                        src.getServer().execute(() -> {
                            try {
                                Thread.sleep(ticks * 50L);
                            } catch (InterruptedException ignored) {
                            }
                            e.continueChain(n, g, ctx);
                        });
                    } else {
                        e.continueChain(n, g, ctx);
                    }
                }));

        engine.register(BuiltinNode.create("util.get_cmd_source", "Get CommandSource", "Utility",
                node -> {
                    node.execInput("Exec");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.COMMAND_SOURCE, "Source");
                },
                (n, e, g, ctx) -> {
                    n.setOutput("Source", ctx.commandSource());
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("util.make_list", "Make List", "Utility",
                node -> {
                    node.input(BlueprintTypes.OBJECT, "Item1");
                    node.input(BlueprintTypes.OBJECT, "Item2");
                    node.output(BlueprintTypes.LIST, "Result");
                },
                (n, e, g, ctx) -> {
                    Object a = ctx.resolvePin(n.inputPin("Item1"));
                    Object b = ctx.resolvePin(n.inputPin("Item2"));
                    n.setOutput("Result", java.util.Arrays.asList(a, b));
                    e.continueChain(n, g, ctx);
                }));
    }
}
