package io.github.luckymcdev.foundryengine.client.debug.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

/**
 * An inline implementation for a DebugRenderer.
 * For Text see {@link io.github.luckymcdev.foundryengine.client.debug.screen.SimpleDebugScreenEntry}
 */
public class SimpleDebugScreenRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final SimpleDebugScreenRendererContext<Minecraft, Vector3d, DebugValueAccess, Frustum, Float> context;
    private final Vector3d camPos;
    private final Minecraft minecraft;

    public SimpleDebugScreenRenderer(Minecraft minecraft, SimpleDebugScreenRendererContext<Minecraft, Vector3d, DebugValueAccess, Frustum, Float> renderer) {
        this.minecraft = minecraft;
        this.context = renderer;
        this.camPos = new Vector3d();
    }

    @Override
    public void emitGizmos(double camX, double camY, double camZ, @NonNull DebugValueAccess debugValueAccess, @NonNull Frustum frustum, float partialTick) {
        camPos.x = camX;
        camPos.y = camY;
        camPos.z = camZ;
        context.accept(minecraft, camPos, debugValueAccess, frustum, partialTick);
    }

    @FunctionalInterface
    public interface SimpleDebugScreenRendererContext<Minecraft, Vector3d, DebugValueAccess, Frustum, Float> {
        void accept(
                net.minecraft.client.Minecraft minecraft,
                org.joml.Vector3d camPos,
                net.minecraft.util.debug.DebugValueAccess debugValueAccess,
                Frustum frustum,
                java.lang.Float partialTicks);
    }
}
