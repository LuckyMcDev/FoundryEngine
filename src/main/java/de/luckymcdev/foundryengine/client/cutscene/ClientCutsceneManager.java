package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.common.cutscene.model.CommandAttachment;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.model.EffectAttachment;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import de.luckymcdev.foundryengine.common.cutscene.network.ScreenEffectPacket;
import de.luckymcdev.foundryengine.common.cutscene.storage.CutsceneSavedData;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.UUID;

public class ClientCutsceneManager {
    public static final double RENDER_PLAYER_RANGE = 1.0;
    private static final ArrayList<QueuedCutscene> cutsceneQueue = new ArrayList<>();
    public static PlayingCutscene currentCutscene;
    public static float holdTimeEnd;
    public static float holdTimeStart;
    public static float currentLengthInTicks;
    public static float currentAgeInTicks;
    public static LerpType currentLerpType = LerpType.LINEAR;
    public static Vec3 pos = Vec3.ZERO;
    public static Vec2 rot = new Vec2(0, 0);
    private static boolean requestedSync = false;
    private static boolean previewActive = false;
    private static Cutscene previewCutscene = null;
    private static float previewT = 0f;

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            requestedSync = false;
            currentCutscene = null;
            cutsceneQueue.clear();
            clearPreview();
            return;
        }

        if (!requestedSync) {
            requestedSync = true;
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Request", true);
            ClientPacketDistributor.sendToServer(new CutscenePacket(tag));
        }
    }

    public static void setPreview(Cutscene cutscene, float t) {
        if (inCutscene()) return;
        previewActive = cutscene != null;
        previewCutscene = cutscene;
        previewT = net.minecraft.util.Mth.clamp(t, 0f, 1f);
    }

    public static void clearPreview() {
        previewActive = false;
        previewCutscene = null;
        previewT = 0f;
    }

    public static boolean isCameraOverrideDisabled() {
        return !inCutscene() && !previewActive;
    }

    public static void renderTick() {
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

        if (mc.screen == null && shouldBlockInput()) {
            KeyMapping.releaseAll();
            mc.player.input.keyPresses = Input.EMPTY;
        }

        float deltaTicks = mc.getDeltaTracker().getGameTimeDeltaTicks();

        if (currentCutscene == null) {
            setCutscene(cutsceneQueue.getFirst());
        }

        float total = currentLengthInTicks + holdTimeStart + holdTimeEnd;
        if (currentAgeInTicks >= total) {
            if (cutsceneQueue.isEmpty()) {
                currentCutscene = null;
                if (!ClientScreenEffectManager.inScreenEffect()) {
                    mc.options.hideGui = false;
                }
                return;
            }
            setCutscene(cutsceneQueue.getFirst());
        }

        currentAgeInTicks += deltaTicks;
        if (currentCutscene == null) return;

        float t = net.minecraft.util.Mth.clamp((currentAgeInTicks - holdTimeStart) / currentLengthInTicks, 0f, 1f);
        float eased = currentLerpType.compute(t);

        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        pos = currentCutscene.getPosAt(eased, partial);
        rot = currentCutscene.getRotAt(eased);
        currentCutscene.tickScreenEffects(t, currentLengthInTicks);
    }

    public static void handlePacket(CutscenePacket packet) {
        CompoundTag tag = packet.nbt();

        if (tag.getBooleanOr("Cancel", false)) {
            if (cutsceneQueue.isEmpty()) {
                currentCutscene = null;
                currentLengthInTicks = 0;
                holdTimeEnd = 0;
                holdTimeStart = 0;
                if (!ClientScreenEffectManager.inScreenEffect()) {
                    Minecraft.getInstance().options.hideGui = false;
                }
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
            return;
        }

        CutsceneRenderer.setCutscenes(CutsceneSavedData.makeList(tag));
    }

    private static void setCutscene(QueuedCutscene queuedCutscene) {
        currentCutscene = queuedCutscene.cutscene;
        currentLengthInTicks = queuedCutscene.length;
        currentLerpType = queuedCutscene.lerpType;
        holdTimeStart = queuedCutscene.holdStart;
        holdTimeEnd = queuedCutscene.holdEnd;
        currentAgeInTicks = 0f;
        cutsceneQueue.removeFirst();
    }

    public static void queueCutscene(PlayingCutscene cutscene, float length, LerpType lerpType, float holdStart, float holdEnd) {
        cutsceneQueue.add(new QueuedCutscene(cutscene, length, lerpType, holdStart, holdEnd));
    }

    public static boolean inCutscene() {
        return currentCutscene != null;
    }

    public static boolean shouldBlockInput() {
        return inCutscene() || ClientScreenEffectManager.disableMovement();
    }

    public record QueuedCutscene(PlayingCutscene cutscene, float length, LerpType lerpType, float holdStart,
                                 float holdEnd) {
    }

    public static final class PlayingCutscene {
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
            if (firedEffects == null || (effects.size() > 0 && firedEffects.length != effects.size())) {
                firedEffects = new boolean[effects.size()];
            }

            // Handle command attachments
            var commands = cutscene.getCommandAttachments();
            if (firedCommands == null || (commands.size() > 0 && firedCommands.length != commands.size())) {
                firedCommands = new boolean[commands.size()];
            }

            if (rawT + 1e-6f < lastEffectT) {
                if (firedEffects != null) {
                    for (int i = 0; i < firedEffects.length; i++) firedEffects[i] = false;
                }
                if (firedCommands != null) {
                    for (int i = 0; i < firedCommands.length; i++) firedCommands[i] = false;
                }
            }
            lastEffectT = rawT;

            // Fire effects
            for (int i = 0; i < effects.size(); i++) {
                if (firedEffects[i]) continue;
                EffectAttachment eff = effects.get(i);
                if (rawT + 1e-6f < eff.getAt()) continue;
                firedEffects[i] = true;
                ClientScreenEffectManager.startEffect(eff.getEffectName(), eff.getIntroTicks(cutsceneLength), eff.getHoldTicks(cutsceneLength), eff.getOutroTicks(cutsceneLength), eff.getLerpType());
            }

            // Fire commands
            for (int i = 0; i < commands.size(); i++) {
                if (firedCommands[i]) continue;
                CommandAttachment cmd = commands.get(i);
                if (rawT + 1e-6f < cmd.getEffectiveAt()) continue;
                firedCommands[i] = true;
                ClientPacketDistributor.sendToServer(new ScreenEffectPacket("", 0, 0, 0, "", cmd.getCommand()));
            }
        }
    }
}
