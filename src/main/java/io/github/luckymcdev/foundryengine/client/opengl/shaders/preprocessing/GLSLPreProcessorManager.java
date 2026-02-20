package io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing;

import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;

/**
 * Manages All {@link GLSLPreProcessor} and processes them over a Shader source code.
 */
public class GLSLPreProcessorManager {
    private static final GenericRegistry<Identifier, GLSLPreProcessor> PREPROCESSORS = new GenericRegistry<>();

    /**
     * Registers a processor.
     * Do this when {@link RegisterGLSLPreProcessorEvent} is fired.
     *
     * @param preProcessor the pre-processor
     */
    public void register(GLSLPreProcessor preProcessor) {
        PREPROCESSORS.register(preProcessor.getId(), preProcessor);
    }

    /**
     * Removes a processor
     *
     * @param preProcessor the pre-processor
     */
    public void remove(GLSLPreProcessor preProcessor) {
        PREPROCESSORS.remove(preProcessor.getId());
    }

    /**
     * Processes a {@link io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader} Source Code
     * by calling {@link GLSLPreProcessor#apply(String)} for all Registered ones.
     *
     * @param source the source
     * @return the string
     */
    public String processAll(String source) {
        String result = source;
        for (GLSLPreProcessor processor : PREPROCESSORS.values()) {
            result = processor.apply(result);
        }
        return result;
    }
}
