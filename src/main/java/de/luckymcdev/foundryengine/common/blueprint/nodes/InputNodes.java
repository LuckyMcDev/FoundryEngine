package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class InputNodes {

    private InputNodes() {
    }

    public static final class StringInput extends BuiltinNode {
        public StringInput() {
            super("String", BlueprintEngine.Categories.INPUTS);
        }

        @Override
        protected void initPins() {
            input(STRING, "Value", "");
            output(STRING, "Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Out", ctx.resolvePin(node.inputPin("Value")));
        }
    }

    public static final class IntegerInput extends BuiltinNode {
        public IntegerInput() {
            super("Integer", BlueprintEngine.Categories.INPUTS);
        }

        @Override
        protected void initPins() {
            input(INT, "Value", 0);
            output(INT, "Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Out", ctx.resolvePin(node.inputPin("Value")));
        }
    }

    public static final class BooleanInput extends BuiltinNode {
        public BooleanInput() {
            super("Boolean", BlueprintEngine.Categories.INPUTS);
        }

        @Override
        protected void initPins() {
            input(BOOL, "Value", false);
            output(BOOL, "Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Out", ctx.resolvePin(node.inputPin("Value")));
        }
    }
}
