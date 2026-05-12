package de.luckymcdev.foundryengine.common.blueprint.nodes;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class RegistryNodes {
    private static final Logger LOGGER = LogUtils.getLogger();

    private RegistryNodes() {
    }

    public static final class RegisterItem extends BuiltinNode {
        public RegisterItem() {
            super("registry.item.register_simple", "Register Item (Simple)", BlueprintEngine.Categories.REGISTRY_ITEMS);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(STRING, "Id", "mybundle:my_item");
            input(INT, "Max Stack", 64);
            input(BOOL, "Fire Resistant", false);
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Object ev = ctx.getVar(BlueprintEngine.CTX_REGISTRY_EVENT, Object.class);
            if (!(ev instanceof RegistryEvent regEv)) {
                LOGGER.warn("[Blueprint] Register Item: missing registry event context ({}).", BlueprintEngine.CTX_REGISTRY_EVENT);
                engine.executePin(node, "Out", graph, ctx);
                return;
            }
            String idStr = ctx.resolvePinAs(node.inputPin("Id"), String.class, "");
            Identifier id = Identifier.tryParse(idStr);
            if (id == null) {
                LOGGER.warn("[Blueprint] Register Item: invalid id '{}'", idStr);
                engine.executePin(node, "Out", graph, ctx);
                return;
            }
            int stack = ctx.resolvePinAs(node.inputPin("Max Stack"), Integer.class, 64);
            boolean fire = ctx.resolvePinAs(node.inputPin("Fire Resistant"), Boolean.class, false);
            ItemBuilder b = ItemBuilder.create(id).stacksTo(stack);
            if (fire) b.fireResistant();
            regEv.items(b);
            engine.executePin(node, "Out", graph, ctx);
        }
    }

    public static final class RegisterBlock extends BuiltinNode {
        public RegisterBlock() {
            super("registry.block.register_simple", "Register Block (Simple)", BlueprintEngine.Categories.REGISTRY_BLOCKS);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(STRING, "Id", "mybundle:my_block");
            input(BOOL, "Has Item", true);
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Object ev = ctx.getVar(BlueprintEngine.CTX_REGISTRY_EVENT, Object.class);
            if (!(ev instanceof RegistryEvent regEv)) {
                LOGGER.warn("[Blueprint] Register Block: missing registry event context ({}).", BlueprintEngine.CTX_REGISTRY_EVENT);
                engine.executePin(node, "Out", graph, ctx);
                return;
            }
            String idStr = ctx.resolvePinAs(node.inputPin("Id"), String.class, "");
            Identifier id = Identifier.tryParse(idStr);
            if (id == null) {
                LOGGER.warn("[Blueprint] Register Block: invalid id '{}'", idStr);
                engine.executePin(node, "Out", graph, ctx);
                return;
            }
            boolean hasItem = ctx.resolvePinAs(node.inputPin("Has Item"), Boolean.class, true);
            BlockBuilder b = BlockBuilder.create(id);
            if (!hasItem) b.noItem();
            regEv.blocks(b);
            engine.executePin(node, "Out", graph, ctx);
        }
    }

    public static final class RegisterSound extends BuiltinNode {
        public RegisterSound() {
            super("registry.sound.register_simple", "Register Sound (Simple)", BlueprintEngine.Categories.REGISTRY_SOUNDS);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(STRING, "Id", "mybundle:my_sound");
            input(STRING, "Subtitle", "");
            input(FLOAT, "Range", 16f);
            execOutput("Out");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Object ev = ctx.getVar(BlueprintEngine.CTX_REGISTRY_EVENT, Object.class);
            if (!(ev instanceof RegistryEvent regEv)) {
                LOGGER.warn("[Blueprint] Register Sound: missing registry event context ({}).", BlueprintEngine.CTX_REGISTRY_EVENT);
                engine.executePin(node, "Out", graph, ctx);
                return;
            }
            String idStr = ctx.resolvePinAs(node.inputPin("Id"), String.class, "");
            Identifier id = Identifier.tryParse(idStr);
            if (id == null) {
                LOGGER.warn("[Blueprint] Register Sound: invalid id '{}'", idStr);
                engine.executePin(node, "Out", graph, ctx);
                return;
            }
            String subtitle = ctx.resolvePinAs(node.inputPin("Subtitle"), String.class, "").trim();
            float range = ctx.resolvePinAs(node.inputPin("Range"), Float.class, 16f);
            SoundBuilder b = SoundBuilder.create(id).range(range);
            if (!subtitle.isEmpty()) b.subtitle(subtitle);
            regEv.sounds(b);
            engine.executePin(node, "Out", graph, ctx);
        }
    }
}
