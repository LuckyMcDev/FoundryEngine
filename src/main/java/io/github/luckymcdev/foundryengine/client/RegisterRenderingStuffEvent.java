package io.github.luckymcdev.foundryengine.client;

import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;

public class RegisterRenderingStuffEvent extends Event{
    private final TbRenderer tbRenderer;
    private final ResourceManager resourceManager;

    public RegisterRenderingStuffEvent(TbRenderer tbRenderer, ResourceManager resourceManager) {
        this.tbRenderer = tbRenderer;
        this.resourceManager = resourceManager;
    }

    public TbRenderer getTbRenderer() {
        return tbRenderer;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
