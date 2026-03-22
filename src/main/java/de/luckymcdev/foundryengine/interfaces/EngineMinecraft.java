package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft Extension
 */
public interface EngineMinecraft extends EngineInterface<Minecraft> {

    void engine$init(GameConfig gameConfig, CallbackInfo ci);

    void engine$close(CallbackInfo ci);
}
