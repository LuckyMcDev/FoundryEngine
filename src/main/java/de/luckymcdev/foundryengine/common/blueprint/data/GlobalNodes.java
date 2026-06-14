package de.luckymcdev.foundryengine.common.blueprint.data;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;

public final class GlobalNodes {

    private GlobalNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerGlobalGet(engine);
        registerGlobalSet(engine);
        registerGlobalHas(engine);
        registerPersistentGet(engine);
        registerPersistentSet(engine);
    }

    private static void registerGlobalGet(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("global.get", "Global Get", "Data",
                node -> {
                    node.input(BlueprintTypes.STRING, "Name", "");
                    node.output(BlueprintTypes.OBJECT, "Value");
                },
                (n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    n.setOutput("Value", e.getGlobalVar(name, Object.class));
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGlobalSet(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("global.set", "Global Set", "Data",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Name", "");
                    node.input(BlueprintTypes.OBJECT, "Value");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    e.setGlobalVar(name, value);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGlobalHas(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("global.has", "Global Has", "Data",
                node -> {
                    node.input(BlueprintTypes.STRING, "Name", "");
                    node.output(BlueprintTypes.BOOL, "Result");
                },
                (n, e, g, ctx) -> {
                    String name = ctx.resolvePinAs(n.inputPin("Name"), String.class, "");
                    n.setOutput("Result", e.hasGlobalVar(name));
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerPersistentGet(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("persistent.get", "Persistent Get", "Data",
                node -> {
                    node.input(BlueprintTypes.STRING, "Key", "");
                    node.output(BlueprintTypes.OBJECT, "Value");
                },
                (n, e, g, ctx) -> {
                    String key = ctx.resolvePinAs(n.inputPin("Key"), String.class, "");
                    n.setOutput("Value", e.getPersistentData(key));
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerPersistentSet(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("persistent.set", "Persistent Set", "Data",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Key", "");
                    node.input(BlueprintTypes.OBJECT, "Value");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    String key = ctx.resolvePinAs(n.inputPin("Key"), String.class, "");
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    e.setPersistentData(key, value);
                    e.continueChain(n, g, ctx);
                }));
    }
}
