package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class LogicNodes {

    private LogicNodes() {
    }

    // ========== Control Flow ==========

    public static final class If extends BuiltinNode {
        public If() {
            super("If", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(BOOL, "Condition", false);
            execOutput("Then");
            execOutput("Continue");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            boolean cond = ctx.resolvePinAs(node.inputPin("Condition"), Boolean.class, false);
            if (cond) {
                engine.executePin(node, "Then", graph, ctx);
            }
            engine.executePin(node, "Continue", graph, ctx);
        }
    }

    public static final class IfElse extends BuiltinNode {
        public IfElse() {
            super("If/Else", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(BOOL, "Condition", false);
            execOutput("Then");
            execOutput("Else");
            execOutput("Continue");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            boolean cond = ctx.resolvePinAs(node.inputPin("Condition"), Boolean.class, false);
            if (cond) {
                engine.executePin(node, "Then", graph, ctx);
            } else {
                engine.executePin(node, "Else", graph, ctx);
            }
            engine.executePin(node, "Continue", graph, ctx);
        }
    }

    // ========== Loops ==========

    public static final class Repeat extends BuiltinNode {
        public Repeat() {
            super("Repeat", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(INT, "Times", 1);
            execOutput("Body");
            execOutput("Continue");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            int times = ctx.resolvePinAs(node.inputPin("Times"), Integer.class, 0);
            for (int i = 0; i < times; i++) {
                ctx.setVar("LoopIndex", i);
                engine.executePin(node, "Body", graph, ctx);
            }
            engine.executePin(node, "Continue", graph, ctx);
        }
    }

    public static final class RepeatUntil extends BuiltinNode {
        public RepeatUntil() {
            super("Repeat Until", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(BOOL, "Condition", false);
            execOutput("Body");
            execOutput("Continue");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            boolean cond = ctx.resolvePinAs(node.inputPin("Condition"), Boolean.class, false);
            int iter = 0;
            while (!cond) {
                ctx.setVar("LoopIndex", iter++);
                engine.executePin(node, "Body", graph, ctx);
                cond = ctx.resolvePinAs(node.inputPin("Condition"), Boolean.class, false);
            }
            engine.executePin(node, "Continue", graph, ctx);
        }
    }

    public static final class ForRange extends BuiltinNode {
        public ForRange() {
            super("For Range", BlueprintEngine.Categories.LOGIC);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(STRING, "Variable", "i");
            input(INT, "Start", 0);
            input(INT, "End", 10);
            input(INT, "Step", 1);
            execOutput("Body");
            execOutput("Continue");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String var = ctx.resolvePinAs(node.inputPin("Variable"), String.class, "i");
            int start = ctx.resolvePinAs(node.inputPin("Start"), Integer.class, 0);
            int end = ctx.resolvePinAs(node.inputPin("End"), Integer.class, 10);
            int step = ctx.resolvePinAs(node.inputPin("Step"), Integer.class, 1);
            for (int i = start; i <= end; i += step) {
                ctx.setVar(var, i);
                ctx.setVar("LoopIndex", i);
                engine.executePin(node, "Body", graph, ctx);
            }
            engine.executePin(node, "Continue", graph, ctx);
        }
    }

    // ========== Existing nodes ==========

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
