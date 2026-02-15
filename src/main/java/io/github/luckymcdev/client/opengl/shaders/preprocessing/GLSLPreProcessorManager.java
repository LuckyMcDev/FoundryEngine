package io.github.luckymcdev.client.opengl.shaders.preprocessing;

import io.github.luckymcdev.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;

/**
 * A Manager for all Glsl Pre-Processors.
 */
public class GLSLPreProcessorManager {
    private static final GenericRegistry<Identifier, GLSLPreProcessor> PREPROCESSORS = new GenericRegistry<>();

    /**
     * Registers a processor.
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
     * Process all string.
     *
     * @param source the source
     * @return the string
     */
    public String processAll(String source) {
        String result = source;
        for (GLSLPreProcessor processor : PREPROCESSORS.getValues()) {
            result = processor.apply(result);
        }
        return result;
    }
}
