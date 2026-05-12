package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

public final class CommentNode extends BuiltinNode {
    public CommentNode() {
        super(BlueprintEngine.BuiltinNodes.COMMENT.id, "Comment", BlueprintEngine.Categories.COMMENTS);
    }

    @Override
    public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
    }
}
