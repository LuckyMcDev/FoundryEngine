package de.luckymcdev.foundryengine.client.editor;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class HandlePicker {
    private static final double HOVER_DEGREES = 1.0;

    private HandlePicker() {
    }

    public static boolean isHovered(Vec3 target, LivingEntity entity) {
        Vec3 eye = entity.getEyePosition();
        Vec3 toTarget = target.subtract(eye);

        double x = toTarget.x;
        double y = toTarget.y;
        double z = toTarget.z;

        double rotation = Math.toDegrees(Math.atan2(x, z)) * -1;
        double playerRot = Mth.wrapDegrees(entity.getYRot());
        double playerRot2 = Mth.wrapDegrees(entity.getXRot());
        double hypot = Math.sqrt(x * x + z * z);
        double rotation2 = Math.toDegrees(Math.atan2(y, hypot)) * -1;

        return Math.abs(playerRot - rotation) < HOVER_DEGREES && Math.abs(playerRot2 - rotation2) < HOVER_DEGREES;
    }

    public static boolean isHovered(Vec3 target, Vec3 eye, Vec3 look) {
        Vec3 toTarget = target.subtract(eye);
        double t = toTarget.dot(look);
        if (t < 0) return false;

        double x = toTarget.x;
        double y = toTarget.y;
        double z = toTarget.z;

        double rotation = Math.toDegrees(Math.atan2(x, z)) * -1;
        double playerRot = Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(look.x, look.z))) * -1;
        double playerRot2 = Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)))) * -1;
        double hypot = Math.sqrt(x * x + z * z);
        double rotation2 = Math.toDegrees(Math.atan2(y, hypot)) * -1;

        return Math.abs(playerRot - rotation) < HOVER_DEGREES && Math.abs(playerRot2 - rotation2) < HOVER_DEGREES;
    }

    public static double getHoverDegrees() {
        return HOVER_DEGREES;
    }
}