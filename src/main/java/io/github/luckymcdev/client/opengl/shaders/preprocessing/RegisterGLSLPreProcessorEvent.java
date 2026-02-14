package io.github.luckymcdev.client.opengl.shaders.preprocessing;

import io.github.luckymcdev.common.Instances;
import net.neoforged.bus.api.Event;

public class RegisterGLSLPreProcessorEvent extends Event {
    public void register(GLSLPreProcessor preProcessor) {
        Instances.getShaderManager().getPreProcessorManager().register(preProcessor);
    }
}
