package io.github.luckymcdev.foundryengine.client.event;

import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;

/**
 * Simple Event to register "Rendering Stuff"
 */
public class RegisterRenderingStuffEvent extends Event {
    private final ResourceManager resourceManager;

    public RegisterRenderingStuffEvent(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
