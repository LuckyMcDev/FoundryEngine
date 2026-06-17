package de.luckymcdev.foundryengine.common.graph.domain;

import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.model.NodeModel;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import org.jetbrains.annotations.Nullable;

/**
 * Per-node handler for the shader domain.
 * <p>
 * Each participating {@link de.luckymcdev.foundryengine.common.graph.registry.NodeDefinition}
 * registers one of these in {@code ShaderDomain}.
 */
public interface ShaderNodeHandler {

    /**
     * Emit GLSL for this node's output expression, given already-resolved
     * input expressions via {@code inputs}.
     *
     * @param node the node model being generated
     * @param graph the full graph (for context / lookups)
     * @param inputs resolved input expressions, accessible by positional index or pin UUID
     * @param ctx accumulation context for uniforms, varyings, temp vars
     * @return the GLSL expression for this node's output
     */
    String emitGLSL(NodeModel node, GraphModel graph, InputResolver inputs, ShaderCodegenContext ctx);

    /**
     * Which shader stage this node produces code for.
     */
    ShaderStage stage();

    /**
     * Given connected input types, resolve this node's output pin types.
     * Called during validation to propagate types through the graph.
     *
     * @param inputTypes resolved input types (null entries = disconnected)
     * @return resolved output type for each output pin (by pin UUID), or null if indeterminate
     */
    default PinType @Nullable [] resolveOutputTypes(PinType @Nullable [] inputTypes) {
        return null;
    }
}
