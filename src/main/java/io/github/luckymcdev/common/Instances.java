package io.github.luckymcdev.common;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

public interface Instances {
    static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    static ResourceManager getResourceManager() {
        return getMinecraft().getResourceManager();
    }
}
