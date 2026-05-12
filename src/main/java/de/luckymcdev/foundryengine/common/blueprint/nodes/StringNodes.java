package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class StringNodes {

    private StringNodes() {
    }

    public static final class Concat extends BuiltinNode {
        public Concat() {
            super("string.concat", "Concat", BlueprintEngine.Categories.STRINGS);
        }

        @Override
        protected void initPins() {
            input(STRING, "A", "");
            input(STRING, "B", "");
            output(STRING, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String a = ctx.resolvePinAs(node.inputPin("A"), String.class, "");
            String b = ctx.resolvePinAs(node.inputPin("B"), String.class, "");
            node.setOutput("Result", a + b);
        }
    }

    public static final class Equals extends BuiltinNode {
        public Equals() {
            super("string.equals", "Equals", BlueprintEngine.Categories.STRINGS);
        }

        @Override
        protected void initPins() {
            input(STRING, "A", "");
            input(STRING, "B", "");
            output(BOOL, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String a = ctx.resolvePinAs(node.inputPin("A"), String.class, "");
            String b = ctx.resolvePinAs(node.inputPin("B"), String.class, "");
            node.setOutput("Result", a.equals(b));
        }
    }

    public static final class IsEmpty extends BuiltinNode {
        public IsEmpty() {
            super("string.is_empty", "Is Empty", BlueprintEngine.Categories.STRINGS);
        }

        @Override
        protected void initPins() {
            input(STRING, "Value", "");
            output(BOOL, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String v = ctx.resolvePinAs(node.inputPin("Value"), String.class, "");
            node.setOutput("Result", v.isEmpty());
        }
    }

    public static final class Length extends BuiltinNode {
        public Length() {
            super("string.length", "Length", BlueprintEngine.Categories.STRINGS);
        }

        @Override
        protected void initPins() {
            input(STRING, "Value", "");
            output(INT, "Length");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String v = ctx.resolvePinAs(node.inputPin("Value"), String.class, "");
            node.setOutput("Length", v.length());
        }
    }
}
