package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class SelectorNodes {

    private static final String CAT = BlueprintEngine.Categories.COMMANDS_TARGET;

    private SelectorNodes() {
    }

    // ========== Preset selectors ==========

    public static final class NearestPlayer extends BuiltinNode {
        public NearestPlayer() {
            super("selector.nearest_player", "@p", CAT);
        }

        @Override
        protected void initPins() {
            output(SELECTOR, "Selector");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Selector", "@p");
        }
    }

    public static final class AllPlayers extends BuiltinNode {
        public AllPlayers() {
            super("selector.all_players", "@a", CAT);
        }

        @Override
        protected void initPins() {
            output(SELECTOR, "Selector");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Selector", "@a");
        }
    }

    public static final class RandomPlayer extends BuiltinNode {
        public RandomPlayer() {
            super("selector.random_player", "@r", CAT);
        }

        @Override
        protected void initPins() {
            output(SELECTOR, "Selector");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Selector", "@r");
        }
    }

    public static final class Self extends BuiltinNode {
        public Self() {
            super("selector.self", "@s", CAT);
        }

        @Override
        protected void initPins() {
            output(SELECTOR, "Selector");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Selector", "@s");
        }
    }

    public static final class AllEntities extends BuiltinNode {
        public AllEntities() {
            super("selector.all_entities", "@e", CAT);
        }

        @Override
        protected void initPins() {
            output(SELECTOR, "Selector");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            node.setOutput("Selector", "@e");
        }
    }

    // ========== Builder ==========

    public static final class EntitySelector extends BuiltinNode {
        public EntitySelector() {
            super("selector.build", "Entity Selector", CAT);
        }

        @Override
        protected void initPins() {
            input(SELECTOR, "Base", "@e");
            input(ENTITY_TYPE, "Type");
            input(INT, "Limit", -1);
            input(STRING, "Tag", "");
            input(STRING, "Team", "");
            input(STRING, "Name", "");
            input(FLOAT, "Distance", -1f);
            input(COORD, "X", 0);
            input(COORD, "Y", 0);
            input(COORD, "Z", 0);
            input(FLOAT, "DX", 0f);
            input(FLOAT, "DY", 0f);
            input(FLOAT, "DZ", 0f);
            input(STRING, "Sort", "");
            input(STRING, "Predicate", "");
            output(SELECTOR, "Selector");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String base = ctx.resolvePinAs(node.inputPin("Base"), String.class, "@e");
            StringBuilder sb = new StringBuilder(base);

            boolean hasArgs = false;
            StringBuilder args = new StringBuilder("[");

            String type = ctx.resolvePinAs(node.inputPin("Type"), String.class, null);
            if (type != null && !type.isEmpty()) {
                appendArg(args, "type", type);
                hasArgs = true;
            }

            int limit = ctx.resolvePinAs(node.inputPin("Limit"), Integer.class, -1);
            if (limit > 0) {
                appendArg(args, "limit", String.valueOf(limit));
                hasArgs = true;
            }

            String tag = ctx.resolvePinAs(node.inputPin("Tag"), String.class, "");
            if (!tag.isEmpty()) {
                appendArg(args, "tag", tag);
                hasArgs = true;
            }

            String team = ctx.resolvePinAs(node.inputPin("Team"), String.class, "");
            if (!team.isEmpty()) {
                appendArg(args, "team", team);
                hasArgs = true;
            }

            String name = ctx.resolvePinAs(node.inputPin("Name"), String.class, "");
            if (!name.isEmpty()) {
                appendArg(args, "name", name);
                hasArgs = true;
            }

            float dist = ctx.resolvePinAs(node.inputPin("Distance"), Float.class, -1f);
            if (dist >= 0) {
                appendArg(args, "distance", String.valueOf(dist));
                hasArgs = true;
            }

            int x = ctx.resolvePinAs(node.inputPin("X"), Integer.class, 0);
            int y = ctx.resolvePinAs(node.inputPin("Y"), Integer.class, 0);
            int z = ctx.resolvePinAs(node.inputPin("Z"), Integer.class, 0);
            boolean hasPos = x != 0 || y != 0 || z != 0;
            if (hasPos) {
                appendArg(args, "x", String.valueOf(x));
                appendArg(args, "y", String.valueOf(y));
                appendArg(args, "z", String.valueOf(z));
                hasArgs = true;
            }

            float dx = ctx.resolvePinAs(node.inputPin("DX"), Float.class, 0f);
            float dy = ctx.resolvePinAs(node.inputPin("DY"), Float.class, 0f);
            float dz = ctx.resolvePinAs(node.inputPin("DZ"), Float.class, 0f);
            if (dx != 0f) {
                appendArg(args, "dx", String.valueOf(dx));
                hasArgs = true;
            }
            if (dy != 0f) {
                appendArg(args, "dy", String.valueOf(dy));
                hasArgs = true;
            }
            if (dz != 0f) {
                appendArg(args, "dz", String.valueOf(dz));
                hasArgs = true;
            }

            String sort = ctx.resolvePinAs(node.inputPin("Sort"), String.class, "");
            if (!sort.isEmpty()) {
                appendArg(args, "sort", sort);
                hasArgs = true;
            }

            String predicate = ctx.resolvePinAs(node.inputPin("Predicate"), String.class, "");
            if (!predicate.isEmpty()) {
                appendArg(args, "predicate", predicate);
                hasArgs = true;
            }

            if (hasArgs) {
                args.append("]");
                sb.append(args);
            }

            node.setOutput("Selector", sb.toString());
        }

        private void appendArg(StringBuilder sb, String key, String value) {
            if (sb.length() > 1 && sb.charAt(sb.length() - 1) != '[') sb.append(",");
            sb.append(key).append("=").append(value);
        }
    }
}
