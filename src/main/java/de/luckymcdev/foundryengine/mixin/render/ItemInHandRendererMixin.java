package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.config.ClientConfig;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render offhand arm when empty and handle item-in-hand rendering overrides.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	@Invoker("renderPlayerArm")
	abstract void invokeRenderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm);

	/**
	 * Injects at the tail of renderArmWithItem to render the offhand arm when empty.
	 */
	//? if 26.1 {
	/*@Inject(method = "renderArmWithItem", at = @At("TAIL"))
	 *///?} else {
	@Inject(method = "submitArmWithItem", at = @At("TAIL"))
		//?}
	private void renderOffhandArmWhenEmpty(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight,
	                                       PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
	                                       int lightCoords, CallbackInfo ci) {


		if (!ClientConfig.RENDER_OFFHAND.getAsBoolean()) {
			return;
		}

		if (hand == InteractionHand.OFF_HAND && itemStack.isEmpty()) {
			HumanoidArm offArm = player.getMainArm().getOpposite();
			invokeRenderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack, offArm);
		}
	}
}
