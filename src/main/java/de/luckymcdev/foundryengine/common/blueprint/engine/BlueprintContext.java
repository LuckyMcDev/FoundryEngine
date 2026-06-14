package de.luckymcdev.foundryengine.common.blueprint.engine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinInfo;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BlueprintContext {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_DEPTH = 512;

    private final BlueprintGraph graph;
    private final BlueprintEngine engine;
    private final Map<String, Object> variables = new HashMap<>();
    private final CommandSourceStack commandSource;
    private int depth = 0;
    private boolean cancelled = false;
    private Object result = null;

    public BlueprintContext(BlueprintGraph graph, BlueprintEngine engine) {
        this.graph = graph;
        this.engine = engine;
        this.commandSource = null;
    }

    public BlueprintContext(BlueprintGraph graph, BlueprintEngine engine, CommandSourceStack commandSource) {
        this.graph = graph;
        this.engine = engine;
        this.commandSource = commandSource;
    }

    public @Nullable CommandSourceStack commandSource() {
        return commandSource;
    }

    public BlueprintContext withCommandSource(CommandSourceStack commandSource) {
        BlueprintContext c = new BlueprintContext(graph, engine, commandSource);
        c.variables.putAll(this.variables);
        c.depth = this.depth;
        return c;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Increments execution depth and returns {@code false} if the maximum
     * has been exceeded (cycle / runaway loop guard).
     * Every call to {@code tickDepth()} must be paired with {@code untickDepth()}.
     */
    public boolean tickDepth() {
        return ++depth <= MAX_DEPTH;
    }

    public void untickDepth() {
        depth--;
    }

    public @Nullable Object getResult() {
        return result;
    }

    public void setResult(@Nullable Object result) {
        this.result = result;
    }

    public void setVar(String name, @Nullable Object value) {
        variables.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <V> @Nullable V getVar(String name, Class<V> type) {
        Object v = variables.get(name);
        if (v == null) return null;
        if (type.isInstance(v)) return (V) v;
        LOGGER.warn("Variable '{}' expected {} but was {}",
                name, type.getSimpleName(), v.getClass().getSimpleName());
        return null;
    }

    public boolean hasVar(String name) {
        return variables.containsKey(name);
    }

    public Map<String, Object> allVars() {
        return Map.copyOf(variables);
    }

    @Nullable
    public Object resolvePin(@Nullable NodePinInfo pin) {
        if (pin == null) return null;

        if (pin.inputLink != null) {
            if (++depth > MAX_DEPTH) {
                LOGGER.error("Blueprint cycle detected – aborting pin resolution.");
                depth = 0;
                return null;
            }

            BlueprintNode source = pin.inputLink.node;
            BuiltinNode builtin = engine.getById(source.identifier);
            if (builtin != null) {
                builtin.execute(source, engine, graph, this);
            }

            depth--;
            return source.outputValues.get(pin.inputLink.pin.label());
        }

        return pin.defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <V> V resolvePinAs(@Nullable NodePinInfo pin, Class<V> type, V fallback) {
        Object v = resolvePin(pin);
        if (v == null) return fallback;
        if (type.isInstance(v)) return (V) v;

        switch (v) {
            case Number n when type == Float.class -> {
                return (V) (Float) n.floatValue();
            }
            case Number n when type == Integer.class -> {
                return (V) (Integer) n.intValue();
            }
            case Number n when type == Boolean.class -> {
                return (V) (Boolean) (n.intValue() != 0);
            }
            case BlockPos bp when type == Vec3.class -> {
                return (V) Vec3.atCenterOf(bp);
            }
            case Vec3 vec when type == BlockPos.class -> {
                return (V) BlockPos.containing(vec.x, vec.y, vec.z);
            }
            default -> {
            }
        }
        if (type == String.class) return (V) v.toString();

        return fallback;
    }
}
