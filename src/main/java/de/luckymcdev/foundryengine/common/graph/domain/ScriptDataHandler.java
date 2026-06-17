package de.luckymcdev.foundryengine.common.graph.domain;

import de.luckymcdev.foundryengine.common.graph.model.NodeModel;

public interface ScriptDataHandler {

    Object evaluate(NodeModel node, ScriptRuntimeContext ctx);
}
