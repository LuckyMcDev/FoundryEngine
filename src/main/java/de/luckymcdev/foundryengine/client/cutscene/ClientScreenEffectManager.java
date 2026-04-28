package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.common.cutscene.network.ScreenEffectPacket;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class ClientScreenEffectManager {
    @Nullable
    public static ScreenEffectInstance screenEffect;

    public static void handlePacket(ScreenEffectPacket packet) {
        screenEffect = new ScreenEffectInstance(packet.name(), packet.introTicks(), packet.holdTicks(), packet.outroTicks(), packet.lerpType(), packet.command());
        Minecraft.getInstance().options.hideGui = true;
    }

    public static void clientTick() {
        // no-op for now
    }

    public static void renderTick() {
        if (!inScreenEffect()) return;
        float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        screenEffect.update(delta);
    }

    public static boolean inScreenEffect() {
        return screenEffect != null;
    }

    public static boolean disableMovement() {
        return inScreenEffect() && !screenEffect.canPlayerMove();
    }
}

