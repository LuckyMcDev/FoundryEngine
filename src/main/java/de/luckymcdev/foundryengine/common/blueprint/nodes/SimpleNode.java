package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePin;

import java.util.List;
import java.util.Map;

public final class SimpleNode extends BuiltinNode {
    private final NodeExecutor executor;

    public SimpleNode(String identifier, String name, String category,
                      List<NodePin> declaredInputs, List<NodePin> declaredOutputs,
                      Map<String, Object> pinDefaults, NodeExecutor executor) {
        super(identifier, name, category, true);
        this.declaredInputs.addAll(declaredInputs);
        this.declaredOutputs.addAll(declaredOutputs);
        this.pinDefaults.putAll(pinDefaults);
        this.executor = executor;
    }

    @Override
    public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
        executor.execute(node, engine, graph, ctx);
    }
}
