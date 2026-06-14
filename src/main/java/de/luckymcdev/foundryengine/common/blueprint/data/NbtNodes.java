package de.luckymcdev.foundryengine.common.blueprint.data;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;

public final class NbtNodes {

    private NbtNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerParseNbt(engine);
        registerCompoundToString(engine);
        registerGetNbtPath(engine);
        registerSetNbtPath(engine);
    }

    private static void registerParseNbt(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("nbt.parse", "Parse NBT", "Data",
                node -> {
                    node.input(BlueprintTypes.STRING, "Input", "{}");
                    node.output(BlueprintTypes.OBJECT, "Compound");
                },
                (n, e, g, ctx) -> {
                    String input = ctx.resolvePinAs(n.inputPin("Input"), String.class, "{}");
                    try {
                        n.setOutput("Compound", TagParser.parseCompoundFully(input));
                    } catch (Exception ex) {
                        com.mojang.logging.LogUtils.getLogger().error("[Blueprint] Failed to parse NBT: {}", ex.getMessage());
                        n.setOutput("Compound", new CompoundTag());
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerCompoundToString(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("nbt.to_string", "NBT To String", "Data",
                node -> {
                    node.input(BlueprintTypes.OBJECT, "Compound");
                    node.output(BlueprintTypes.STRING, "Output");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Compound"));
                    String result = "";
                    if (obj instanceof CompoundTag tag) result = tag.toString();
                    n.setOutput("Output", result);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerGetNbtPath(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("nbt.get_path", "Get NBT Path", "Data",
                node -> {
                    node.input(BlueprintTypes.OBJECT, "Compound");
                    node.input(BlueprintTypes.STRING, "Path", "");
                    node.output(BlueprintTypes.OBJECT, "Value");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Compound"));
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "");
                    if (obj instanceof CompoundTag tag && !path.isEmpty()) {
                        n.setOutput("Value", resolveNbtPath(tag, path));
                    } else {
                        n.setOutput("Value", null);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerSetNbtPath(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("nbt.set_path", "Set NBT Path", "Data",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Compound");
                    node.input(BlueprintTypes.STRING, "Path", "");
                    node.input(BlueprintTypes.OBJECT, "Value");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Compound");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Compound"));
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "");
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    if (obj instanceof CompoundTag tag && !path.isEmpty()) {
                        String[] parts = path.split("\\.");
                        CompoundTag current = tag;
                        for (int i = 0; i < parts.length - 1; i++) {
                            String part = parts[i];
                            if (part.matches("\\d+")) {
                                int idx = Integer.parseInt(part);
                                String listKey = parts[i - 1];
                                ListTag list = current.getListOrEmpty(listKey);
                                if (idx < list.size()) {
                                    var entry = list.getCompoundOrEmpty(idx);
                                    if (!entry.isEmpty()) current = entry;
                                }
                            } else {
                                if (!current.contains(part) || !(current.get(part) instanceof CompoundTag)) {
                                    current.put(part, new CompoundTag());
                                }
                                current = current.getCompoundOrEmpty(part);
                            }
                        }
                        String lastKey = parts[parts.length - 1];
                        putTagValue(current, lastKey, value);
                        n.setOutput("Compound", tag);
                    } else {
                        n.setOutput("Compound", obj);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static Object resolveNbtPath(CompoundTag tag, String path) {
        String[] parts = path.split("\\.");
        CompoundTag current = tag;
        for (String part : parts) {
            if (part.matches("\\d+")) {
                int idx = Integer.parseInt(part);
                String listKey = parts[java.util.Arrays.asList(parts).indexOf(part) - 1];
                ListTag list = current.getListOrEmpty(listKey);
                if (idx >= 0 && idx < list.size()) {
                    return extractTagValue(list.get(idx));
                }
                return null;
            }
            if (current.contains(part)) {
                var element = current.get(part);
                if (element instanceof CompoundTag ct) {
                    current = ct;
                } else {
                    return extractTagValue(element);
                }
            } else {
                return null;
            }
        }
        return current;
    }

    private static Object extractTagValue(net.minecraft.nbt.Tag tag) {
        if (tag instanceof StringTag(String value)) {
            return value;
        } else if (tag instanceof net.minecraft.nbt.NumericTag num) {
            if (tag instanceof net.minecraft.nbt.IntTag) {
                return ((net.minecraft.nbt.IntTag) tag).value();
            } else if (tag instanceof net.minecraft.nbt.FloatTag) {
                return num.floatValue();
            } else if (tag instanceof net.minecraft.nbt.DoubleTag) {
                return num.doubleValue();
            } else if (tag instanceof net.minecraft.nbt.ByteTag) {
                return num.byteValue() != 0;
            } else if (tag instanceof net.minecraft.nbt.LongTag) {
                return num.longValue();
            } else if (tag instanceof net.minecraft.nbt.ShortTag) {
                return num.shortValue();
            }
            return num.box();
        } else if (tag instanceof ListTag list) {
            return list;
        }
        return tag;
    }

    private static void putTagValue(CompoundTag tag, String key, Object value) {
        if (value instanceof String s) tag.putString(key, s);
        else if (value instanceof Integer i) tag.putInt(key, i);
        else if (value instanceof Float f) tag.putFloat(key, f);
        else if (value instanceof Double d) tag.putDouble(key, d);
        else if (value instanceof Boolean b) tag.putBoolean(key, b);
        else if (value instanceof Long l) tag.putLong(key, l);
        else if (value instanceof Short s) tag.putShort(key, s);
        else if (value instanceof Byte b) tag.putByte(key, b);
        else if (value instanceof CompoundTag ct) tag.put(key, ct);
        else if (value instanceof ListTag lt) tag.put(key, lt);
        else tag.putString(key, String.valueOf(value));
    }
}
