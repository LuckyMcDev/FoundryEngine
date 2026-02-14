package io.github.luckymcdev.client.opengl.shaders.preprocessing;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import io.github.luckymcdev.common.registry.GenericRegistry;
import net.minecraft.resources.ResourceLocation;

public class GLSLPreProcessorManager {
    private static final GenericRegistry<ResourceLocation, GLSLPreProcessor> PREPROCESSORS = new GenericRegistry<>();

    public void register(GLSLPreProcessor preProcessor) {
        PREPROCESSORS.register(preProcessor.getId(), preProcessor);
    }

    public void remove(GLSLPreProcessor preProcessor) {
        PREPROCESSORS.remove(preProcessor.getId());
    }

    public String processAll(String source) {
        String result = source;
        for (GLSLPreProcessor processor : PREPROCESSORS.getValues()) {
            result = processor.apply(result);
        }
        return result;
    }
}
