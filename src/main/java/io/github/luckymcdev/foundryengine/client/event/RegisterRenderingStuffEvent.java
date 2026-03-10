package io.github.luckymcdev.foundryengine.client.event;

import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Simple Event to register "Rendering Stuff"
 */
@ApiStatus.Experimental
public class RegisterRenderingStuffEvent extends Event {
    private final ResourceManager resourceManager;

    public RegisterRenderingStuffEvent(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
