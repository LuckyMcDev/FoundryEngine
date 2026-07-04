package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.command.FoundryCommandsClient;
import de.luckymcdev.foundryengine.client.debug.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.client.debug.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.client.editor.panel.NodeTestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.TestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.TextEditorTestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.cutscenes.CutscenePanel;
import de.luckymcdev.foundryengine.client.editor.panel.cutscenes.CutsceneTimelinePanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.AreaPanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.DialogueEditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.MainEditor;
import de.luckymcdev.foundryengine.client.editor.panel.editor.RecipeEditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.explorer.FileExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.panel.explorer.ResourceExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.CataloguePanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.ConsolePanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.DevToolsPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.EffectPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.MinecraftToolsPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.StopwatchPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.WaypointPanel;
import de.luckymcdev.foundryengine.client.editor.panel.view.InfoPanel;
import de.luckymcdev.foundryengine.client.editor.panel.view.ThemeSelectorPanel;
import de.luckymcdev.foundryengine.client.event.registry.RegistryEventClient;
import de.luckymcdev.foundryengine.client.ext.ModPathBroadcaster;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.render.EngineSceneDepth;
import de.luckymcdev.foundryengine.client.waypoint.ClientWaypointManager;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.BundleHashPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.WaypointPacket;
import de.luckymcdev.foundryengine.common.util.FolderHash;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterDebugRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Client-side entrypoint for FoundryEngine. Registers client event listeners, panels, and key bindings.
 */
@Mod(value = Common.MODID, dist = Dist.CLIENT)
public class FoundryEngineModClient {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IEventBus BUS = NeoForge.EVENT_BUS;

	public FoundryEngineModClient(IEventBus modBus, ModContainer modContainer) {
		modBus.addListener(this::onClientSetup);
		modBus.addListener(this::addClientReloadListener);
		modBus.addListener(this::onRegisterKeyMapping);
		modBus.addListener(this::onRegisterDebugEntry);
		modBus.addListener(this::onRegisterDebugRenderers);
		modBus.addListener(this::onRegisterGuiLayers);
		modBus.addListener(this::onRegisterParticleProviders);
		BUS.addListener(this::onClientTickPost);
		BUS.addListener(this::onRenderLevel);
		BUS.addListener(this::onAfterOpaqueFeatures);
		BUS.addListener(this::onRegisterCommands);
		BUS.addListener(this::onLoggingIn);
		BUS.addListener(this::onClientTickPre);
		BUS.addListener(this::onRenderFramePost);

		Config.registerClient(modContainer);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		LOGGER.debug("FoundryEngineModClient setup called");
		ModPathBroadcaster.broadcast();
		Client.getEditorManager().register(
			TestPanel.INSTANCE,
			TextEditorTestPanel.INSTANCE,
			FileExplorerPanel.INSTANCE,
			ResourceExplorerPanel.INSTANCE,
			ConsolePanel.INSTANCE,
			MainEditor.INSTANCE,
			MinecraftToolsPanel.INSTANCE,
			DevToolsPanel.INSTANCE,
			StopwatchPanel.INSTANCE,
			InfoPanel.INSTANCE,
			CataloguePanel.INSTANCE,
			ThemeSelectorPanel.INSTANCE,
			EffectPanel.INSTANCE,
			AreaPanel.INSTANCE,
			RecipeEditorPanel.INSTANCE,
			CutscenePanel.INSTANCE,
			CutsceneTimelinePanel.INSTANCE,
			WaypointPanel.INSTANCE,
			NodeTestPanel.INSTANCE,
			DialogueEditorPanel.INSTANCE
		);
		event.enqueueWork(() -> Common.getBundleManager().loadClientScripts());
	}

	private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(Common.id("icon_exporter"), Client.getIconExporterLayer());
	}

	private void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
		var collector = Common.getRegistryCollector();
		if (collector != null) {
			RegistryEventClient.registerParticleProviders(event, collector);
		}
	}

	private void onRenderFramePost(RenderFrameEvent.Post event) {
		Client.getIconExporterLayer().onPostRender();
	}


	private void addClientReloadListener(AddClientReloadListenersEvent event) {
		event.addListener(Common.id("imgui_handler"), Client.getImGuiManager());
		event.addListener(Common.id("obj_models"), createReloadListener(() -> Client.getObjModelManager().loadModels()));
		event.addListener(Common.id("post_effects"), createReloadListener(() -> Client.getPostEffectManager().getRegistry().invalidatePipelineCaches()));
		event.addListener(Common.id("item_icon_cache"), createReloadListener(ImGraphicsExtractor::clearItemIconCache));
	}

	private PreparableReloadListener createReloadListener(Runnable runnable) {
		return (sharedState, backgroundExecutor, barrier, gameExecutor) ->
			CompletableFuture
				.<Void>supplyAsync(() -> null, backgroundExecutor)
				.thenCompose(barrier::wait)
				.thenAcceptAsync(v ->
						runnable.run(),
					gameExecutor
				);
	}

	private void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
		event.registerCategory(Client.EDITOR_CATEGORY);
		event.register(Client.EDITOR_KEY);
		event.register(Client.MENU_BAR_KEY);
		event.register(ClientWaypointManager.PRIMARY_WAYPOINT_KEY);
		event.register(ClientWaypointManager.REMOVE_WAYPOINT_KEY);
	}

	private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
		event.register(Common.id("bundles_info"), new BundleDebugEntry(Common.getBundleManager()));
		event.register(Common.id("gamestages_info"), new GameStagesDebugEntry());
	}

	private void onRegisterDebugRenderers(RegisterDebugRenderersEvent event) {
	}

	private void onRegisterCommands(RegisterClientCommandsEvent event) {
		FoundryCommandsClient.registerAll(event.getDispatcher(), event.getBuildContext());
	}

	private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		try {
			String hash = FolderHash.hashFolder(Common.BUNDLES);
			ClientPacketDistributor.sendToServer(new BundleHashPacket(hash));
		} catch (Exception e) {
			LOGGER.error("Failed to hash bundles folder", e);
		}
	}

	private void onAfterOpaqueFeatures(RenderLevelStageEvent.AfterOpaqueFeatures event) {
		EngineSceneDepth.update();
	}

	private void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
		var camState = event.getLevelRenderState().cameraRenderState;
		Client.updateMain(camState.viewRotationMatrix, camState.projectionMatrix);

		Client.getCutsceneManager().renderTick();
		Client.getEditorController().renderFeatures();
		Client.getWaypointRenderer().renderWaypoints(event);
		Client.getAreaRenderer().renderAreaModules(event);
	}

	private void onClientTickPre(ClientTickEvent.Pre event) {
		Client.getSkyboxManager().tick(event);
	}

	private void onClientTickPost(ClientTickEvent.Post event) {
		Client.getEditorManager().handleTick();
		Client.getCutsceneManager().clientTick();
		Client.getEditorController().clientTick();
		handleWaypointKeys();

		if (Minecraft.getInstance().level instanceof ClientLevel clientLevel) {
			Common.getGameManager().tickClient(Minecraft.getInstance(), clientLevel);
		}


	}

	private void handleWaypointKeys() {
		Vec3i targetedCoords = Client.getBlockHitOrNull();

		while (ClientWaypointManager.PRIMARY_WAYPOINT_KEY.consumeClick()) {
			if (targetedCoords != null) {
				ClientPacketDistributor.sendToServer(WaypointPacket.add(
					targetedCoords.getX(), targetedCoords.getY(), targetedCoords.getZ(),
					"Info", "I", Color.TURQUOISE
				));
			}
		}

		while (ClientWaypointManager.REMOVE_WAYPOINT_KEY.consumeClick()) {
			if (targetedCoords != null) {
				ClientPacketDistributor.sendToServer(WaypointPacket.remove(
					targetedCoords.getX(), targetedCoords.getY(), targetedCoords.getZ()
				));
			}
		}
	}
}