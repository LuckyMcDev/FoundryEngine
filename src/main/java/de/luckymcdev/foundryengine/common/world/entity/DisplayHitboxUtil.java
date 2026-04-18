package de.luckymcdev.foundryengine.common.world.entity;

import com.mojang.math.Transformation;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

/**
 * Computes axis-aligned hitbox dimensions for Display entities after an
 * arbitrary {@link Transformation} has been applied.
 *
 * <p>Transforms all 8 corners of the source geometry through the matrix and
 * derives the enclosing AABB, which is fed into {@code EntityDimensions} so
 * that {@code makeBoundingBox} produces the correct interaction hitbox.
 */
public final class DisplayHitboxUtil {

    /**
     * Block display: 1×1×1 cube with corners in [0,1]³.
     */
    private static final float[][] BLOCK_CORNERS = {
            {0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {1, 1, 0},
            {0, 0, 1}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1},
    };
    /**
     * Item / text display: flat quad centred on the origin in [−0.5, 0.5]²,
     * with a tiny ±0.05 depth so the box stays non-degenerate after rotation.
     */
    private static final float[][] FLAT_CORNERS = {
            {-0.5f, -0.5f, -0.05f}, {0.5f, -0.5f, -0.05f},
            {-0.5f, 0.5f, -0.05f}, {0.5f, 0.5f, -0.05f},
            {-0.5f, -0.5f, 0.05f}, {0.5f, -0.5f, 0.05f},
            {-0.5f, 0.5f, 0.05f}, {0.5f, 0.5f, 0.05f},
    };

    private DisplayHitboxUtil() {
    }

    public static HitboxSize forBlock(Transformation t) {
        return compute(t, BLOCK_CORNERS);
    }

    public static HitboxSize forItem(Transformation t) {
        return compute(t, FLAT_CORNERS);
    }

    public static HitboxSize forText(Transformation t) {
        return compute(t, FLAT_CORNERS);
    }

    private static HitboxSize compute(Transformation t, float[][] corners) {
        Matrix4fc mat = t.getMatrix();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

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

        float extentX = Math.max(Math.abs(minX), Math.abs(maxX));
        float extentZ = Math.max(Math.abs(minZ), Math.abs(maxZ));
        float width = Math.max(extentX, extentZ) * 2.0f;
        float height = maxY - minY;

        return new HitboxSize(Math.max(width, 0.1f), Math.max(height, 0.1f));
    }

    public record HitboxSize(float width, float height) {
    }
}