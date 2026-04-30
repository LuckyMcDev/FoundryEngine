package de.luckymcdev.foundryengine.common.cutscene.model;

import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
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
    private final ArrayList<ScreenEffectEvent> screenEffects = new ArrayList<>();
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

        // Screen effects
        ListTag effectList = tag.getListOrEmpty("ScreenEffects");
        cutscene.screenEffects.clear();
        for (int i = 0; i < effectList.size(); i++) {
            cutscene.screenEffects.add(ScreenEffectEvent.fromNbt(effectList.getCompoundOrEmpty(i)));
        }
        cutscene.screenEffects.sort(Comparator.comparingDouble(e -> e.at));

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

        ListTag effectList = new ListTag();
        for (ScreenEffectEvent ev : this.screenEffects) {
            effectList.add(ev.toNbt());
        }
        tag.put("ScreenEffects", effectList);

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

    public ArrayList<ScreenEffectEvent> getScreenEffects() {
        return this.screenEffects;
    }

    public void addScreenEffect(ScreenEffectEvent ev) {
        if (ev == null) return;
        this.screenEffects.add(ev);
        this.screenEffects.sort(Comparator.comparingDouble(e -> e.at));
    }

    public void removeScreenEffect(int index) {
        if (index < 0 || index >= this.screenEffects.size()) return;
        this.screenEffects.remove(index);
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

    public static class ScreenEffectEvent {
        /**
         * Normalized time (0..1) along the cutscene playback segment (excluding holds).
         */
        public float at;
        public String name;
        public int introTicks;
        public int holdTicks;
        public int outroTicks;
        public String lerpType;
        public String command;

        public ScreenEffectEvent(float at, String name, int introTicks, int holdTicks, int outroTicks, String lerpType, String command) {
            this.at = net.minecraft.util.Mth.clamp(at, 0f, 1f);
            this.name = name == null ? "" : name;
            this.introTicks = Math.max(0, introTicks);
            this.holdTicks = Math.max(0, holdTicks);
            this.outroTicks = Math.max(0, outroTicks);
            this.lerpType = (lerpType == null || lerpType.isBlank()) ? LerpType.LINEAR.name() : lerpType;
            this.command = command == null ? "" : command;
        }

        public static ScreenEffectEvent fromNbt(CompoundTag tag) {
            float at = tag.getFloatOr("At", 0f);
            String name = tag.getStringOr("Name", "");
            int intro = tag.getIntOr("Intro", 0);
            int hold = tag.getIntOr("Hold", 0);
            int outro = tag.getIntOr("Outro", 0);
            String lerp = tag.getStringOr("LerpType", LerpType.LINEAR.name());
            String cmd = tag.getStringOr("Command", "");
            return new ScreenEffectEvent(at, name, intro, hold, outro, lerp, cmd);
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("At", this.at);
            tag.putString("Name", this.name);
            tag.putInt("Intro", this.introTicks);
            tag.putInt("Hold", this.holdTicks);
            tag.putInt("Outro", this.outroTicks);
            tag.putString("LerpType", this.lerpType);
            tag.putString("Command", this.command);
            return tag;
        }
    }
}
