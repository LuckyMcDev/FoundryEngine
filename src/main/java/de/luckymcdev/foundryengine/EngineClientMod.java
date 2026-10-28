package de.luckymcdev.foundryengine;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.command.client.FoundryCommandsClient;
import de.luckymcdev.foundryengine.command.client.suggest.nbt.NbtSuggestions;
import de.luckymcdev.foundryengine.debug.client.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.debug.client.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.editor.client.panel.test.NodeTestPanel;
import de.luckymcdev.foundryengine.editor.client.panel.cutscene.CutscenePanel;
import de.luckymcdev.foundryengine.editor.client.panel.cutscene.CutsceneTimelinePanel;
import de.luckymcdev.foundryengine.editor.client.panel.editor.AreaPanel;
import de.luckymcdev.foundryengine.editor.client.panel.editor.DialogueEditorPanel;
import de.luckymcdev.foundryengine.editor.client.panel.editor.RecipeEditorPanel;
import de.luckymcdev.foundryengine.editor.client.panel.explorer.ExplorerPanel;
import de.luckymcdev.foundryengine.editor.client.panel.test.GizmoTestPanel;
import de.luckymcdev.foundryengine.editor.client.panel.test.TestPanel;
import de.luckymcdev.foundryengine.editor.client.panel.test.TextEditorTestPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.CataloguePanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.ConsolePanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.DevToolsPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.EffectPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.GitPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.MinecraftToolsPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.OutlinePanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.PakkuPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.ProblemsPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.StopwatchPanel;
import de.luckymcdev.foundryengine.editor.client.panel.tool.WaypointPanel;
import de.luckymcdev.foundryengine.editor.client.panel.view.HotkeySettingsPanel;
import de.luckymcdev.foundryengine.editor.client.panel.view.InfoPanel;
import de.luckymcdev.foundryengine.editor.client.panel.view.ThemeSelectorPanel;
import de.luckymcdev.foundryengine.event.client.registry.RegistryEventClient;
import de.luckymcdev.foundryengine.ext.client.ModPathRecorder;
import de.luckymcdev.foundryengine.gizmo.client.GizmoBuffer;
import de.luckymcdev.foundryengine.gizmo.client.GizmoRenderer;
import de.luckymcdev.foundryengine.imgui.client.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.imgui.client.ImGuiManager;
import de.luckymcdev.foundryengine.render.client.EngineSceneDepth;
import de.luckymcdev.foundryengine.script.client.MaliciousScriptScreen;
import de.luckymcdev.foundryengine.waypoint.client.ClientWaypointManager;
import de.luckymcdev.foundryengine.bundle.common.modcompat.BundleModContainer;
import de.luckymcdev.foundryengine.dialogue.common.DialogueNode;
import de.luckymcdev.foundryengine.dialogue.common.DialogueSession;
import de.luckymcdev.foundryengine.network.common.packet.BundleHashPacket;
import de.luckymcdev.foundryengine.network.common.packet.dialogue.ClientboundDialoguePacket;
import de.luckymcdev.foundryengine.network.common.packet.editor.CutscenePacket;
import de.luckymcdev.foundryengine.network.common.packet.editor.WaypointPacket;
import de.luckymcdev.foundryengine.network.common.packet.explorer.ClientBoundExplorerPacket;
import de.luckymcdev.foundryengine.network.common.packet.sync.ScreenEffectPacket;
import de.luckymcdev.foundryengine.script.common.sandbox.SandboxWaiver;
import de.luckymcdev.foundryengine.util.common.FolderHash;
import de.luckymcdev.foundryengine.util.common.color.Color;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.config.StartupConfig;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.neoforge.api.event.ImGuiLoadEventsNeoforge;
import foundry.imgui.neoforge.api.event.RegisterImGuiFontsEventNeoforge;
import foundry.imgui.neoforge.api.event.RenderImGuiEventsNeoforge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
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
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

//? if >=26.2 {
/*import net.minecraft.client.PreferredGraphicsApi;
import de.luckymcdev.foundryengine.exception.common.EngineException;
*///?} else {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
//?}

/**
 * Client-side entrypoint for FoundryEngine. Registers client event listeners, panels, and key bindings.
 */
@Mod(value = Common.MODID, dist = Dist.CLIENT)
public class EngineClientMod {
	public static final Identifier LOGO_TRANSPARENT = Common.id("textures/logo_transparent.png");
	public static final Identifier LOGO_BLACK = Common.id("textures/logo_black.png");
	public static final Identifier LOGO_WHITE = Common.id("textures/logo_white.png");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IEventBus BUS = NeoForge.EVENT_BUS;
	private static boolean handledClassFilter = false;

	public EngineClientMod(IEventBus modBus, ModContainer modContainer) {
		modBus.addListener(this::onClientSetup);
		modBus.addListener(this::addClientReloadListener);
		modBus.addListener(this::onRegisterKeyMapping);
		modBus.addListener(this::onRegisterDebugEntry);
		modBus.addListener(this::onRegisterDebugRenderers);
		modBus.addListener(this::onRegisterGuiLayers);
		modBus.addListener(this::onRegisterParticleProviders);
		modBus.addListener(this::onRegisterMenuScreens);
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
		BUS.addListener(this::onScreenOpening);

		Config.registerClient(modContainer);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		//? if >=26.2 {
		/*if (Minecraft.getInstance().options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN) {
			//throw new EngineException("Sadly due to how FoundryEngine renders its InGame Editor, Vulkan is not supported at this Time. Switch to OpenGL. or delte the Mod.");
		}
		*///?}

		NbtSuggestions.init();
		LOGGER.debug("ClientMod setup called");
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
			GitPanel.INSTANCE,
			PakkuPanel.INSTANCE
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
			Common.getBundleManager().reportScriptErrors();
			for (var container : Common.getBundleManager().getBundleContainers()) {
				if (container instanceof BundleModContainer) {
					container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
				}
			}
			//Client.getMc().reloadResourcePacks();
		});
	}

	private void checkMaliciousScriptConfig() {
		if (StartupConfig.DISABLE_CLASS_FILTER.get() && !SandboxWaiver.isAccepted()) {
			var parent = Client.getCurrentScreen();
			Minecraft.getInstance().execute(() -> {
				Client.setScreen(new MaliciousScriptScreen(parent, this::onClassFilterAccepted, this::onClassFilterDeclined));
			});
		}
	}

	private void onClassFilterAccepted() {
		try {
			SandboxWaiver.markAccepted();
		} catch (IOException e) {
			LOGGER.error("Failed to write class filter acceptance file", e);
			return;
		}
		LOGGER.info("Class filter acknowledged by user - disabling class filter");
		Common.getScriptManager().unloadAll();
	}

	private void onClassFilterDeclined() {
		StartupConfig.DISABLE_CLASS_FILTER.set(false);
		StartupConfig.DISABLE_CLASS_FILTER.save();
		StartupConfig.SPEC.save();
		LOGGER.info("User declined to disable class filter - keeping sandbox enabled");
		Common.getScriptManager().unloadAll();
	}

	private void registerImGuiFonts(RegisterImGuiFontsEventNeoforge event) {
		try (var ctx = ImGuiMC.withImGui()) {
			var font = ImGuiMC.getFont(ImGuiManager.FONT, false, false);
			font.setScale(font.getScale() * ImGuiManager.scaleOverride);
			ctx.io().setFontDefault(font);
			event.getAtlas().setFreeTypeRenderer(true);
			// its a bit better i guess
		}
	}

	private void onScreenOpening(ScreenEvent.Opening event) {
		if (handledClassFilter || !(event.getNewScreen() instanceof TitleScreen title)) {
			return;
		}
		handledClassFilter = true;
		checkMaliciousScriptConfig();
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

	private void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		var collector = Common.getRegistryCollector();
		if (collector != null) {
			RegistryEventClient.registerMenuScreens(event, collector);
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

	//? if >=26.2 {
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
	*///?} else {
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
	//?}

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