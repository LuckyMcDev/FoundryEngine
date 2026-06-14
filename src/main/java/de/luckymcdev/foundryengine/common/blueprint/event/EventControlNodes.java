package de.luckymcdev.foundryengine.common.blueprint.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;

public final class EventControlNodes {

    private EventControlNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerCancelEvent(engine);
        registerSetResult(engine);
    }

    /**
     * Cancels the current event (e.g. block break, living hurt).
     * Any event execution after Cancel Event is skipped.
     */
    private static void registerCancelEvent(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("event.cancel", "Cancel Event", "Events/Misc",
                node -> {
                    node.execInput("Exec");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    ctx.setCancelled(true);
                    e.continueChain(n, g, ctx);
                }));
    }

    /**
     * Sets a result value on the event context.
     * Used by events that can modify outcomes (e.g. LivingHurt to change damage).
     */
    private static void registerSetResult(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("event.set_result", "Set Result", "Events/Misc",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Value");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    ctx.setResult(value);
                    e.continueChain(n, g, ctx);
                }));
    }
}
