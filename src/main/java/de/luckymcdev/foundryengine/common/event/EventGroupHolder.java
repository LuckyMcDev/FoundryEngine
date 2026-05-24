package de.luckymcdev.foundryengine.common.event;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Function;

public class EventGroupHolder<T> {
    private static final Logger LOGGER = LogUtils.getLogger();

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
            try {
                Map<String, Object> ctx = contextMapper.apply(event);
                if (ctx.isEmpty())
                    Common.getBlueprintManager().executeCommonEvent(blueprintNodeId);
                else
                    Common.getBlueprintManager().executeCommonEvent(blueprintNodeId, ctx);
            } catch (Throwable e) {
                LOGGER.error("Error executing blueprint event '{}'", blueprintNodeId, e);
                var server = ServerLifecycleHooks.getCurrentServer();
                String loc = e.getStackTrace().length > 0 ? " (" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")" : "";
                if (server != null) {
                    server.getPlayerList().broadcastSystemMessage(
                            Component.literal("§c[Script Error] Blueprint event '" + blueprintNodeId + "': " + e + loc), false);
                }
            }
        }
    }

    public void clear() {
        group.clear();
    }
}