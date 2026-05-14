package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class VectorNodes {

    private static final String CAT = BlueprintEngine.Categories.VECTORS;

    private VectorNodes() {
    }

    // ========== Create / Decompose ==========

    public static final class CreateVec3 extends BuiltinNode {
        public CreateVec3() {
            super("vector.create_vec3", "Create Vec3", CAT);
        }

        @Override
        protected void initPins() {
            input(FLOAT, "X", 0f);
            input(FLOAT, "Y", 0f);
            input(FLOAT, "Z", 0f);
            output(VEC3, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            float x = ctx.resolvePinAs(node.inputPin("X"), Float.class, 0f);
            float y = ctx.resolvePinAs(node.inputPin("Y"), Float.class, 0f);
            float z = ctx.resolvePinAs(node.inputPin("Z"), Float.class, 0f);
            node.setOutput("Result", new Vec3(x, y, z));
        }
    }

    public static final class BreakVec3 extends BuiltinNode {
        public BreakVec3() {
            super("vector.break_vec3", "Break Vec3", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "Vector", new Vec3(0, 0, 0));
            output(FLOAT, "X");
            output(FLOAT, "Y");
            output(FLOAT, "Z");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 v = ctx.resolvePinAs(node.inputPin("Vector"), Vec3.class, new Vec3(0, 0, 0));
            node.setOutput("X", (float) v.x);
            node.setOutput("Y", (float) v.y);
            node.setOutput("Z", (float) v.z);
        }
    }

    // ========== Arithmetic ==========

    public static final class Vec3Add extends BuiltinNode {
        public Vec3Add() {
            super("vector.vec3_add", "Vec3 Add", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "A", new Vec3(0, 0, 0));
            input(VEC3, "B", new Vec3(0, 0, 0));
            output(VEC3, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 a = ctx.resolvePinAs(node.inputPin("A"), Vec3.class, new Vec3(0, 0, 0));
            Vec3 b = ctx.resolvePinAs(node.inputPin("B"), Vec3.class, new Vec3(0, 0, 0));
            node.setOutput("Result", a.add(b));
        }
    }

    public static final class Vec3Sub extends BuiltinNode {
        public Vec3Sub() {
            super("vector.vec3_sub", "Vec3 Sub", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "A", new Vec3(0, 0, 0));
            input(VEC3, "B", new Vec3(0, 0, 0));
            output(VEC3, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 a = ctx.resolvePinAs(node.inputPin("A"), Vec3.class, new Vec3(0, 0, 0));
            Vec3 b = ctx.resolvePinAs(node.inputPin("B"), Vec3.class, new Vec3(0, 0, 0));
            node.setOutput("Result", a.subtract(b));
        }
    }

    public static final class Vec3Mul extends BuiltinNode {
        public Vec3Mul() {
            super("vector.vec3_mul", "Vec3 Scale", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "Vector", new Vec3(0, 0, 0));
            input(FLOAT, "Scalar", 1f);
            output(VEC3, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 v = ctx.resolvePinAs(node.inputPin("Vector"), Vec3.class, new Vec3(0, 0, 0));
            float s = ctx.resolvePinAs(node.inputPin("Scalar"), Float.class, 1f);
            node.setOutput("Result", v.scale(s));
        }
    }

    public static final class Vec3Div extends BuiltinNode {
        public Vec3Div() {
            super("vector.vec3_div", "Vec3 Divide", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "Vector", new Vec3(0, 0, 0));
            input(FLOAT, "Scalar", 1f);
            output(VEC3, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 v = ctx.resolvePinAs(node.inputPin("Vector"), Vec3.class, new Vec3(0, 0, 0));
            float s = ctx.resolvePinAs(node.inputPin("Scalar"), Float.class, 1f);
            node.setOutput("Result", s == 0f ? v : new Vec3(v.x / s, v.y / s, v.z / s));
        }
    }

    // ========== Distance / Length ==========

    public static final class Vec3Distance extends BuiltinNode {
        public Vec3Distance() {
            super("vector.vec3_distance", "Vec3 Distance", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "A", new Vec3(0, 0, 0));
            input(VEC3, "B", new Vec3(0, 0, 0));
            output(FLOAT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 a = ctx.resolvePinAs(node.inputPin("A"), Vec3.class, new Vec3(0, 0, 0));
            Vec3 b = ctx.resolvePinAs(node.inputPin("B"), Vec3.class, new Vec3(0, 0, 0));
            node.setOutput("Result", (float) a.distanceTo(b));
        }
    }

    public static final class Vec3Length extends BuiltinNode {
        public Vec3Length() {
            super("vector.vec3_length", "Vec3 Length", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "Vector", new Vec3(0, 0, 0));
            output(FLOAT, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 v = ctx.resolvePinAs(node.inputPin("Vector"), Vec3.class, new Vec3(0, 0, 0));
            node.setOutput("Result", (float) v.length());
        }
    }

    public static final class Vec3Normalize extends BuiltinNode {
        public Vec3Normalize() {
            super("vector.vec3_normalize", "Vec3 Normalize", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "Vector", new Vec3(0, 0, 0));
            output(VEC3, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 v = ctx.resolvePinAs(node.inputPin("Vector"), Vec3.class, new Vec3(0, 0, 0));
            double len = v.length();
            node.setOutput("Result", len == 0 ? v : v.scale(1.0 / len));
        }
    }

    // ========== Lerp ==========

    public static final class Vec3Lerp extends BuiltinNode {
        public Vec3Lerp() {
            super("vector.vec3_lerp", "Vec3 Lerp", CAT);
        }

        @Override
        protected void initPins() {
            input(VEC3, "From", new Vec3(0, 0, 0));
            input(VEC3, "To", new Vec3(0, 0, 0));
            input(FLOAT, "T", 0.5f);
            output(VEC3, "Result");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Vec3 from = ctx.resolvePinAs(node.inputPin("From"), Vec3.class, new Vec3(0, 0, 0));
            Vec3 to = ctx.resolvePinAs(node.inputPin("To"), Vec3.class, new Vec3(0, 0, 0));
            float t = ctx.resolvePinAs(node.inputPin("T"), Float.class, 0.5f);
            node.setOutput("Result", new Vec3(
                    from.x + (to.x - from.x) * t,
                    from.y + (to.y - from.y) * t,
                    from.z + (to.z - from.z) * t));
        }
    }

    // ========== Entity Position ==========

    public static final class EntityPosition extends BuiltinNode {
        public EntityPosition() {
            super("vector.entity_position", "Entity Position", CAT);
        }

        @Override
        protected void initPins() {
            input(ENTITY, "Entity");
            output(VEC3, "Position");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Entity entity = ctx.resolvePinAs(node.inputPin("Entity"), Entity.class, null);
            if (entity != null) {
                node.setOutput("Position", entity.position());
            } else {
                node.setOutput("Position", new Vec3(0, 0, 0));
            }
        }
    }
}
