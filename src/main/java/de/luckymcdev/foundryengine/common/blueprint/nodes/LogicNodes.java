package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.ANY;
import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.BOOL;

public final class LogicNodes {

    private LogicNodes() {
    }

    public static final class Branch extends BuiltinNode {
        public Branch() {
            super("Branch", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(BOOL, "Condition", false);
            execOutput("True");
            execOutput("False");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            boolean cond = ctx.resolvePinAs(node.inputPin("Condition"), Boolean.class, false);
            engine.executePin(node, cond ? "True" : "False", graph, ctx);
        }
    }

    public static final class Sequence extends BuiltinNode {
        public Sequence() {
            super("Sequence", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            execOutput("Then 0");
            execOutput("Then 1");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            engine.executePin(node, "Then 0", graph, ctx);
            engine.executePin(node, "Then 1", graph, ctx);
        }
    }

    public static final class RerouteExec extends BuiltinNode {
        public RerouteExec() {
            super(BlueprintEngine.BuiltinNodes.REROUTE_EXEC.id, "Reroute (Exec)", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            engine.executePin(node, "Out", graph, ctx);
        }
    }

    public static final class RerouteAny extends BuiltinNode {
        public RerouteAny() {
            super(BlueprintEngine.BuiltinNodes.REROUTE_ANY.id, "Reroute (Any)", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            input(ANY, "In");
            output(ANY, "Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Out", ctx.resolvePin(node.inputPin("In")));
        }
    }

    public static final class Not extends BuiltinNode {
        public Not() {
            super("bool.not", "Not", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            input(BOOL, "Value", false);
            output(BOOL, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            boolean v = ctx.resolvePinAs(node.inputPin("Value"), Boolean.class, false);
            node.setOutput("Result", !v);
        }
    }

    public static final class And extends BuiltinNode {
        public And() {
            super("bool.and", "And", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            input(BOOL, "A");
            input(BOOL, "B");
            output(BOOL, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            boolean a = ctx.resolvePinAs(node.inputPin("A"), Boolean.class, false);
            boolean b = ctx.resolvePinAs(node.inputPin("B"), Boolean.class, false);
            node.setOutput("Result", a && b);
        }
    }

    public static final class Or extends BuiltinNode {
        public Or() {
            super("bool.or", "Or", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            input(BOOL, "A");
            input(BOOL, "B");
            output(BOOL, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            boolean a = ctx.resolvePinAs(node.inputPin("A"), Boolean.class, false);
            boolean b = ctx.resolvePinAs(node.inputPin("B"), Boolean.class, false);
            node.setOutput("Result", a || b);
        }
    }
}
