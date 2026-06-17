package de.luckymcdev.foundryengine.common.graph.domain;

import de.luckymcdev.foundryengine.common.graph.model.NodeModel;
import org.jetbrains.annotations.Nullable;

public interface ScriptExecHandler {

    void execute(NodeModel node, ScriptRuntimeContext ctx);

    @Nullable Class<?> eventClass();

    default boolean isBlocking() { return true; }
}
