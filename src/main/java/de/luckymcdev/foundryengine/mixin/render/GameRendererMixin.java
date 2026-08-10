package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.post.RenderPhase;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render ImGui and custom Post Chain stuff
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow
	@Final
	public CrossFrameResourcePool resourcePool;

	/**
	 * Injects before post-world depth clear to capture snapshots and apply POST_WORLD effects.
	 */
	@Inject(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V",
			shift = At.Shift.BEFORE
		)
	)
	private void engine$onPostWorldRender(DeltaTracker ticker, CallbackInfo ci) {
		if (Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.PRE_GUI)
			|| Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.POST_RENDER)) {
			Client.getPostEffectManager().getRegistry().captureWorldDepthSnapshot(Client.getMainRenderTarget());
		}
		Client.getPostEffectManager().getRegistry().applyAll(RenderPhase.POST_WORLD, ticker.getGameTimeDeltaPartialTick(true), resourcePool);
	}

	/**
	 * Injects before pre-GUI depth clear to capture snapshots and apply PRE_GUI effects.
	 */
	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V",
			shift = At.Shift.BEFORE
		)
	)
	private void engine$onPreScreenDepthClear(DeltaTracker ticker, boolean renderLevel, CallbackInfo ci) {
		if (!renderLevel) {
			return;
		}

		if (Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.POST_RENDER)) {
			Client.getPostEffectManager().getRegistry().capturePostRenderDepthSnapshot(Client.getMainRenderTarget());
		}
		Client.getPostEffectManager().getRegistry().applyAll(RenderPhase.PRE_GUI, ticker.getGameTimeDeltaPartialTick(true), resourcePool);
	}

	/**
	 * Injects at render TAIL to restore depth and apply POST_RENDER effects.
	 */
	@Inject(method = "render", at = @At("TAIL"))
	private void engine$onPostRender(DeltaTracker ticker, boolean renderLevel, CallbackInfo ci) {
		if (Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.POST_RENDER)) {
			Client.getPostEffectManager().getRegistry().restorePostRenderDepthSnapshotInto(Client.getMainRenderTarget());
		}
		Client.getPostEffectManager().getRegistry().applyAll(RenderPhase.POST_RENDER, ticker.getGameTimeDeltaPartialTick(true), resourcePool);
	}
}