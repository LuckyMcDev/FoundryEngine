package de.luckymcdev.foundryengine.common.blueprint.engine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeResult;
import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinInfo;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.common.registry.GenericRegistryList;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class BlueprintEngine {
    public static final String CTX_REGISTRY_EVENT = "_registry_event";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GenericRegistryList<BuiltinNode> builtinNodes = new GenericRegistryList<>();
    private final GenericRegistry<String, BuiltinNode> builtinById = new GenericRegistry<>();
    private final List<ItemBuilder> pendingItems = new ArrayList<>();
    private final List<BlockBuilder> pendingBlocks = new ArrayList<>();
    private final List<RecipeBuilder> pendingRecipes = new ArrayList<>();
    private final List<SoundBuilder> pendingSounds = new ArrayList<>();
    private final List<ParticleBuilder> pendingParticles = new ArrayList<>();
    private final Map<String, Object> globalVars = new HashMap<>();
    private final Map<String, Object> persistentData = new HashMap<>();

    private static boolean hasExecInput(BlueprintNode node) {
        for (var p : node.inputPins) {
            if (p.pin.type() == BlueprintTypes.EXEC) return true;
        }
        return false;
    }

    public void register(BuiltinNode node) {
        builtinNodes.add(node);
        builtinById.register(node.identifier, node);
    }

    public Color getCategoryColor(@Nullable String category) {
        return BlueprintCategories.color(category);
    }

    public GenericRegistryList<BuiltinNode> getBuiltinNodes() {
        return builtinNodes;
    }

    public @Nullable BuiltinNode getById(String identifier) {
        return builtinById.get(identifier);
    }

    public BlueprintNode createNode(BuiltinNode builtin) {
        return builtin.createNode();
    }

    public boolean canConnect(NodePinInfo src, NodePinInfo dst) {
        return src.pin.type().isCompatibleWith(dst.pin.type());
    }

    public void executeGraph(BlueprintGraph graph) {
        executeEvent("event.begin_play", graph);
    }

    public void executeEvent(String eventName, BlueprintGraph graph) {
        executeEvent(eventName, graph, Collections.emptyMap());
    }

    public void executeEvent(String eventName, BlueprintGraph graph, Map<String, Object> payload) {
        for (BlueprintNode node : graph.nodes.values()) {
            if (node.identifier.equals(eventName)) {
                BlueprintContext ctx = new BlueprintContext(graph, this);
                payload.forEach(ctx::setVar);
                if (payload.containsKey("CommandSource") && payload.get("CommandSource") instanceof CommandSourceStack cs) {
                    ctx = ctx.withCommandSource(cs);
                }
                if (!payload.isEmpty()) {
                    for (var out : node.outputPins) {
                        if (out.pin.type() == BlueprintTypes.EXEC) continue;
                        Object v = payload.get(out.pin.label());
                        if (v != null) node.setOutput(out.pin.label(), v);
                    }
                }
                executeNode(node, graph, ctx);
            }
        }
    }

    /**
     * Runs a node's executor and then follows its exec output to the next node.
     * Called when entering a node for the first time (from executeEvent or executePin).
     */
    public void executeNode(BlueprintNode node, BlueprintGraph graph, BlueprintContext ctx) {
        if (ctx.isCancelled()) return;
        if (!ctx.tickDepth()) {
            LOGGER.error("Blueprint execution exceeded max depth – aborting chain. Node: {}", node.identifier);
            return;
        }
        try {
            BuiltinNode builtin = builtinById.get(node.identifier);
            if (builtin != null) {
                builtin.execute(node, this, graph, ctx);
            }
            for (var pin : node.outputPins) {
                if (pin.pin.type() == BlueprintTypes.EXEC) {
                    executePin(node, pin.pin.label(), graph, ctx);
                    return;
                }
            }
        } finally {
            ctx.untickDepth();
        }
    }

    /**
     * Follows a node's exec output to the next connected node.
     * Called by node executors when they want to continue the chain.
     * Does NOT re-run the current node's executor.
     */
    public void continueChain(BlueprintNode node, BlueprintGraph graph, BlueprintContext ctx) {
        for (var pin : node.outputPins) {
            if (pin.pin.type() == BlueprintTypes.EXEC) {
                executePin(node, pin.pin.label(), graph, ctx);
                return;
            }
        }
    }

    public void executePin(BlueprintNode node, String pinLabel, BlueprintGraph graph, BlueprintContext ctx) {
        if (ctx.isCancelled()) return;
        var pin = node.outputPin(pinLabel);
        if (pin != null) {
            var connectedInput = graph.getConnectedInputPin(pin);
            if (connectedInput != null) {
                executeNode(connectedInput.node, graph, ctx);
            }
        }
    }

    /**
     * @deprecated Use {@link #executeNode} or {@link #continueChain} instead.
     */
    @Deprecated
    public void executeNext(BlueprintNode node, BlueprintGraph graph, BlueprintContext ctx) {
        executeNode(node, graph, ctx);
    }

    public void registerBuiltins() {
        de.luckymcdev.foundryengine.common.blueprint.event.BuiltinEventNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.command.ExecuteModifierNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.command.TeleportNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.command.InteractionNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.command.WorldNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.command.InfoNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.command.AdminNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.kernel.KernelNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.reflect.ReflectNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.event.EventControlNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.entity.EntityNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.data.GlobalNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.data.TriggerNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.data.NbtNodes.registerAll(this);
        de.luckymcdev.foundryengine.common.blueprint.builder.BuilderNodes.registerAll(this);
    }

    public void addPendingBuilder(BuilderBase<?> builder) {
        if (builder instanceof ItemBuilder ib) pendingItems.add(ib);
        else if (builder instanceof BlockBuilder bb) pendingBlocks.add(bb);
        else if (builder instanceof RecipeBuilder rb) pendingRecipes.add(rb);
        else if (builder instanceof SoundBuilder sb) pendingSounds.add(sb);
        else if (builder instanceof ParticleBuilder pb) pendingParticles.add(pb);
    }

    public void processPendingItemRegistrations(RegisterEvent.RegisterHelper<net.minecraft.world.item.Item> helper) {
        if (pendingItems.isEmpty()) return;
        LOGGER.info("[Blueprint] Registering {} pending items...", pendingItems.size());
        for (ItemBuilder builder : pendingItems) {
            Identifier id = builder.getId();
            try {
                builder.register(helper);
                LOGGER.info("[Blueprint] Registered item: {}", id);
            } catch (Exception e) {
                LOGGER.error("[Blueprint] Failed to register item: {}", id, e);
            }
        }
        pendingItems.clear();
    }

    public void processPendingBlockRegistrations(RegisterEvent.RegisterHelper<net.minecraft.world.level.block.Block> helper) {
        if (pendingBlocks.isEmpty()) return;
        LOGGER.info("[Blueprint] Registering {} pending blocks...", pendingBlocks.size());
        for (BlockBuilder builder : pendingBlocks) {
            Identifier id = builder.getId();
            try {
                builder.registerBlock(helper);
                LOGGER.info("[Blueprint] Registered block: {}", id);
            } catch (Exception e) {
                LOGGER.error("[Blueprint] Failed to register block: {}", id, e);
            }
        }
        pendingBlocks.clear();
    }

    public void processPendingRecipeRegistrations(RegisterEvent.RegisterHelper<RecipeResult> helper) {
        if (pendingRecipes.isEmpty()) return;
        LOGGER.info("[Blueprint] Registering {} pending recipes...", pendingRecipes.size());
        for (RecipeBuilder builder : pendingRecipes) {
            Identifier id = builder.getId();
            try {
                builder.register(helper);
                LOGGER.info("[Blueprint] Registered recipe: {}", id);
            } catch (Exception e) {
                LOGGER.error("[Blueprint] Failed to register recipe: {}", id, e);
            }
        }
        pendingRecipes.clear();
    }

    public void processPendingSoundRegistrations(RegisterEvent.RegisterHelper<net.minecraft.sounds.SoundEvent> helper) {
        if (pendingSounds.isEmpty()) return;
        LOGGER.info("[Blueprint] Registering {} pending sounds...", pendingSounds.size());
        for (SoundBuilder builder : pendingSounds) {
            Identifier id = builder.getId();
            try {
                builder.register(helper);
                LOGGER.info("[Blueprint] Registered sound: {}", id);
            } catch (Exception e) {
                LOGGER.error("[Blueprint] Failed to register sound: {}", id, e);
            }
        }
        pendingSounds.clear();
    }

    public void processPendingParticleRegistrations(RegisterEvent.RegisterHelper<net.minecraft.core.particles.ParticleType<?>> helper) {
        if (pendingParticles.isEmpty()) return;
        LOGGER.info("[Blueprint] Registering {} pending particles...", pendingParticles.size());
        for (ParticleBuilder builder : pendingParticles) {
            Identifier id = builder.getId();
            try {
                builder.register(helper);
                LOGGER.info("[Blueprint] Registered particle: {}", id);
            } catch (Exception e) {
                LOGGER.error("[Blueprint] Failed to register particle: {}", id, e);
            }
        }
        pendingParticles.clear();
    }

    // ======================== Global Variables (session-scoped) ========================

    public void setGlobalVar(String name, @Nullable Object value) {
        globalVars.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <V> @Nullable V getGlobalVar(String name, Class<V> type) {
        Object v = globalVars.get(name);
        if (v == null) return null;
        if (type.isInstance(v)) return (V) v;
        return null;
    }

    public boolean hasGlobalVar(String name) {
        return globalVars.containsKey(name);
    }

    // ======================== Persistent Data (world-scoped, in-memory for now) ========================

    public void setPersistentData(String key, @Nullable Object value) {
        persistentData.put(key, value);
    }

    public @Nullable Object getPersistentData(String key) {
        return persistentData.get(key);
    }

    public boolean hasPersistentData(String key) {
        return persistentData.containsKey(key);
    }
}
