package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft Extension
 */
public interface EngineMinecraft {
    default Minecraft tb$self() {
        return (Minecraft) this;
    }

    void tb$init(GameConfig gameConfig, CallbackInfo ci);
}
