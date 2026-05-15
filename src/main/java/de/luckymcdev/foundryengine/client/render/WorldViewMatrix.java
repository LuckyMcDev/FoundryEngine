package de.luckymcdev.foundryengine.client.render;

import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * Builds a world-space Model-View-Projection (MVP) matrix from the camera state.
 */
public class WorldViewMatrix {
    private final Matrix4fc projection;
    private final Matrix4fc viewRotation;
    private final Vec3 cameraPos;
    private boolean billboarded = false;
    private float tx = 0, ty = 0, tz = 0;
    private float rx = 0, ry = 0, rz = 0;
    private float sx = 1, sy = 1, sz = 1;

    private WorldViewMatrix(Matrix4fc projection, Matrix4fc viewRotation, Vec3 cameraPos) {
        this.projection = projection;
        this.viewRotation = viewRotation;
        this.cameraPos = cameraPos;
    }

    /**
     * Creates a builder using the current camera and projection state.
     */
    public static WorldViewMatrix from(RenderLevelStageEvent.AfterLevel event) {
        return new WorldViewMatrix(
                event.getLevelRenderState().cameraRenderState.projectionMatrix,
                event.getModelViewMatrix(),
                event.getLevelRenderState().cameraRenderState.pos
        );
    }

    public WorldViewMatrix at(float x, float y, float z) {
        this.tx = x;
        this.ty = y;
        this.tz = z;
        return this;
    }

    public WorldViewMatrix at(Vec3 pos) {
        return at((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public WorldViewMatrix rotateX(float radians) {
        this.rx = radians;
        return this;
    }

    public WorldViewMatrix rotateY(float radians) {
        this.ry = radians;
        return this;
    }

    public WorldViewMatrix rotateZ(float radians) {
        this.rz = radians;
        return this;
    }

    public WorldViewMatrix scale(float s) {
        this.sx = s;
        this.sy = s;
        this.sz = s;
        return this;
    }

    public WorldViewMatrix scale(float x, float y, float z) {
        this.sx = x;
        this.sy = y;
        this.sz = z;
        return this;
    }

    public WorldViewMatrix billboard() {
        this.billboarded = true;
        return this;
    }

    /**
     * Builds the MV matrix instead of MVP.
     */
    public Matrix4f buildModelView() {
        if (billboarded) {
            Vector3f camSpacePos = new Vector3f(
                    (float) (tx - cameraPos.x),
                    (float) (ty - cameraPos.y),
                    (float) (tz - cameraPos.z)
            );
            viewRotation.transformPosition(camSpacePos);
            return new Matrix4f()
                    .translate(camSpacePos)
                    .rotateXYZ(rx, ry, rz)
                    .scale(sx, sy, sz);
        }

        return new Matrix4f(viewRotation)
                .translate((float) (tx - cameraPos.x), (float) (ty - cameraPos.y), (float) (tz - cameraPos.z))
                .rotateXYZ(rx, ry, rz)
                .scale(sx, sy, sz);
    }


    /**
     * Builds the final MVP matrix.
     * Uses relative translation (ModelPos - CameraPos) to prevent jitter.
     */
    public Matrix4f build() {
        return new Matrix4f(projection)
                .mul(viewRotation)
                .translate((float) (tx - cameraPos.x), (float) (ty - cameraPos.y), (float) (tz - cameraPos.z))
                .rotateXYZ(rx, ry, rz)
                .scale(sx, sy, sz);
    }
}