package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.network.ScreenEffectPacket;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ScreenEffectInstance {
    private final Identifier resourceLocation;
    private final int introTicks;
    private final int holdTicks;
    private final int outroTicks;
    private final LerpType lerpType;
    private final String command;
    private final boolean isNone;

    private float ageInTicks = 0f;
    private boolean hasRunCommand = false;

    public ScreenEffectInstance(String effectName, int introTicks, int holdTicks, int outroTicks, String lerpType, String command) {
        this.isNone = "none".equalsIgnoreCase(effectName) || effectName == null || effectName.isBlank();
        this.resourceLocation = this.isNone ? null : Common.id(effectName);
        this.introTicks = introTicks;
        this.holdTicks = holdTicks;
        this.outroTicks = outroTicks;
        this.lerpType = LerpType.fromString(lerpType);
        this.command = command == null ? "" : command;
    }

    private int totalLengthTicks() {
        return this.holdTicks + this.introTicks + this.outroTicks;
    }

    public void update(float tickDelta) {
        Minecraft mc = Minecraft.getInstance();

        // For "none" type, just handle command execution and complete immediately
        if (this.isNone) {
            if (!hasRunCommand && !command.isBlank()) {
                ClientPacketDistributor.sendToServer(new ScreenEffectPacket("", 0, 0, 0, "", command));
                hasRunCommand = true;
            }
            ClientScreenEffectManager.screenEffect = null;
            if (!ClientCutsceneManager.inCutscene()) {
                mc.options.hideGui = false;
            }
            return;
        }

        if (this.ageInTicks < this.totalLengthTicks()) {
            this.ageInTicks += tickDelta;
            if (this.resourceLocation != null) {
                mc.gameRenderer.setPostEffect(this.resourceLocation);
            }
        } else {
            ClientScreenEffectManager.screenEffect = null;
            mc.gameRenderer.clearPostEffect();
            if (!ClientCutsceneManager.inCutscene()) {
                mc.options.hideGui = false;
            }
        }
    }

    public float getProgress() {
        if (!hasRunCommand && !command.isBlank() && this.ageInTicks > this.introTicks + (this.holdTicks / 2f)) {
            ClientPacketDistributor.sendToServer(new ScreenEffectPacket("", 0, 0, 0, "", command));
            hasRunCommand = true;
        }

        if (this.isNone) return 0f;

        float progress = this.introTicks <= 0 ? 1f : (this.ageInTicks / (float) this.introTicks);

        if (progress > 1 && ageInTicks > this.introTicks + this.holdTicks) {
            float denom = (float) Math.max(1, this.outroTicks);
            progress = 1.0f - ((this.totalLengthTicks() - ageInTicks) / denom);
            progress = 1.0f - this.lerpType.compute(net.minecraft.util.Mth.clamp(progress, 0f, 1f));
        } else {
            progress = this.lerpType.compute(net.minecraft.util.Mth.clamp(progress, 0f, 1f));
        }
        return progress > 1 ? 1.0f : progress;
    }

    public boolean canPlayerMove() {
        return this.ageInTicks < this.introTicks || this.ageInTicks > this.introTicks + this.holdTicks;
    }
}
