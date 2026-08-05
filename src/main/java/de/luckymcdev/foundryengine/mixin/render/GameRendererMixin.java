package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.post.RenderPhase;
import de.luckymcdev.foundryengine.interfaces.render.EngineGameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render ImGui and custom Post Chain stuff
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin implements EngineGameRenderer {

	@Unique
	private static final Logger engine$LOGGER = LogUtils.getLogger();

	@Shadow
	@Final
	public CrossFrameResourcePool resourcePool;

	/**
	 * Injects at render HEAD as a hook for future use.
	 */
	@Override
	@Inject(method = "render", at = @At("HEAD"))
	public void engine$renderHead(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
	}

	/**
	 * Injects at render RETURN to render ImGui and editor overlays.
	 */
	@Override
	@Inject(method = "render", at = @At("RETURN"))
	public void engine$renderReturn(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		var imguiManager = Client.getImGuiManager();

		if (imguiManager.isEnabled()) {
			var mainMenu = Client.getMainMenu();
			var editorManager = Client.getEditorManager();

			try {
				imguiManager.begin();
				mainMenu.handleShortcuts();
				if (imguiManager.isMenuBarVisible()) {
					mainMenu.render();
				}
				editorManager.handleRender();
			} catch (Exception e) {
				engine$LOGGER.error("ImGui editor frame failed, restoring rendering state", e);
			} finally {
				try {
					imguiManager.end();
				} catch (Exception e) {
					engine$LOGGER.error("ImGui frame teardown failed", e);
				}
			}
		}
	}

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
		Minecraft mc = Minecraft.getInstance();
		if (Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.PRE_GUI)
			|| Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.POST_RENDER)) {
			Client.getPostEffectManager().getRegistry().captureWorldDepthSnapshot(mc.getMainRenderTarget());
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

		Minecraft mc = Minecraft.getInstance();
		if (Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.POST_RENDER)) {
			Client.getPostEffectManager().getRegistry().capturePostRenderDepthSnapshot(mc.getMainRenderTarget());
		}
		Client.getPostEffectManager().getRegistry().applyAll(RenderPhase.PRE_GUI, ticker.getGameTimeDeltaPartialTick(true), resourcePool);
	}

	/**
	 * Injects at render TAIL to restore depth and apply POST_RENDER effects.
	 */
	@Inject(method = "render", at = @At("TAIL"))
	private void engine$onPostRender(DeltaTracker ticker, boolean renderLevel, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (Client.getPostEffectManager().getRegistry().hasEnabledEffectInPhase(RenderPhase.POST_RENDER)) {
			Client.getPostEffectManager().getRegistry().restorePostRenderDepthSnapshotInto(mc.getMainRenderTarget());
		}
		Client.getPostEffectManager().getRegistry().applyAll(RenderPhase.POST_RENDER, ticker.getGameTimeDeltaPartialTick(true), resourcePool);
	}
}