package de.luckymcdev.foundryengine.common.graph.domain;

import de.luckymcdev.foundryengine.common.graph.type.PinType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Accumulates GLSL output during shader graph traversal.
 */
public interface ShaderCodegenContext {

    String tempVar(PinType type);

    void emitMain(String line, ShaderStage stage);

    void emitGlobal(String declaration, ShaderStage stage);

    void mapPinToVar(UUID pinId, String varName);

    @Nullable String varForPin(UUID pinId);

    Collection<String> uniforms();

    String vertexSource();

    String fragmentSource();
}
