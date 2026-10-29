package de.luckymcdev.foundryengine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.command.client.FoundryCommandsClient;
import de.luckymcdev.foundryengine.command.client.suggest.nbt.NbtSuggestions;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.event.client.registry.RegistryEventClient;
import de.luckymcdev.foundryengine.ext.client.ModPathRecorder;
import de.luckymcdev.foundryengine.gizmo.client.GizmoBuffer;
import de.luckymcdev.foundryengine.gizmo.client.GizmoRenderer;
import de.luckymcdev.foundryengine.render.client.EngineSceneDepth;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Core client entrypoint — registry renderers, shared client commands, tooltips,
 * and the central render/tick coordinator.
 */
@Mod(value = Common.MODID, dist = Dist.CLIENT)
public class EngineClientMod {
	public static final Identifier LOGO_TRANSPARENT = Common.id("textures/logo_transparent.png");
	public static final Identifier LOGO_BLACK = Common.id("textures/logo_black.png");
	public static final Identifier LOGO_WHITE = Common.id("textures/logo_white.png");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IEventBus BUS = NeoForge.EVENT_BUS;

	public EngineClientMod(IEventBus modBus, ModContainer modContainer) {
		modBus.addListener(this::onClientSetup);
		modBus.addListener(this::addClientReloadListener);
		modBus.addListener(this::onRegisterParticleProviders);
		modBus.addListener(this::onRegisterMenuScreens);
		BUS.addListener(this::onRegisterCommands);
		BUS.addListener(this::onRenderLevel);
		BUS.addListener(this::onAfterOpaqueFeatures);
		BUS.addListener(this::onClientTickPre);
		BUS.addListener(this::onItemTooltip);
		BUS.addListener(this::onRightClickItem);

		Config.registerClient(modContainer);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		NbtSuggestions.init();
		ModPathRecorder.record();
		var collector = Common.getRegistryCollector();
		if (collector != null) {
			RegistryEventClient.registerRenderers(collector);
		}
	}

	private void addClientReloadListener(AddClientReloadListenersEvent event) {
		event.addListener(Common.id("obj_models"),
			createReloadListener(() -> Client.getObjModelManager().loadModels()));
	}

	private PreparableReloadListener createReloadListener(Runnable runnable) {
		return (s, bg, barrier, game) ->
			CompletableFuture.<Void>supplyAsync(() -> null, bg)
				.thenCompose(barrier::wait)
				.thenAcceptAsync(v -> runnable.run(), game);
	}

	private void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
		var collector = Common.getRegistryCollector();
		if (collector != null) {
			RegistryEventClient.registerParticleProviders(event, collector);
		}
	}

	private void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		var collector = Common.getRegistryCollector();
		if (collector != null) {
			RegistryEventClient.registerMenuScreens(event, collector);
		}
	}

	private void onRegisterCommands(RegisterClientCommandsEvent event) {
		FoundryCommandsClient.registerAll(event.getDispatcher(), event.getBuildContext());
	}

	private void onItemTooltip(ItemTooltipEvent event) {
		Client.getItemCommandManager().handleItemTooltip(event);
		Client.getTooltipManager().handleItemTooltip(event);
	}

	private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		Client.getItemCommandManager().handleRightClick(event);
	}

	private void onAfterOpaqueFeatures(RenderLevelStageEvent.AfterOpaqueFeatures event) {
		EngineSceneDepth.update();
	}

	private void onClientTickPre(ClientTickEvent.Pre event) {
		Client.getSkyboxManager().tick(event);
	}

	private void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
		var camState = event.getLevelRenderState().cameraRenderState;
		Client.updateMain(camState.viewRotationMatrix, camState.projectionMatrix);

		GizmoBuffer.startFrame();
		Client.getCutsceneManager().renderTick();
		Client.getEditorController().renderFeatures();
		Client.getWaypointRenderer().renderWaypoints(event);
		Client.getAreaRenderer().renderAreaModules(event);

		var mc = Minecraft.getInstance();
		var poseStack = new PoseStack();
		var bufferSource = mc.renderBuffers().bufferSource();
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.mul(camState.viewRotationMatrix);
		GizmoRenderer.render(poseStack, bufferSource, camState, camState.viewRotationMatrix);
		modelViewStack.popMatrix();
	}
}