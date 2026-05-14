package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.CommandContext;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class ExecuteModifierNodes {

    private static final String CAT = BlueprintEngine.Categories.COMMANDS_EXECUTE;

    private static final String CTX_KEY = "_cmd_ctx";

    private ExecuteModifierNodes() {
    }

    private static CommandContext ctx(BlueprintContext bpc) {
        CommandContext c = bpc.getVar(CTX_KEY, CommandContext.class);
        if (c == null) {
            c = new CommandContext();
            bpc.setVar(CTX_KEY, c);
        }
        return c;
    }

    // ========== Core modifiers ==========

    public static final class AsEntity extends BuiltinNode {
        public AsEntity() {
            super("execute.as", "As Entity", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(ENTITY, "Entity");
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            Entity entity = bpc.resolvePinAs(node.inputPin("Entity"), Entity.class, null);
            if (entity != null) {
                CommandContext cmdCtx = ctx(bpc);
                cmdCtx.executor = entity;
                if (cmdCtx.level == null) cmdCtx.level = entity.level();
            }
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class AtEntity extends BuiltinNode {
        public AtEntity() {
            super("execute.at", "At Entity", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(ENTITY, "Entity");
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            Entity entity = bpc.resolvePinAs(node.inputPin("Entity"), Entity.class, null);
            if (entity != null) {
                CommandContext cmdCtx = ctx(bpc);
                cmdCtx.position = entity.position();
                cmdCtx.rotation = entity.getRotationVector();
                cmdCtx.level = entity.level();
            }
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class AsAt extends BuiltinNode {
        public AsAt() {
            super("execute.asat", "As/At", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(ENTITY, "Entity");
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            Entity entity = bpc.resolvePinAs(node.inputPin("Entity"), Entity.class, null);
            if (entity != null) {
                CommandContext cmdCtx = ctx(bpc);
                cmdCtx.executor = entity;
                cmdCtx.position = entity.position();
                cmdCtx.rotation = entity.getRotationVector();
                cmdCtx.level = entity.level();
            }
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class PositionedTo extends BuiltinNode {
        public PositionedTo() {
            super("execute.positioned_to", "Positioned To", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(COORD, "X", 0);
            input(COORD, "Y", 0);
            input(COORD, "Z", 0);
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            float x = bpc.resolvePinAs(node.inputPin("X"), Float.class, 0f);
            float y = bpc.resolvePinAs(node.inputPin("Y"), Float.class, 0f);
            float z = bpc.resolvePinAs(node.inputPin("Z"), Float.class, 0f);
            ctx(bpc).position = new Vec3(x, y, z);
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class PositionedAs extends BuiltinNode {
        public PositionedAs() {
            super("execute.positioned_as", "Positioned As Entity", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(ENTITY, "Entity");
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            Entity entity = bpc.resolvePinAs(node.inputPin("Entity"), Entity.class, null);
            if (entity != null) ctx(bpc).position = entity.position();
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class RotatedTo extends BuiltinNode {
        public RotatedTo() {
            super("execute.rotated_to", "Rotated To", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(FLOAT, "Yaw", 0f);
            input(FLOAT, "Pitch", 0f);
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            float yaw = bpc.resolvePinAs(node.inputPin("Yaw"), Float.class, 0f);
            float pitch = bpc.resolvePinAs(node.inputPin("Pitch"), Float.class, 0f);
            ctx(bpc).rotation = new net.minecraft.world.phys.Vec2(pitch, yaw);
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class RotatedAs extends BuiltinNode {
        public RotatedAs() {
            super("execute.rotated_as", "Rotated As Entity", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(ENTITY, "Entity");
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            Entity entity = bpc.resolvePinAs(node.inputPin("Entity"), Entity.class, null);
            if (entity != null) ctx(bpc).rotation = entity.getRotationVector();
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class Anchored extends BuiltinNode {
        public Anchored() {
            super("execute.anchored", "Anchored", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(ANCHOR, "Anchor", "feet");
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            String a = bpc.resolvePinAs(node.inputPin("Anchor"), String.class, "feet");
            ctx(bpc).anchor = "eyes".equals(a) ? CommandContext.Anchor.EYES : CommandContext.Anchor.FEET;
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class Align extends BuiltinNode {
        public Align() {
            super("execute.align", "Align", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(BOOL, "X", true);
            input(BOOL, "Y", true);
            input(BOOL, "Z", true);
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            CommandContext cmdCtx = ctx(bpc);
            cmdCtx.alignX = bpc.resolvePinAs(node.inputPin("X"), Boolean.class, true);
            cmdCtx.alignY = bpc.resolvePinAs(node.inputPin("Y"), Boolean.class, true);
            cmdCtx.alignZ = bpc.resolvePinAs(node.inputPin("Z"), Boolean.class, true);
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class FacingPos extends BuiltinNode {
        public FacingPos() {
            super("execute.facing_pos", "Facing Position", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(COORD, "X", 0);
            input(COORD, "Y", 0);
            input(COORD, "Z", 0);
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            float x = bpc.resolvePinAs(node.inputPin("X"), Float.class, 0f);
            float y = bpc.resolvePinAs(node.inputPin("Y"), Float.class, 0f);
            float z = bpc.resolvePinAs(node.inputPin("Z"), Float.class, 0f);
            ctx(bpc).facingPos = new Vec3(x, y, z);
            ctx(bpc).facingEntity = null;
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class FacingEntity extends BuiltinNode {
        public FacingEntity() {
            super("execute.facing_entity", "Facing Entity", CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(ENTITY, "Entity");
            input(ANCHOR, "Anchor", "feet");
            execOutput("Execute");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            CommandContext cmdCtx = ctx(bpc);
            cmdCtx.facingEntity = bpc.resolvePinAs(node.inputPin("Entity"), Entity.class, null);
            String a = bpc.resolvePinAs(node.inputPin("Anchor"), String.class, "feet");
            cmdCtx.facingAnchor = "eyes".equals(a) ? CommandContext.Anchor.EYES : CommandContext.Anchor.FEET;
            cmdCtx.facingPos = null;
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    // ========== Condition modifiers ==========
    // These record conditions in the CommandContext.
    // The actual condition evaluation is done by the Minecraft command engine
    // when performPrefixedCommand() is called with the built command string.

    private abstract static class ConditionNode extends BuiltinNode {
        ConditionNode(String id, String name) {
            super(id, name, CAT);
        }

        @Override
        protected void initPins() {
            execInput("In");
            initConditionPins();
            execOutput("Execute");
        }

        protected abstract void initConditionPins();

        protected abstract void recordCondition(BlueprintNode node, BlueprintContext bpc);

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext bpc) {
            recordCondition(node, bpc);
            engine.executePin(node, "Execute", graph, bpc);
        }
    }

    public static final class IfBlock extends ConditionNode {
        public IfBlock() {
            super("execute.if_block", "If Block");
        }

        @Override
        protected void initConditionPins() {
            input(COORD, "X", 0);
            input(COORD, "Y", 0);
            input(COORD, "Z", 0);
            input(BLOCK_STATE, "Block");
        }

        @Override
        protected void recordCondition(BlueprintNode node, BlueprintContext bpc) {
            int x = bpc.resolvePinAs(node.inputPin("X"), Integer.class, 0);
            int y = bpc.resolvePinAs(node.inputPin("Y"), Integer.class, 0);
            int z = bpc.resolvePinAs(node.inputPin("Z"), Integer.class, 0);
            String block = bpc.resolvePinAs(node.inputPin("Block"), String.class, "minecraft:air");
            ctx(bpc).conditions.add(new CommandContext.Condition("if_block",
                    new BlockPos(x, y, z), block));
        }
    }

    public static final class UnlessBlock extends ConditionNode {
        public UnlessBlock() {
            super("execute.unless_block", "Unless Block");
        }

        @Override
        protected void initConditionPins() {
            input(COORD, "X", 0);
            input(COORD, "Y", 0);
            input(COORD, "Z", 0);
            input(BLOCK_STATE, "Block");
        }

        @Override
        protected void recordCondition(BlueprintNode node, BlueprintContext bpc) {
            int x = bpc.resolvePinAs(node.inputPin("X"), Integer.class, 0);
            int y = bpc.resolvePinAs(node.inputPin("Y"), Integer.class, 0);
            int z = bpc.resolvePinAs(node.inputPin("Z"), Integer.class, 0);
            String block = bpc.resolvePinAs(node.inputPin("Block"), String.class, "minecraft:air");
            ctx(bpc).conditions.add(new CommandContext.Condition("unless_block",
                    new BlockPos(x, y, z), block));
        }
    }

    public static final class IfEntity extends ConditionNode {
        public IfEntity() {
            super("execute.if_entity", "If Entity");
        }

        @Override
        protected void initConditionPins() {
            input(SELECTOR, "Selector", "@e");
        }

        @Override
        protected void recordCondition(BlueprintNode node, BlueprintContext bpc) {
            String sel = bpc.resolvePinAs(node.inputPin("Selector"), String.class, "@e");
            ctx(bpc).conditions.add(new CommandContext.Condition("if_entity", sel));
        }
    }

    public static final class UnlessEntity extends ConditionNode {
        public UnlessEntity() {
            super("execute.unless_entity", "Unless Entity");
        }

        @Override
        protected void initConditionPins() {
            input(SELECTOR, "Selector", "@e");
        }

        @Override
        protected void recordCondition(BlueprintNode node, BlueprintContext bpc) {
            String sel = bpc.resolvePinAs(node.inputPin("Selector"), String.class, "@e");
            ctx(bpc).conditions.add(new CommandContext.Condition("unless_entity", sel));
        }
    }

    public static final class IfPredicate extends ConditionNode {
        public IfPredicate() {
            super("execute.if_predicate", "If Predicate");
        }

        @Override
        protected void initConditionPins() {
            input(STRING, "Predicate", "minecraft:example/predicate");
        }

        @Override
        protected void recordCondition(BlueprintNode node, BlueprintContext bpc) {
            String pred = bpc.resolvePinAs(node.inputPin("Predicate"), String.class, "");
            ctx(bpc).conditions.add(new CommandContext.Condition("if_predicate", pred));
        }
    }
}
