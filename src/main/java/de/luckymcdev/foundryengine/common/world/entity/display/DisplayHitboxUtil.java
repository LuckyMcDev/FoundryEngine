package de.luckymcdev.foundryengine.common.world.entity.display;

import com.mojang.math.Transformation;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

/**
 * Computes axis-aligned hitbox bounds for Display entities after an
 * arbitrary {@link Transformation} has been applied.
 */
public final class DisplayHitboxUtil {

    private static final float[][] BLOCK_CORNERS = {
            {0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {1, 1, 0},
            {0, 0, 1}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1},
    };

    private static final float[][] ITEM_CORNERS = {
            {-0.5f, -0.5f, -0.05f}, {0.5f, -0.5f, -0.05f},
            {-0.5f, 0.5f, -0.05f}, {0.5f, 0.5f, -0.05f},
            {-0.5f, -0.5f, 0.05f}, {0.5f, -0.5f, 0.05f},
            {-0.5f, 0.5f, 0.05f}, {0.5f, 0.5f, 0.05f},
    };

    private static final float[][] TEXT_CORNERS = {
            {-0.5f, 0.0f, -0.01f}, {0.5f, 0.0f, -0.01f},
            {-0.5f, 0.3f, -0.01f}, {0.5f, 0.3f, -0.01f},
            {-0.5f, 0.0f, 0.01f}, {0.5f, 0.0f, 0.01f},
            {-0.5f, 0.3f, 0.01f}, {0.5f, 0.3f, 0.01f},
    };

    private DisplayHitboxUtil() {
    }

    public static HitboxBounds forBlock(Transformation t) {
        return compute(t, BLOCK_CORNERS);
    }

    public static HitboxBounds forItem(Transformation t) {
        return compute(t, ITEM_CORNERS);
    }

    public static HitboxBounds forText(Transformation t) {
        return compute(t, TEXT_CORNERS);
    }

    private static HitboxBounds compute(Transformation t, float[][] corners) {
        Matrix4fc mat = t.getMatrix();
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        Vector4f v = new Vector4f();
        for (float[] c : corners) {
            v.set(c[0], c[1], c[2], 1.0f);
            mat.transform(v);
            if (v.x < minX) minX = v.x;
            if (v.x > maxX) maxX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.y > maxY) maxY = v.y;
            if (v.z < minZ) minZ = v.z;
            if (v.z > maxZ) maxZ = v.z;
        }

        if (maxX - minX < 0.1f) {
            float m = (minX + maxX) * 0.5f;
            minX = m - 0.05f;
            maxX = m + 0.05f;
        }
        if (maxY - minY < 0.1f) {
            float m = (minY + maxY) * 0.5f;
            minY = m - 0.05f;
            maxY = m + 0.05f;
        }
        if (maxZ - minZ < 0.1f) {
            float m = (minZ + maxZ) * 0.5f;
            minZ = m - 0.05f;
            maxZ = m + 0.05f;
        }

        return new HitboxBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public record HitboxBounds(float minX, float minY, float minZ,
                               float maxX, float maxY, float maxZ) {
        public float cullingWidth() {
            return Math.max(
                    Math.max(Math.abs(minX), Math.abs(maxX)),
                    Math.max(Math.abs(minZ), Math.abs(maxZ))
            ) * 2.0f;
        }

        public float cullingHeight() {
            return maxY - minY;
        }
    }
}