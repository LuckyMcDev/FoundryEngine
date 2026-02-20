package io.github.luckymcdev.foundryengine.client;

import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * Simple Interface for getting Client Matrices.
 * credit LatvianModder.
 */
public interface ClientMatrices {
    /**
     * The Model View Matrix
     */
    Matrix4f MODEL_VIEW = new Matrix4f();
    /** The Projection Matrix */
    Matrix4f PROJECTION = new Matrix4f();
    /** The World Matrix */
    Matrix4f WORLD = new Matrix4f();
    /** The Inverse World Matrix */
    Matrix4f INVERSE_WORLD = new Matrix4f();
    /** The Perspective Matrix */
    Matrix4f PERSPECTIVE = new Matrix4f();
    /** The Frustum Matrix */
    Matrix4f FRUSTUM = new Matrix4f();

    /**
     * Updates the {@link #MODEL_VIEW}, {@link #PROJECTION}, {@link #WORLD} and {@link #INVERSE_WORLD} Matrices.
     * <br>
     * Called from {@link FrameGraphSetupEvent}
     *
     * @param modelView  updated Model View Matrix.
     * @param projection updated Projection Matrix.
     */
    static void updateMain(Matrix4fc modelView, Matrix4fc projection) {
        MODEL_VIEW.set(modelView);
        PROJECTION.set(projection);
        WORLD.set(projection).mul(modelView);
        INVERSE_WORLD.set(WORLD).invert();
    }
}
