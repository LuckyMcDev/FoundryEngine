package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.ANY;
import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.STRING;

public final class VariableNodes {

    private VariableNodes() {
    }

    public static final class SetVariable extends BuiltinNode {
        public SetVariable() {
            super("Set Variable", BlueprintEngine.Categories.VARIABLES);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(STRING, "Name", "myVar");
            input(ANY, "Value");
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String name = ctx.resolvePinAs(node.inputPin("Name"), String.class, "unnamed");
            Object value = ctx.resolvePin(node.inputPin("Value"));
            ctx.setVar(name, value);
        }
    }

    public static final class GetVariable extends BuiltinNode {
        public GetVariable() {
            super("Get Variable", BlueprintEngine.Categories.VARIABLES);
        }

        @Override
        protected void initPins() {
            input(STRING, "Name", "myVar");
            output(ANY, "Value");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String name = ctx.resolvePinAs(node.inputPin("Name"), String.class, "");
            node.setOutput("Value", ctx.getVar(name, Object.class));
        }
    }
}
