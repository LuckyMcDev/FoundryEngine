package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.cutscene.model.CommandAttachment;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.model.EffectAttachment;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutsceneCommandPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutscenePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Client-side cutscene playback manager.
 * <p>
 * Owned by {@link Client} as a singleton instance (not static state).
 */
public class ClientCutsceneManager {
    /**
     * Maximum distance in blocks from the player for cutscene camera override to be active.
     */
    public static final double RENDER_PLAYER_RANGE = 1.0;

    private final ArrayList<QueuedCutscene> cutsceneQueue = new ArrayList<>();

    private PlayingCutscene currentCutscene;
    private float holdTimeEnd;
    private float holdTimeStart;
    private float currentLengthInTicks;
    private float currentAgeInTicks;
    private LerpType currentLerpType = LerpType.LINEAR;

    private Vec3 pos = Vec3.ZERO;
    private Vec2 rot = new Vec2(0, 0);

    private boolean previewActive = false;
    private Cutscene previewCutscene = null;
    private float previewT = 0f;

    public void clientTick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            currentCutscene = null;
            cutsceneQueue.clear();
            clearPreview();
        }
    }

    public void setPreview(Cutscene cutscene, float t) {
        if (inCutscene()) return;
        previewActive = cutscene != null;
        previewCutscene = cutscene;
        previewT = net.minecraft.util.Mth.clamp(t, 0f, 1f);
    }

    public void clearPreview() {
        previewActive = false;
        previewCutscene = null;
        previewT = 0f;
    }

    public boolean isCameraOverrideDisabled() {
        return !inCutscene() && !previewActive;
    }

    public void renderTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!inCutscene() && previewActive && previewCutscene != null) {
            pos = previewCutscene.getPosAt(previewT);
            rot = previewCutscene.getRotAt(previewT);
            return;
        }

        boolean anyQueued = currentCutscene != null || !cutsceneQueue.isEmpty();
        if (!anyQueued) return;

        mc.options.hideGui = true;

        float deltaTicks = mc.getDeltaTracker().getGameTimeDeltaTicks();

        if (currentCutscene == null) {
            setCutscene(cutsceneQueue.getFirst());
        }

        float total = currentLengthInTicks + holdTimeStart + holdTimeEnd
                + (currentCutscene != null ? currentCutscene.cutscene.getTotalAnchorHoldTicks() : 0);
        if (currentAgeInTicks >= total) {
            if (cutsceneQueue.isEmpty()) {
                currentCutscene = null;
                if (!Client.getPostEffectManager().inScreenEffect()) {
                    mc.options.hideGui = false;
                }
                return;
            }
            setCutscene(cutsceneQueue.getFirst());
        }

        currentAgeInTicks += deltaTicks;
        if (currentCutscene == null) return;

        float t = currentCutscene.cutscene.computeProgress(currentAgeInTicks, currentLengthInTicks, holdTimeStart, holdTimeEnd);
        float eased = currentLerpType.compute(t);

        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        pos = currentCutscene.getPosAt(eased, partial);
        rot = currentCutscene.getRotAt(eased);
        currentCutscene.tickScreenEffects(t, currentLengthInTicks);
    }

    public void handlePacket(CutscenePacket packet) {
        CompoundTag tag = packet.nbt();

        if (tag.getBooleanOr("Cancel", false)) {
            Client.getPostEffectManager().stopScreenEffect();
            if (cutsceneQueue.isEmpty()) {
                currentCutscene = null;
                currentLengthInTicks = 0;
                holdTimeEnd = 0;
                holdTimeStart = 0;
                Minecraft.getInstance().options.hideGui = false;
                return;
            }
            setCutscene(cutsceneQueue.getFirst());
            return;
        }

        String playName = tag.getStringOr("PlayName", "");
        if (playName != null && !playName.isBlank()) {
            var base = CutsceneRenderer.findByName(playName);
            if (base == null) return;

            UUID startUuid = tag.read("startPlayer", UUIDUtil.CODEC).orElse(null);
            UUID endUuid = tag.read("endPlayer", UUIDUtil.CODEC).orElse(null);

            var cutscene = base;
            if (startUuid != null) {
                var p = Minecraft.getInstance().level.getPlayerByUUID(startUuid);
                if (p != null) cutscene = cutscene.originAtPlayer(p);
            }
            if (endUuid != null) {
                var p = Minecraft.getInstance().level.getPlayerByUUID(endUuid);
                if (p != null) cutscene = cutscene.endAtPlayer(p);
            }

            int length = tag.getIntOr("Length", 0);
            int holdStart = tag.getIntOr("holdStart", 0);
            int holdEnd = tag.getIntOr("holdEnd", 0);
            LerpType easing = LerpType.fromString(tag.getStringOr("LerpType", LerpType.LINEAR.name()));

            PlayingCutscene playingCutscene = new PlayingCutscene(cutscene, startUuid, endUuid);
            queueCutscene(playingCutscene, length, easing, holdStart, holdEnd);
        }
    }

    public boolean inCutscene() {
        return currentCutscene != null;
    }

    public Vec3 getPos() {
        return pos;
    }

    public Vec2 getRot() {
        return rot;
    }

    private void setCutscene(QueuedCutscene queuedCutscene) {
        currentCutscene = queuedCutscene.cutscene;
        currentLengthInTicks = queuedCutscene.length;
        currentLerpType = queuedCutscene.lerpType;
        holdTimeStart = queuedCutscene.holdStart;
        holdTimeEnd = queuedCutscene.holdEnd;
        currentAgeInTicks = 0f;
        cutsceneQueue.removeFirst();
    }

    public void queueCutscene(PlayingCutscene cutscene, float length, LerpType lerpType, float holdStart, float holdEnd) {
        cutsceneQueue.add(new QueuedCutscene(cutscene, length, lerpType, holdStart, holdEnd));
    }

    public record QueuedCutscene(PlayingCutscene cutscene, float length, LerpType lerpType, float holdStart,
                                 float holdEnd) {
    }

    public final class PlayingCutscene {
        public final Cutscene cutscene;
        public final UUID startPlayerUuid;
        public final UUID endPlayerUuid;

        private float lastEffectT = -1f;
        private boolean[] firedEffects = null;
        private boolean[] firedCommands = null;

        public PlayingCutscene(Cutscene cutscene, UUID startPlayerUuid, UUID endPlayerUuid) {
            this.cutscene = cutscene;
            this.startPlayerUuid = startPlayerUuid;
            this.endPlayerUuid = endPlayerUuid;
        }

        public Vec3 getPosAt(float t, float partialTick) {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                if (startPlayerUuid != null) {
                    var p = level.getPlayerByUUID(startPlayerUuid);
                    if (p != null) {
                        cutscene.path.getPoints().getFirst().setPos(p.getEyePosition(partialTick));
                        cutscene.setInitRot(new Vec2(p.getXRot(), p.getYRot()));
                    }
                }
                if (endPlayerUuid != null) {
                    var p = level.getPlayerByUUID(endPlayerUuid);
                    if (p != null) {
                        cutscene.path.getPoints().getLast().setPos(p.getEyePosition(partialTick));
                        cutscene.setFinalRot(new Vec2(p.getXRot(), p.getYRot()));
                    }
                }
            }
            return cutscene.getPosAt(t);
        }

        public Vec2 getRotAt(float t) {
            return cutscene.getRotAt(t);
        }

        public void tickScreenEffects(float rawT, float cutsceneLength) {
            // Handle effect attachments
            var effects = cutscene.getEffectAttachments();
            if (firedEffects == null || (!effects.isEmpty() && firedEffects.length != effects.size())) {
                firedEffects = new boolean[effects.size()];
            }

            // Handle command attachments
            var commands = cutscene.getCommandAttachments();
            if (firedCommands == null || (!commands.isEmpty() && firedCommands.length != commands.size())) {
                firedCommands = new boolean[commands.size()];
            }

            if (rawT + 1e-6f < lastEffectT) {
                if (firedEffects != null) {
                    Arrays.fill(firedEffects, false);
                }
                if (firedCommands != null) {
                    Arrays.fill(firedCommands, false);
                }
            }
            lastEffectT = rawT;

            // Fire effects
            for (int i = 0; i < effects.size(); i++) {
                if (firedEffects[i]) continue;
                EffectAttachment eff = effects.get(i);
                if (rawT + 1e-6f < eff.getAt()) continue;
                firedEffects[i] = true;
                Client.getPostEffectManager().startScreenEffect(
                        eff.getEffectName(),
                        eff.getIntroTicks(cutsceneLength),
                        eff.getHoldTicks(cutsceneLength),
                        eff.getOutroTicks(cutsceneLength),
                        eff.getLerpType()
                );
            }

            // Fire commands
            for (int i = 0; i < commands.size(); i++) {
                if (firedCommands[i]) continue;
                CommandAttachment cmd = commands.get(i);
                if (rawT + 1e-6f < cmd.getEffectiveAt()) continue;
                firedCommands[i] = true;
                ClientPacketDistributor.sendToServer(new CutsceneCommandPacket(cmd.getCommand()));
            }
        }
    }
}

