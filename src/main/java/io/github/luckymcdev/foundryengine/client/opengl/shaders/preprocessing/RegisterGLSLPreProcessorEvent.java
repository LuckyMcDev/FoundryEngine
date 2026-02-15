package io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing;

import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.bus.api.Event;

public class RegisterGLSLPreProcessorEvent extends Event {
    public void register(GLSLPreProcessor preProcessor) {
        Instances.getShaderManager().getPreProcessorManager().register(preProcessor);
    }
}
