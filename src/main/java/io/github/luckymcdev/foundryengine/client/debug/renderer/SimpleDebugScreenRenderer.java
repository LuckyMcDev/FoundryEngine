package io.github.luckymcdev.foundryengine.client.debug.renderer;

import io.github.luckymcdev.foundryengine.common.util.PentaConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public class SimpleDebugScreenRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final PentaConsumer<Minecraft, Vector3d, DebugValueAccess, Frustum, Float> renderer;
    private final Vector3d camPos;
    private final Minecraft minecraft;

    public SimpleDebugScreenRenderer(Minecraft minecraft, PentaConsumer<Minecraft, Vector3d, DebugValueAccess, Frustum, Float> renderer) {
        this.minecraft = minecraft;
        this.renderer = renderer;
        this.camPos = new Vector3d();
    }

    @Override
    public void emitGizmos(double camX, double camY, double camZ, @NonNull DebugValueAccess debugValueAccess, @NonNull Frustum frustum, float partialTick) {
        camPos.x = camX;
        camPos.y = camY;
        camPos.z = camZ;
        renderer.accept(minecraft, camPos, debugValueAccess, frustum, partialTick);
    }
}
