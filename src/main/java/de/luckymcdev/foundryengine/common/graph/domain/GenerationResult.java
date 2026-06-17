package de.luckymcdev.foundryengine.common.graph.domain;

import com.google.gson.JsonElement;

/**
 * Sealed result type for graph generation, so domains can return
 * richer output than a plain String.
 */
public sealed interface GenerationResult {

    record ShaderResult(
            String vertexSource,
            String fragmentSource,
            java.util.List<String> uniforms
    ) implements GenerationResult {}

    record ScriptResult(
            String source,
            java.util.Map<String, String> eventListeners
    ) implements GenerationResult {}

    record JsonResult(
            JsonElement element
    ) implements GenerationResult {}
}
