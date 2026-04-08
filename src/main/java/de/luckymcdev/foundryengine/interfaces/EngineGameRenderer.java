package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

/**
 * Game Renderer Extension
 */
public interface EngineGameRenderer extends EngineInterface<GameRenderer> {
    void engine$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);

    void engine$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);

    void engine$addEffect(Identifier id, int priority);

    void engine$removeEffect(Identifier id);

    Collection<Identifier> engine$getActiveEffects();

    void engine$clearEffects();
}
