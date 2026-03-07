package io.github.luckymcdev.foundryengine.client.opengl.uniform;

import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.opengl.program.ShaderProgram;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * A central registry of common Engine Uniforms.
 * These use {@link Supplier} to ensure values are
 * always synchronized with the current engine state when polled by a shader.
 */
@ApiStatus.Experimental
public class Uniforms {
    public static final Uniform<?> CAMERA_POS = new Uniform<>("cameraPos",
            () -> new Vector3f(Client.getMainCamera().position().toVector3f()));
    public static final Uniform<?> LOOK_VECTOR = new Uniform<>("lookVector",
            () -> Client.getMainCamera().forwardVector());
    public static final Uniform<?> UP_VECTOR = new Uniform<>("upVector",
            () -> Client.getMainCamera().upVector());
    public static final Uniform<?> LEFT_VECTOR = new Uniform<>("leftVector",
            () -> Client.getMainCamera().leftVector());
    public static final Uniform<?> INV_VIEW_MAT = new Uniform<>("invViewMat",
            Client.MODEL_VIEW::invert);
    public static final Uniform<?> INV_PROJ_MAT = new Uniform<>("invProjMat",
            Client.PROJECTION::invert);
    public static final Uniform<?> VIEW_MAT = new Uniform<>("viewMat",
            () -> Client.MODEL_VIEW);
    public static final Uniform<?> PROJ_MAT = new Uniform<>("projMat",
            () -> Client.PROJECTION);
    public static final Uniform<?> NEAR_PLANE_DISTANCE = new Uniform<>("nearPlaneDistance",
            () -> GameRenderer.PROJECTION_Z_NEAR);
    public static final Uniform<?> FAR_PLANE_DISTANCE = new Uniform<>("farPlaneDistance",
            () -> Client.getGameRenderer().getDepthFar());
    public static final Uniform<?> FOV = new Uniform<>("fov",
            () -> Math.toRadians(Client.getMinecraft().options.fov().get()));
    public static final Uniform<?> ASPECT_RATIO = new Uniform<>("aspectRatio",
            () -> (float) Client.getWindow().getWidth() / (float) Client.getWindow().getHeight());

    /**
     * Gets all Uniforms as a Collection for setting using {@link ShaderProgram#setUniforms(Iterable)}
     *
     * @return the Uniform Collection
     */
    public static Collection<Uniform<?>> getCollection() {
        return List.of(
                CAMERA_POS, LOOK_VECTOR, UP_VECTOR, LEFT_VECTOR,
                INV_VIEW_MAT, INV_PROJ_MAT, NEAR_PLANE_DISTANCE,
                FAR_PLANE_DISTANCE, FOV, ASPECT_RATIO
        );
    }
}
