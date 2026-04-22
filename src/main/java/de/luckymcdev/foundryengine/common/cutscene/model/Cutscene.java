package de.luckymcdev.foundryengine.common.cutscene.model;

import de.luckymcdev.foundryengine.common.easing.BezierPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Cutscene {
    public final BezierPath path;
    private final String name;
    private Vec2 initialRot;
    private Vec2 finalRot;

    public Cutscene(String name, Vec2 initialRot, Vec2 finalRot, BezierPath path) {
        this.name = name;
        this.initialRot = initialRot;
        this.finalRot = finalRot;
        this.path = path;
    }

    public static Cutscene fromNbt(CompoundTag tag) {
        String name = tag.getStringOr("Name", "cutscene");
        Vec2 initRot = new Vec2(tag.getFloatOr("InitPitch", 0f), tag.getFloatOr("InitYaw", 0f));
        Vec2 finalRot = new Vec2(tag.getFloatOr("FinalPitch", 0f), tag.getFloatOr("FinalYaw", 0f));
        BezierPath path = new BezierPath(tag.getListOrEmpty("BezierPath"));
        return new Cutscene(name, initRot, finalRot, path);
    }

    public String getName() {
        return name;
    }

    public Vec2 getInitialRot() {
        return initialRot;
    }

    public Vec2 getFinalRot() {
        return finalRot;
    }

    public void setFinalRot(Vec2 rot) {
        this.finalRot = rot;
        if (path.isSinglePoint()) {
            this.initialRot = rot;
        }
    }

    public void setInitRot(Vec2 rot) {
        this.initialRot = rot;
        if (path.isSinglePoint()) {
            this.finalRot = rot;
        }
    }

    public Vec3 getPosAt(float t) {
        return path.lerpSpeedWeighted(t);
    }

    public Vec2 getRotAt(float t) {
        float pitch = Mth.rotLerp(t, initialRot.x, finalRot.x);
        float yaw = Mth.rotLerp(t, initialRot.y, finalRot.y);
        return new Vec2(pitch, yaw);
    }

    public Cutscene originAtPlayer(Player player) {
        BezierPath newPath = this.path.withPlayerOrigin(player);
        return new Cutscene(name, new Vec2(player.getXRot(), player.getYRot()), finalRot, newPath);
    }

    public Cutscene endAtPlayer(Player player) {
        BezierPath newPath = this.path.withPlayerEnd(player);
        return new Cutscene(name, initialRot, new Vec2(player.getXRot(), player.getYRot()), newPath);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", this.name);
        tag.putFloat("InitPitch", this.initialRot.x);
        tag.putFloat("InitYaw", this.initialRot.y);
        tag.putFloat("FinalPitch", this.finalRot.x);
        tag.putFloat("FinalYaw", this.finalRot.y);
        tag.put("BezierPath", this.path.toNbt());
        return tag;
    }
}
