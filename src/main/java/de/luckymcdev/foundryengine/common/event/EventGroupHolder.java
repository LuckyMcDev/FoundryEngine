package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;

import java.util.Map;
import java.util.function.Function;

public class EventGroupHolder<T> {
    private final EventGroup<T> group = new EventGroup<>();
    private final String blueprintNodeId;
    private final Function<T, Map<String, Object>> contextMapper;

    public EventGroupHolder(BlueprintEngine.BuiltinNodes blueprintNode) {
        this(blueprintNode.id, event -> Map.of());
    }

    public EventGroupHolder(BlueprintEngine.BuiltinNodes blueprintNode,
                            Function<T, Map<String, Object>> contextMapper) {
        this(blueprintNode.id, contextMapper);
    }

    public EventGroupHolder(String blueprintNodeId) {
        this(blueprintNodeId, event -> Map.of());
    }

    public EventGroupHolder(String blueprintNodeId,
                            Function<T, Map<String, Object>> contextMapper) {
        this.blueprintNodeId = blueprintNodeId;
        this.contextMapper = contextMapper;
    }

    public EventGroupHolder() {
        this((String) null);
    }

    public void register(EventCallback<T> callback) {
        group.add(callback);
    }

    public void post(T event) {
        group.post(event);
        if (blueprintNodeId != null) {
            Map<String, Object> ctx = contextMapper.apply(event);
            if (ctx.isEmpty())
                Common.getBlueprintManager().executeCommonEvent(blueprintNodeId);
            else
                Common.getBlueprintManager().executeCommonEvent(blueprintNodeId, ctx);
        }
    }

    public void clear() {
        group.clear();
    }
}