package de.luckymcdev.foundryengine.common.graph;

import de.luckymcdev.foundryengine.common.graph.domain.ScriptExecHandler;
import de.luckymcdev.foundryengine.common.graph.domain.ScriptRuntimeContext;
import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.model.NodeModel;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public class ScriptRuntime {
    private final List<Runnable> unregistrations = new ArrayList<>();
    private boolean compiled = false;

    public void compile(GraphModel graph, ScriptDomain domain) {
        if (compiled) {
            throw new IllegalStateException("Already compiled. Call dispose() first.");
        }

        for (var node : graph.nodes().values()) {
            var handler = domain.getExecHandler(node.typeRef());
            if (handler == null) continue;

            var eventClass = handler.eventClass();
            if (eventClass == null) continue;

            registerEntry(node, handler, domain, graph, eventClass);
        }

        compiled = true;
    }

    @SuppressWarnings("unchecked")
    private void registerEntry(NodeModel node, ScriptExecHandler handler,
                               ScriptDomain domain, GraphModel graph, Class<?> eventClass) {
        var casted = (Class<? extends Event>) eventClass;
        var listener = new ScriptEventListener(casted, node, handler, domain, graph);
        NeoForge.EVENT_BUS.register(listener);
        unregistrations.add(() -> NeoForge.EVENT_BUS.unregister(listener));
    }

    private void walkExecChain(NodeModel node, ScriptRuntimeContext ctx, ScriptExecHandler handler) {
        handler.execute(node, ctx);

        for (var outputPin : node.outputPins()) {
            var links = ctx.graph().linksFrom(outputPin.id());
            for (var link : links) {
                var toPin = ctx.graph().pin(link.toPin());
                if (toPin == null) continue;
                var nextNode = ctx.graph().nodeForPin(toPin.id());
                if (nextNode == null) continue;
                var nextHandler = ctx.domain().getExecHandler(nextNode.typeRef());
                if (nextHandler == null) continue;
                walkExecChain(nextNode, ctx, nextHandler);
            }
        }
    }

    private record ScriptEventListener(
            Class<?> eventClass,
            NodeModel node,
            ScriptExecHandler handler,
            ScriptDomain domain,
            GraphModel graph
    ) {
        @SubscribeEvent
        public void onEvent(Event event) {
            if (!eventClass.isInstance(event)) return;
            var ctx = new ScriptRuntimeContext(graph, domain, event);
            handler.execute(node, ctx);
        }
    }

    public void dispose() {
        unregistrations.forEach(Runnable::run);
        unregistrations.clear();
        compiled = false;
    }

    public boolean isCompiled() {
        return compiled;
    }
}
