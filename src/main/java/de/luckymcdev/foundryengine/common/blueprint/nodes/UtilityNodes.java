package de.luckymcdev.foundryengine.common.blueprint.nodes;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.PLAYER;
import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.STRING;

public final class UtilityNodes {
    private static final Logger LOGGER = LogUtils.getLogger();

    private UtilityNodes() {
    }

    public static final class PrintString extends BuiltinNode {
        public PrintString() {
            super("Print String", BlueprintEngine.Categories.UTILS);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(STRING, "String", "Hello");
            execOutput("Continue");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            String text = ctx.resolvePinAs(node.inputPin("String"), String.class, "");
            LOGGER.info("[Blueprint] {}", text);
        }
    }

    public static final class Tell extends BuiltinNode {
        public Tell() {
            super("Tell", BlueprintEngine.Categories.UTILS);
        }

        @Override
        protected void initPins() {
            execInput("In");
            input(PLAYER, "Target");
            input(STRING, "Message", "Notification");
            execOutput("Continue");
        }

        @Override
        public void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx) {
            Player player = ctx.resolvePinAs(node.inputPin("Target"), Player.class, null);
            String msg = ctx.resolvePinAs(node.inputPin("Message"), String.class, "");
            if (player != null) player.sendSystemMessage(Component.literal(msg));
        }
    }
}
