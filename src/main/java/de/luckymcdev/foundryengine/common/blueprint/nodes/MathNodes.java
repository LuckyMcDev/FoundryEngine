package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class MathNodes {

    private MathNodes() {
    }

    public static final class IntAdd extends BuiltinNode {
        public IntAdd() {
            super("Int Add", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(INT, "A", 0);
            input(INT, "B", 0);
            output(INT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            int a = ctx.resolvePinAs(node.inputPin("A"), Integer.class, 0);
            int b = ctx.resolvePinAs(node.inputPin("B"), Integer.class, 0);
            node.setOutput("Result", a + b);
        }
    }

    public static final class IntSub extends BuiltinNode {
        public IntSub() {
            super("Int Sub", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(INT, "A", 0);
            input(INT, "B", 0);
            output(INT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            int a = ctx.resolvePinAs(node.inputPin("A"), Integer.class, 0);
            int b = ctx.resolvePinAs(node.inputPin("B"), Integer.class, 0);
            node.setOutput("Result", a - b);
        }
    }

    public static final class IntMul extends BuiltinNode {
        public IntMul() {
            super("Int Mul", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(INT, "A", 0);
            input(INT, "B", 0);
            output(INT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            int a = ctx.resolvePinAs(node.inputPin("A"), Integer.class, 0);
            int b = ctx.resolvePinAs(node.inputPin("B"), Integer.class, 0);
            node.setOutput("Result", a * b);
        }
    }

    public static final class IntEquals extends BuiltinNode {
        public IntEquals() {
            super("Int Equals", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(INT, "A", 0);
            input(INT, "B", 0);
            output(BOOL, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            int a = ctx.resolvePinAs(node.inputPin("A"), Integer.class, 0);
            int b = ctx.resolvePinAs(node.inputPin("B"), Integer.class, 0);
            node.setOutput("Result", a == b);
        }
    }

    public static final class IntMod extends BuiltinNode {
        public IntMod() {
            super("Int Mod", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(INT, "A", 0);
            input(INT, "B", 0);
            output(INT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            int a = ctx.resolvePinAs(node.inputPin("A"), Integer.class, 0);
            int b = ctx.resolvePinAs(node.inputPin("B"), Integer.class, 0);
            node.setOutput("Result", b != 0 ? a % b : 0);
        }
    }

    public static final class FloatAdd extends BuiltinNode {
        public FloatAdd() {
            super("Float Add", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(FLOAT, "A", 0f);
            input(FLOAT, "B", 0f);
            output(FLOAT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            float a = ctx.resolvePinAs(node.inputPin("A"), Float.class, 0f);
            float b = ctx.resolvePinAs(node.inputPin("B"), Float.class, 0f);
            node.setOutput("Result", a + b);
        }
    }

    public static final class FloatSub extends BuiltinNode {
        public FloatSub() {
            super("Float Sub", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(FLOAT, "A", 0f);
            input(FLOAT, "B", 0f);
            output(FLOAT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            float a = ctx.resolvePinAs(node.inputPin("A"), Float.class, 0f);
            float b = ctx.resolvePinAs(node.inputPin("B"), Float.class, 0f);
            node.setOutput("Result", a - b);
        }
    }

    public static final class FloatMul extends BuiltinNode {
        public FloatMul() {
            super("Float Mul", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(FLOAT, "A", 0f);
            input(FLOAT, "B", 0f);
            output(FLOAT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            float a = ctx.resolvePinAs(node.inputPin("A"), Float.class, 0f);
            float b = ctx.resolvePinAs(node.inputPin("B"), Float.class, 0f);
            node.setOutput("Result", a * b);
        }
    }

    public static final class FloatDiv extends BuiltinNode {
        public FloatDiv() {
            super("Float Div", BlueprintEngine.Categories.MATH);
        }

        @Override
        protected void initPins() {
            input(FLOAT, "A", 0f);
            input(FLOAT, "B", 1f);
            output(FLOAT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            float a = ctx.resolvePinAs(node.inputPin("A"), Float.class, 0f);
            float b = ctx.resolvePinAs(node.inputPin("B"), Float.class, 1f);
            if (b == 0f) b = 1f;
            node.setOutput("Result", a / b);
        }
    }
}
