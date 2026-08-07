package de.luckymcdev.foundryengine.client.area.module;

import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.module.AreaModule;
import net.minecraft.client.multiplayer.ClientLevel;
//? if 26.1 {
import net.minecraft.client.renderer.MultiBufferSource;
 //?}
//? if 26.2 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
*///?}
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface AreaRenderModule extends AreaModule {
	//? if 26.1 {
	void render(ClientLevel level, Area area, PoseStack poseStack, MultiBufferSource buffer, float partialTick);
	 //?} else {
	/*void render(ClientLevel level, Area area, PoseStack poseStack, SubmitNodeCollector collector, float partialTick);
	*///?}
}
