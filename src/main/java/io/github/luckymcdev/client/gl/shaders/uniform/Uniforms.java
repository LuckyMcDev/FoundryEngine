package io.github.luckymcdev.client.gl.shaders.uniform;

import com.google.common.collect.Lists;
import io.github.luckymcdev.client.ClientMatrices;
import io.github.luckymcdev.common.Instances;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Vector3f;

import java.util.Collection;

public class Uniforms {
    public static final Uniform<?> CAMERA_POS = new Uniform<>("cameraPos", new Vector3f(Instances.getMainCamera().getPosition().toVector3f()));
    public static final Uniform<?> LOOK_VECTOR = new Uniform<>("lookVector", Instances.getMainCamera().getLookVector());
    public static final Uniform<?> UP_VECTOR = new Uniform<>("upVector", Instances.getMainCamera().getUpVector());
    public static final Uniform<?> LEFT_VECTOR = new Uniform<>("leftVector", Instances.getMainCamera().getLeftVector());
    public static final Uniform<?> INV_VIEW_MAT = new Uniform<>("invViewMat", ClientMatrices.MODEL_VIEW.invert());
    public static final Uniform<?> INV_PROJ_MAT = new Uniform<>("invProjMat", ClientMatrices.PROJECTION.invert());
    public static final Uniform<?> NEAR_PLANE_DISTANCE = new Uniform<>("nearPlaneDistance", GameRenderer.PROJECTION_Z_NEAR);
    public static final Uniform<?> FAR_PLANE_DISTANCE = new Uniform<>("farPlaneDistance", Instances.getGameRenderer().getDepthFar());
    public static final Uniform<?> FOV = new Uniform<>("fov", Math.toRadians(Instances.getMinecraft().options.fov().get()));
    public static final Uniform<?> ASPECT_RATIO = new Uniform<>("aspectRatio", Instances.getWindow().getWidth() / Instances.getWindow().getHeight());

    public static Collection<Uniform<?>> getCollection() {
        return Lists.newArrayList(
                CAMERA_POS,
                LOOK_VECTOR,
                UP_VECTOR,
                LEFT_VECTOR,
                INV_VIEW_MAT,
                INV_PROJ_MAT,
                NEAR_PLANE_DISTANCE,
                FAR_PLANE_DISTANCE,
                FOV,
                ASPECT_RATIO
        );
    }
}
