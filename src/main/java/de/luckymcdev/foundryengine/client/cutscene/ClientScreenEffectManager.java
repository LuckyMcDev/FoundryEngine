package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.cutscene.network.ScreenEffectPacket;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side screen effect playback manager.
 * <p>
 * Owned by {@link Client} as a singleton instance (not static state).
 */
public class ClientScreenEffectManager {
    @Nullable
    private ScreenEffectInstance screenEffect;

    public void handlePacket(ScreenEffectPacket packet) {
        startEffect(packet.name(), packet.introTicks(), packet.holdTicks(), packet.outroTicks(), packet.lerpType());
    }

    public void startEffect(String name, int introTicks, int holdTicks, int outroTicks, String lerpType) {
        screenEffect = new ScreenEffectInstance(name, introTicks, holdTicks, outroTicks, lerpType);
        Minecraft.getInstance().options.hideGui = true;
    }

    public void renderTick() {
        if (!inScreenEffect()) return;
        float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        screenEffect.update(delta, this);
    }

    public boolean inScreenEffect() {
        return screenEffect != null;
    }

    public void stopEffect() {
        if (screenEffect != null) {
            Minecraft.getInstance().gameRenderer.clearPostEffect();
            screenEffect = null;
            if (!Client.getCutsceneManager().inCutscene()) {
                Minecraft.getInstance().options.hideGui = false;
            }
        }
    }

    @Nullable
    public ScreenEffectInstance getScreenEffect() {
        return screenEffect;
    }

    void clearActiveEffect() {
        screenEffect = null;
    }
}

