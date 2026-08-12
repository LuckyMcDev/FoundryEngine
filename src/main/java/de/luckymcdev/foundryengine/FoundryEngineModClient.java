package de.luckymcdev.foundryengine;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.command.FoundryCommandsClient;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestions;
import de.luckymcdev.foundryengine.client.debug.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.client.debug.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.client.editor.panel.NodeTestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.cutscenes.CutscenePanel;
import de.luckymcdev.foundryengine.client.editor.panel.cutscenes.CutsceneTimelinePanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.AreaPanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.DialogueEditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.RecipeEditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.explorer.ExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.panel.test.GizmoTestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.test.TestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.test.TextEditorTestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.CataloguePanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.ConsolePanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.DevToolsPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.EffectPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.GitPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.MinecraftToolsPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.OutlinePanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.ProblemsPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.StopwatchPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.WaypointPanel;
import de.luckymcdev.foundryengine.client.editor.panel.view.HotkeySettingsPanel;
import de.luckymcdev.foundryengine.client.editor.panel.view.InfoPanel;
import de.luckymcdev.foundryengine.client.editor.panel.view.ThemeSelectorPanel;
import de.luckymcdev.foundryengine.client.event.registry.RegistryEventClient;
import de.luckymcdev.foundryengine.client.ext.ModPathRecorder;
import de.luckymcdev.foundryengine.client.gizmo.GizmoBuffer;
import de.luckymcdev.foundryengine.client.gizmo.GizmoRenderer;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import de.luckymcdev.foundryengine.client.render.EngineSceneDepth;
import de.luckymcdev.foundryengine.client.waypoint.ClientWaypointManager;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.modcompat.BundleModContainer;
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueSession;
import de.luckymcdev.foundryengine.common.network.packets.BundleHashPacket;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ClientboundDialoguePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutscenePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.WaypointPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ClientBoundExplorerPacket;
import de.luckymcdev.foundryengine.common.network.packets.sync.ScreenEffectPacket;
import de.luckymcdev.foundryengine.common.util.FolderHash;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.config.Config;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.neoforge.api.event.ImGuiLoadEventsNeoforge;
import foundry.imgui.neoforge.api.event.RegisterImGuiFontsEventNeoforge;
import foundry.imgui.neoforge.api.event.RenderImGuiEventsNeoforge;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.flag.ImFontAtlasFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
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
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

//? if 26.1 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
//?}
//? if 26.2 {
/*import net.minecraft.client.PreferredGraphicsApi;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
*///?}

/**
 * Client-side entrypoint for FoundryEngine. Registers client event listeners, panels, and key bindings.
 */
@Mod(value = Common.MODID, dist = Dist.CLIENT)
public class FoundryEngineModClient {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IEventBus BUS = NeoForge.EVENT_BUS;
	public static final Identifier LOGO_TRANSPARENT = Common.id("textures/logo_transparent.png");
	public static final Identifier LOGO_BLACK = Common.id("textures/logo_black.png");
	public static final Identifier LOGO_WHITE = Common.id("textures/logo_white.png");

	public FoundryEngineModClient(IEventBus modBus, ModContainer modContainer) {
		modBus.addListener(this::onClientSetup);
		modBus.addListener(this::addClientReloadListener);
		modBus.addListener(this::onRegisterKeyMapping);
		modBus.addListener(this::onRegisterDebugEntry);
		modBus.addListener(this::onRegisterDebugRenderers);
		modBus.addListener(this::onRegisterGuiLayers);
		modBus.addListener(this::onRegisterParticleProviders);
		modBus.addListener(this::loadImGuiEvent);
		modBus.addListener(this::registerImGuiFonts);
		BUS.addListener(this::onClientTickPost);
		BUS.addListener(this::onRenderLevel);
		BUS.addListener(this::onAfterOpaqueFeatures);
		BUS.addListener(this::onRegisterCommands);
		BUS.addListener(this::onLoggingIn);
		BUS.addListener(this::onClientTickPre);
		BUS.addListener(this::onRenderFramePost);
		BUS.addListener(this::onItemTooltip);
		BUS.addListener(this::onRightClickItem);
		BUS.addListener(this::renderImGuiEvent);

		Config.registerClient(modContainer);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		//? if 26.2 {
		/*if (Minecraft.getInstance().options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN) {
			//throw new EngineException("Sadly due to how FoundryEngine renders its InGame Editor, Vulkan is not supported at this Time. Switch to OpenGL. or delte the Mod.");
		}
		*///?}

		NbtSuggestions.init();
		LOGGER.debug("FoundryEngineModClient setup called");
		ModPathRecorder.record();

		var collector = Common.getRegistryCollector();
		if (collector != null) {
			RegistryEventClient.registerRenderers(collector);
		}

		CutscenePacket.CLIENT_HANDLER = p -> Client.getCutsceneManager().handlePacket(p);
		ClientboundDialoguePacket.CLIENT_HANDLER = p -> {
			var clientManager = Client.getDialogueManager();
			switch (p.action()) {
				case SHOW -> {
					var node = DialogueNode.fromNbt(p.node());
					var session = DialogueSession.fromNbt(p.session());
					clientManager.startDialogue(p.treeId(), session, node);
				}
				case ADVANCE -> {
					var node = DialogueNode.fromNbt(p.node());
					var session = DialogueSession.fromNbt(p.session());
					clientManager.advanceDialogue(session, node);
				}
				case ENDED -> clientManager.endDialogue();
			}
		};
		ScreenEffectPacket.CLIENT_HANDLER = p -> Client.getPostEffectManager().startScreenEffect(p.name(), p.introTicks(), p.holdTicks(), p.outroTicks(), p.lerpType());
		ClientBoundExplorerPacket.CLIENT_HANDLER = p -> {
			switch (p.action()) {
				case FILE_LIST -> ExplorerPanel.INSTANCE.receiveRemoteFileList(p.entries());
				case FILE_CONTENT -> ExplorerPanel.INSTANCE.receiveRemoteFileContent(p.path(), p.payload());
				case RESOURCE_LIST -> ExplorerPanel.INSTANCE.receiveResourceList(p.resourceIds());
				case RESOURCE_CONTENT -> ExplorerPanel.INSTANCE.receiveResourceContent(p.path(), p.payload());
			}
		};

		Client.getEditorManager().register(
			TestPanel.INSTANCE,
			TextEditorTestPanel.INSTANCE,
			GizmoTestPanel.INSTANCE,
			ExplorerPanel.INSTANCE,
			ConsolePanel.INSTANCE,
			MinecraftToolsPanel.INSTANCE,
			DevToolsPanel.INSTANCE,
			ProblemsPanel.INSTANCE,
			StopwatchPanel.INSTANCE,
			InfoPanel.INSTANCE,
			CataloguePanel.INSTANCE,
			ThemeSelectorPanel.INSTANCE,
			EffectPanel.INSTANCE,
			AreaPanel.INSTANCE,
			RecipeEditorPanel.INSTANCE,
			CutscenePanel.INSTANCE,
			CutsceneTimelinePanel.INSTANCE,
			OutlinePanel.INSTANCE,
			WaypointPanel.INSTANCE,
			NodeTestPanel.INSTANCE,
			DialogueEditorPanel.INSTANCE,
			HotkeySettingsPanel.INSTANCE,
			GitPanel.INSTANCE
		);
		var hkm = Client.getHotKeyManager();
		hkm.register(Common.id("code_editor.save"), "Save", "Save active file", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_S);
		hkm.register(Common.id("code_editor.find"), "Find", "Find in active file", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_F);
		hkm.register(Common.id("code_editor.replace"), "Find/Replace", "Find and replace in active file", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_H);
		hkm.register(Common.id("code_editor.goto_line"), "Go to Line", "Go to a specific line", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_G);

		hkm.getImHotKey().setOnHotKeySet(hkm::save);
		hkm.load();

		event.enqueueWork(() -> {
			Common.getBundleManager().loadClientScripts();
			for (var container : Common.getBundleManager().getBundleContainers()) {
				if (container instanceof BundleModContainer) {
					container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
				}
			}
			//Client.getMc().reloadResourcePacks();
		});
	}

	private void registerImGuiFonts(RegisterImGuiFontsEventNeoforge event) {
		try(var ctx = ImGuiMC.withImGui()) {
			var font = ImGuiMC.getFont(ImGuiManager.FONT, false, false);
			font.setScale(font.getScale() * ImGuiManager.scaleOverride);
			ctx.io().setFontDefault(font);
			event.getAtlas().setFreeTypeRenderer(true);
			// its a bit better i guess
		}
	}

	private void loadImGuiEvent(ImGuiLoadEventsNeoforge.Pre event) {
		Client.getImGuiManager().create();
		Client.getMainMenu().register();
	}

	private void renderImGuiEvent(RenderImGuiEventsNeoforge.Pre event) {
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
				LOGGER.error("ImGui editor frame failed", e);
			}
		}
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

	private void onItemTooltip(ItemTooltipEvent event) {
		Client.getItemCommandManager().handleItemTooltip(event);
		Client.getTooltipManager().handleItemTooltip(event);
	}

	private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		Client.getItemCommandManager().handleRightClick(event);
	}

	private void onRegisterCommands(RegisterClientCommandsEvent event) {
		FoundryCommandsClient.registerAll(event.getDispatcher(), event.getBuildContext());
	}

	private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		FolderHash.hashFolderAsync(Common.BUNDLES)
			.thenAccept(hash -> ClientPacketDistributor.sendToServer(new BundleHashPacket(hash)))
			.exceptionally(e -> {
				LOGGER.error("Failed to hash bundles folder", e);
				return null;
			});
	}

	private void onAfterOpaqueFeatures(RenderLevelStageEvent.AfterOpaqueFeatures event) {
		EngineSceneDepth.update();
	}

	//? if 26.1 {
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
	//?} elif 26.2 {
	/*private void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
		var camState = event.getLevelRenderState().cameraRenderState;
		Client.updateMain(camState.viewRotationMatrix, camState.projectionMatrix);

		GizmoBuffer.startFrame();

		Client.getCutsceneManager().renderTick();
		Client.getEditorController().renderFeatures();

		var mc = Minecraft.getInstance();
		var submitNodeStorage = new net.minecraft.client.renderer.SubmitNodeStorage();
		Client.getWaypointRenderer().renderWaypoints(event, submitNodeStorage);
		Client.getAreaRenderer().renderAreaModules(event, submitNodeStorage);
		GizmoRenderer.render(submitNodeStorage, camState, false);

		mc.gameRenderer.featureRenderDispatcher().renderAllFeatures(submitNodeStorage);
	}
	*///?}

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