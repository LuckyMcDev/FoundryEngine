package io.github.luckymcdev.client;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public interface ClientMatrices {
    Matrix4f MODEL_VIEW = new Matrix4f();
    Matrix4f PROJECTION = new Matrix4f();
    Matrix4f WORLD = new Matrix4f();
    Matrix4f INVERSE_WORLD = new Matrix4f();
    Matrix4f PERSPECTIVE = new Matrix4f();
    Matrix4f FRUSTUM = new Matrix4f();

    static void updateMain(Matrix4fc modelView, Matrix4fc projection) {
        MODEL_VIEW.set(modelView);
        PROJECTION.set(projection);
        WORLD.set(projection).mul(modelView);
        INVERSE_WORLD.set(WORLD).invert();
    }
}
