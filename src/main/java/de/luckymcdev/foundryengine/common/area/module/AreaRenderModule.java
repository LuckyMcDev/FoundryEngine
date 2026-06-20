package de.luckymcdev.foundryengine.common.area.module;

import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.common.area.Area;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface AreaRenderModule extends AreaModule {
    void render(ClientLevel level, Area area, PoseStack poseStack, MultiBufferSource buffer, float partialTick);
}
