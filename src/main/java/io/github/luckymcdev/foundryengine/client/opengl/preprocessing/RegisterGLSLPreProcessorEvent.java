package io.github.luckymcdev.foundryengine.client.opengl.preprocessing;

import io.github.luckymcdev.foundryengine.client.Client;
import net.neoforged.bus.api.Event;

/**
 * A simple event for registering a {@link GLSLPreProcessor}
 */
public class RegisterGLSLPreProcessorEvent extends Event {
    public void register(GLSLPreProcessor preProcessor) {
        Client.getShaderManager().getPreProcessorManager().register(preProcessor);
    }
}
