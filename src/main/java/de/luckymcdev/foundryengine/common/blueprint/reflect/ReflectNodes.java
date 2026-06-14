package de.luckymcdev.foundryengine.common.blueprint.reflect;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class ReflectNodes {

    private ReflectNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerCallMethod(engine);
        registerGetField(engine);
        registerSetField(engine);
    }

    private static void registerCallMethod(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("reflect.call_method", "Call Method", "Reflection",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Target");
                    node.input(BlueprintTypes.STRING, "MethodName", "");
                    node.input(BlueprintTypes.LIST, "Args");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Result");
                },
                (n, e, g, ctx) -> {
                    Object target = ctx.resolvePin(n.inputPin("Target"));
                    String methodName = ctx.resolvePinAs(n.inputPin("MethodName"), String.class, "");
                    @SuppressWarnings("unchecked")
                    List<Object> args = ctx.resolvePinAs(n.inputPin("Args"), List.class, List.of());

                    if (target == null || methodName.isEmpty()) {
                        n.setOutput("Result", null);
                        e.continueChain(n, g, ctx);
                        return;
                    }

                    try {
                        Method method = findMethod(target.getClass(), methodName, args);
                        if (method != null) {
                            Object result = method.invoke(target, args.toArray());
                            n.setOutput("Result", result);
                        } else {
                            com.mojang.logging.LogUtils.getLogger().warn(
                                    "[Blueprint] Method '{}' not found on {}", methodName, target.getClass().getSimpleName());
                        }
                    } catch (Exception ex) {
                        com.mojang.logging.LogUtils.getLogger().error(
                                "[Blueprint] Error calling method '{}' on {}: {}",
                                methodName, target.getClass().getSimpleName(), ex.getMessage());
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetField(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("reflect.get_field", "Get Field", "Reflection",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Target");
                    node.input(BlueprintTypes.STRING, "FieldName", "");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Value");
                },
                (n, e, g, ctx) -> {
                    Object target = ctx.resolvePin(n.inputPin("Target"));
                    String fieldName = ctx.resolvePinAs(n.inputPin("FieldName"), String.class, "");

                    if (target == null || fieldName.isEmpty()) {
                        n.setOutput("Value", null);
                        e.continueChain(n, g, ctx);
                        return;
                    }

                    try {
                        Field field = findField(target.getClass(), fieldName);
                        if (field != null) {
                            n.setOutput("Value", field.get(target));
                        }
                    } catch (Exception ex) {
                        com.mojang.logging.LogUtils.getLogger().error(
                                "[Blueprint] Error reading field '{}' on {}: {}",
                                fieldName, target.getClass().getSimpleName(), ex.getMessage());
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerSetField(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("reflect.set_field", "Set Field", "Reflection",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Target");
                    node.input(BlueprintTypes.STRING, "FieldName", "");
                    node.input(BlueprintTypes.OBJECT, "Value");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    Object target = ctx.resolvePin(n.inputPin("Target"));
                    String fieldName = ctx.resolvePinAs(n.inputPin("FieldName"), String.class, "");
                    Object value = ctx.resolvePin(n.inputPin("Value"));

                    if (target == null || fieldName.isEmpty()) {
                        e.continueChain(n, g, ctx);
                        return;
                    }

                    try {
                        Field field = findField(target.getClass(), fieldName);
                        if (field != null) {
                            field.set(target, value);
                        }
                    } catch (Exception ex) {
                        com.mojang.logging.LogUtils.getLogger().error(
                                "[Blueprint] Error setting field '{}' on {}: {}",
                                fieldName, target.getClass().getSimpleName(), ex.getMessage());
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static @Nullable Method findMethod(Class<?> clazz, String name, List<Object> args) {
        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals(name)) continue;
            if (m.getParameterCount() != args.size()) continue;
            boolean match = true;
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                if (arg == null) continue;
                Class<?> paramType = m.getParameterTypes()[i];
                if (!paramType.isInstance(arg)) {
                    match = false;
                    break;
                }
            }
            if (match) return m;
        }
        return null;
    }

    private static @Nullable Field findField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
