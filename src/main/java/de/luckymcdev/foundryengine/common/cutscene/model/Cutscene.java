package de.luckymcdev.foundryengine.common.cutscene.model;

import de.luckymcdev.foundryengine.common.easing.BezierPath;
import de.luckymcdev.foundryengine.common.easing.BezierPoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;

public class Cutscene {
    public final BezierPath path;
    private final String name;
    private final ArrayList<Vec2> anchorRotations = new ArrayList<>();
    private final ArrayList<CutsceneAttachment> attachments = new ArrayList<>();
    private int colorArgb;
    private int defaultLength = 60;
    private int defaultHoldStart = 0;
    private int defaultHoldEnd = 0;
    private String defaultEasing = "LINEAR";

    public Cutscene(String name, Vec2 initialRot, Vec2 finalRot, BezierPath path) {
        this.name = name;
        this.path = path;
        this.colorArgb = defaultColorFromName(name);
        initAnchorRotationsFromEndpoints(initialRot, finalRot);
    }

    public static Cutscene fromNbt(CompoundTag tag) {
        String name = tag.getStringOr("Name", "cutscene");
        BezierPath path = new BezierPath(tag.getListOrEmpty("BezierPath"));

        ArrayList<Vec2> rots = new ArrayList<>();
        ListTag rotList = tag.getListOrEmpty("Rotations");
        for (int i = 0; i < rotList.size(); i++) {
            CompoundTag rotTag = rotList.getCompoundOrEmpty(i);
            float pitch = rotTag.getFloatOr("Pitch", 0f);
            float yaw = rotTag.getFloatOr("Yaw", 0f);
            rots.add(new Vec2(pitch, yaw));
        }

        Vec2 initRot = new Vec2(tag.getFloatOr("InitPitch", 0f), tag.getFloatOr("InitYaw", 0f));
        Vec2 finalRot = new Vec2(tag.getFloatOr("FinalPitch", 0f), tag.getFloatOr("FinalYaw", 0f));

        Cutscene cutscene = new Cutscene(name, initRot, finalRot, path);
        cutscene.colorArgb = tag.getIntOr("Color", defaultColorFromName(name));
        if (!rots.isEmpty()) {
            cutscene.anchorRotations.clear();
            cutscene.anchorRotations.addAll(rots);
        }

        cutscene.defaultLength = tag.getIntOr("DefaultLength", 60);
        cutscene.defaultHoldStart = tag.getIntOr("DefaultHoldStart", 0);
        cutscene.defaultHoldEnd = tag.getIntOr("DefaultHoldEnd", 0);
        cutscene.defaultEasing = tag.getStringOr("DefaultEasing", "LINEAR");

        // Attachments (with backward compatibility for old ScreenEffects)
        cutscene.attachments.clear();
        if (tag.contains("Attachments")) {
            ListTag attachmentList = tag.getListOrEmpty("Attachments");
            for (int i = 0; i < attachmentList.size(); i++) {
                CompoundTag attTag = attachmentList.getCompoundOrEmpty(i);
                CutsceneAttachment att = deserializeAttachment(attTag);
                if (att != null) cutscene.attachments.add(att);
            }
        } else if (tag.contains("ScreenEffects")) {
            // Migration: convert old ScreenEffectEvent to EffectAttachment and CommandAttachment
            ListTag effectList = tag.getListOrEmpty("ScreenEffects");
            for (int i = 0; i < effectList.size(); i++) {
                CompoundTag effTag = effectList.getCompoundOrEmpty(i);
                // Create EffectAttachment from old format
                EffectAttachment eff = EffectAttachment.fromNbt(effTag);
                cutscene.attachments.add(eff);
                // If there was a command, create a separate CommandAttachment
                String cmd = effTag.getStringOr("Command", "");
                if (!cmd.isBlank()) {
                    CommandAttachment cmdAtt = new CommandAttachment(eff.getAt(), cmd, 0f);
                    cutscene.attachments.add(cmdAtt);
                }
            }
        }
        cutscene.attachments.sort(Comparator.comparingDouble(a -> a.getAt()));

        cutscene.ensureAnchorRotations();
        return cutscene;
    }

    private static int defaultColorFromName(String name) {
        if (name == null) name = "cutscene";
        int h = name.hashCode();
        // Stable, high-contrast-ish palette via HSV.
        float hue = ((h >>> 1) & 0xFFFF) / 65535f;
        int rgb = Mth.hsvToRgb(hue, 0.65f, 0.95f); // 0xRRGGBB
        return 0xFF000000 | rgb;
    }

    /**
     * Deserializes an attachment from NBT, handling different attachment types.
     */
    private static CutsceneAttachment deserializeAttachment(CompoundTag tag) {
        String type = tag.getStringOr("Type", "");
        return switch (type) {
            case EffectAttachment.TYPE -> EffectAttachment.fromNbt(tag);
            case CommandAttachment.TYPE -> CommandAttachment.fromNbt(tag);
            default -> null; // Unknown type, skip
        };
    }

    public String getName() {
        return name;
    }

    public Vec2 getInitialRot() {
        ensureAnchorRotations();
        return anchorRotations.getFirst();
    }

    public Vec2 getFinalRot() {
        ensureAnchorRotations();
        return anchorRotations.getLast();
    }

    public void setFinalRot(Vec2 rot) {
        setAnchorRotation(getAnchorPointCount() - 1, rot);
    }

    public void setInitRot(Vec2 rot) {
        setAnchorRotation(0, rot);
    }

    public Vec3 getPosAt(float t) {
        return path.lerpSpeedWeighted(t);
    }

    public Vec2 getRotAt(float t) {
        ensureAnchorRotations();

        int anchors = getAnchorPointCount();
        if (anchors <= 0) return new Vec2(0, 0);
        if (anchors == 1) return anchorRotations.getFirst();

        if (t <= 0f) return anchorRotations.getFirst();
        if (t >= 1f) return anchorRotations.getLast();

        double[] keys = getAnchorDistanceKeys();

        int seg = 0;
        for (int i = 0; i < keys.length - 1; i++) {
            if (t >= keys[i] && t <= keys[i + 1]) {
                seg = i;
                break;
            }
        }

        double a = keys[seg];
        double b = keys[seg + 1];
        float local = (float) ((b - a) <= 1e-6 ? 0.0 : ((t - a) / (b - a)));

        Vec2 r1 = anchorRotations.get(seg);
        Vec2 r2 = anchorRotations.get(seg + 1);
        float pitch = Mth.rotLerp(local, r1.x, r2.x);
        float yaw = Mth.rotLerp(local, r1.y, r2.y);
        return new Vec2(pitch, yaw);
    }

    public Cutscene originAtPlayer(Player player) {
        BezierPath newPath = this.path.withPlayerOrigin(player);
        Cutscene c = new Cutscene(name, getInitialRot(), getFinalRot(), newPath);
        c.anchorRotations.clear();
        c.anchorRotations.addAll(this.anchorRotations);
        c.ensureAnchorRotations();
        c.setInitRot(new Vec2(player.getXRot(), player.getYRot()));
        return c;
    }

    public Cutscene endAtPlayer(Player player) {
        BezierPath newPath = this.path.withPlayerEnd(player);
        Cutscene c = new Cutscene(name, getInitialRot(), getFinalRot(), newPath);
        c.anchorRotations.clear();
        c.anchorRotations.addAll(this.anchorRotations);
        c.ensureAnchorRotations();
        c.setFinalRot(new Vec2(player.getXRot(), player.getYRot()));
        return c;
    }

    public CompoundTag toNbt() {
        ensureAnchorRotations();

        CompoundTag tag = new CompoundTag();
        tag.putString("Name", this.name);
        tag.putInt("Color", this.colorArgb);
        tag.putInt("DefaultLength", this.defaultLength);
        tag.putInt("DefaultHoldStart", this.defaultHoldStart);
        tag.putInt("DefaultHoldEnd", this.defaultHoldEnd);
        tag.putString("DefaultEasing", this.defaultEasing);
        tag.putFloat("InitPitch", this.getInitialRot().x);
        tag.putFloat("InitYaw", this.getInitialRot().y);
        tag.putFloat("FinalPitch", this.getFinalRot().x);
        tag.putFloat("FinalYaw", this.getFinalRot().y);

        ListTag rotList = new ListTag();
        for (Vec2 rot : this.anchorRotations) {
            CompoundTag rt = new CompoundTag();
            rt.putFloat("Pitch", rot.x);
            rt.putFloat("Yaw", rot.y);
            rotList.add(rt);
        }
        tag.put("Rotations", rotList);
        tag.put("BezierPath", this.path.toNbt());

        ListTag attachmentList = new ListTag();
        for (CutsceneAttachment att : this.attachments) {
            attachmentList.add(att.toNbt());
        }
        tag.put("Attachments", attachmentList);

        return tag;
    }

    public int getAnchorPointCount() {
        return this.path.getAnchorPointCount();
    }

    public int getColorArgb() {
        return this.colorArgb;
    }

    public void setColorArgb(int argb) {
        this.colorArgb = argb;
    }

    public ArrayList<Vec2> getAnchorRotations() {
        ensureAnchorRotations();
        return this.anchorRotations;
    }

    /**
     * Returns all attachments of type EffectAttachment.
     * For backward compatibility, this replaces the old getScreenEffects().
     */
    public ArrayList<EffectAttachment> getEffectAttachments() {
        ArrayList<EffectAttachment> effects = new ArrayList<>();
        for (CutsceneAttachment att : this.attachments) {
            if (att instanceof EffectAttachment eff) effects.add(eff);
        }
        return effects;
    }

    /**
     * Returns all attachments of type CommandAttachment.
     */
    public ArrayList<CommandAttachment> getCommandAttachments() {
        ArrayList<CommandAttachment> commands = new ArrayList<>();
        for (CutsceneAttachment att : this.attachments) {
            if (att instanceof CommandAttachment cmd) commands.add(cmd);
        }
        return commands;
    }

    /**
     * Returns all attachments.
     */
    public ArrayList<CutsceneAttachment> getAttachments() {
        return this.attachments;
    }

    /**
     * Adds an attachment to the cutscene.
     */
    public void addAttachment(CutsceneAttachment att) {
        if (att == null) return;
        this.attachments.add(att);
        this.attachments.sort(Comparator.comparingDouble(a -> a.getAt()));
    }

    /**
     * Removes an attachment at the specified index (from the filtered list).
     *
     * @param index Index in the filtered list of the specific type.
     * @param type  The class type to filter by.
     */
    public void removeAttachment(int index, Class<? extends CutsceneAttachment> type) {
        ArrayList<CutsceneAttachment> filtered = new ArrayList<>();
        for (CutsceneAttachment att : this.attachments) {
            if (type.isInstance(att)) filtered.add(att);
        }
        if (index < 0 || index >= filtered.size()) return;
        this.attachments.remove(filtered.get(index));
    }

    /**
     * Removes an attachment by its direct reference.
     */
    public void removeAttachment(CutsceneAttachment att) {
        this.attachments.remove(att);
    }

    /**
     * @deprecated Use addAttachment() with EffectAttachment instead.
     */
    @Deprecated
    public void addScreenEffect(EffectAttachment ev) {
        addAttachment(ev);
    }

    /**
     * @deprecated Use removeAttachment() instead.
     */
    @Deprecated
    public void removeScreenEffect(int index) {
        ArrayList<EffectAttachment> effects = getEffectAttachments();
        if (index < 0 || index >= effects.size()) return;
        removeAttachment(effects.get(index));
    }

    public void setAnchorRotation(int anchorIndex, Vec2 rot) {
        ensureAnchorRotations();
        if (anchorRotations.isEmpty()) return;
        int idx = Math.clamp(anchorIndex, 0, anchorRotations.size() - 1);
        anchorRotations.set(idx, rot);
        if (path.isSinglePoint() && anchorRotations.size() == 1) {
            // keep trivial paths stable
            anchorRotations.set(0, rot);
        }
    }

    public void setRotationForAnchorPoint(BezierPoint point, Vec2 rot) {
        if (point == null || point.isTangent()) return;
        ArrayList<BezierPoint> anchors = this.path.getAnchorPoints();
        int idx = anchors.indexOf(point);
        if (idx >= 0) setAnchorRotation(idx, rot);
    }

    public void insertAnchorRotationAtStart(Vec2 rot) {
        ensureAnchorRotations();
        anchorRotations.add(0, rot);
        ensureAnchorRotations();
    }

    public void insertAnchorRotationAtEnd(Vec2 rot) {
        ensureAnchorRotations();
        anchorRotations.add(rot);
        ensureAnchorRotations();
    }

    public void removeAnchorRotationAtStart() {
        ensureAnchorRotations();
        if (!anchorRotations.isEmpty()) anchorRotations.removeFirst();
        ensureAnchorRotations();
    }

    public void removeAnchorRotationAtEnd() {
        ensureAnchorRotations();
        if (!anchorRotations.isEmpty()) anchorRotations.removeLast();
        ensureAnchorRotations();
    }

    private void initAnchorRotationsFromEndpoints(Vec2 init, Vec2 fin) {
        anchorRotations.clear();
        int anchors = getAnchorPointCount();
        if (anchors <= 0) return;
        if (anchors == 1) {
            anchorRotations.add(init);
            return;
        }
        for (int i = 0; i < anchors; i++) {
            float t = anchors == 1 ? 0f : (i / (float) (anchors - 1));
            float pitch = Mth.rotLerp(t, init.x, fin.x);
            float yaw = Mth.rotLerp(t, init.y, fin.y);
            anchorRotations.add(new Vec2(pitch, yaw));
        }
    }

    /**
     * Ensures {@link #anchorRotations} matches the current path anchor count. When padding, the last rotation is used.
     * When expanding from only endpoints, intermediate values are interpolated.
     */
    private void ensureAnchorRotations() {
        int anchors = getAnchorPointCount();
        if (anchors <= 0) {
            anchorRotations.clear();
            return;
        }

        if (anchorRotations.isEmpty()) {
            anchorRotations.add(new Vec2(0, 0));
        }

        // Special-case: we only have endpoints but path has more anchors -> interpolate in between.
        if (anchorRotations.size() == 2 && anchors > 2) {
            Vec2 a = anchorRotations.getFirst();
            Vec2 b = anchorRotations.getLast();
            initAnchorRotationsFromEndpoints(a, b);
            return;
        }

        if (anchorRotations.size() < anchors) {
            Vec2 last = anchorRotations.getLast();
            while (anchorRotations.size() < anchors) anchorRotations.add(last);
        } else if (anchorRotations.size() > anchors) {
            while (anchorRotations.size() > anchors) anchorRotations.removeLast();
        }

        if (anchors == 1 && anchorRotations.size() > 1) {
            Vec2 only = anchorRotations.getFirst();
            anchorRotations.clear();
            anchorRotations.add(only);
        }
    }

    private double[] getAnchorDistanceKeys() {
        int anchors = getAnchorPointCount();
        double[] keys = new double[anchors];
        if (anchors <= 0) return keys;
        keys[0] = 0.0;
        if (anchors == 1) return keys;

        int splineCount = this.path.splines.size();
        for (int i = 1; i < anchors - 1; i++) {
            double time = splineCount <= 0 ? (double) i / (double) (anchors - 1) : ((double) i / (double) splineCount);
            keys[i] = this.path.getNormalizedDistanceAtTime(time);
        }
        keys[anchors - 1] = 1.0;
        return keys;
    }

    public int getDefaultHoldEnd() {
        return defaultHoldEnd;
    }

    public void setDefaultHoldEnd(int defaultHoldEnd) {
        this.defaultHoldEnd = defaultHoldEnd;
    }

    public int getDefaultHoldStart() {
        return defaultHoldStart;
    }

    public void setDefaultHoldStart(int defaultHoldStart) {
        this.defaultHoldStart = defaultHoldStart;
    }

    public int getDefaultLength() {
        return defaultLength;
    }

    public void setDefaultLength(int defaultLength) {
        this.defaultLength = defaultLength;
    }

    public String getDefaultEasing() {
        return defaultEasing;
    }

    public void setDefaultEasing(String defaultEasing) {
        this.defaultEasing = defaultEasing;
    }

    public BezierPath getPath() {
        return path;
    }

}
