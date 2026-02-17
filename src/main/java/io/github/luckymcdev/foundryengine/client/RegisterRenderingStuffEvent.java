package io.github.luckymcdev.foundryengine.client;

import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;

public class RegisterRenderingStuffEvent extends Event{
    private final ResourceManager resourceManager;

    public RegisterRenderingStuffEvent(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
