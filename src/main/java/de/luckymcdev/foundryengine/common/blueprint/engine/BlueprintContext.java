package de.luckymcdev.foundryengine.common.blueprint.engine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime context passed to every {@link BlueprintEngine.NodeBehavior}
 * during graph execution.
 */
public class BlueprintContext {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_DEPTH = 512;

    private final BlueprintGraph graph;
    private final Map<String, Object> variables = new HashMap<>();
    private int depth = 0;

    public BlueprintContext(BlueprintGraph graph) {
        this.graph = graph;
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
            BlueprintEngine eng = graph instanceof EngineAwareGraph eag ? eag.getEngine() : null;
            if (eng != null) {
                BlueprintEngine.NodeBehavior behavior = eng.getBehavior(source.identifier);
                if (behavior != null) {
                    behavior.execute(source, eng, graph, this);
                }
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
            default -> {
            }
        }
        if (type == String.class) return (V) v.toString();

        return fallback;
    }

    /**
     * Marker interface so {@link BlueprintContext} can reach back to the engine
     * without a hard dependency on the editor or any specific graph subclass.
     */
    public interface EngineAwareGraph {
        BlueprintEngine getEngine();
    }
}
