package de.luckymcdev.foundryengine.editor;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.editor.client.panel.explorer.ExplorerPanel;
import de.luckymcdev.foundryengine.editor.client.panel.test.*;
import de.luckymcdev.foundryengine.editor.client.panel.tool.*;
import de.luckymcdev.foundryengine.editor.client.panel.view.*;
import de.luckymcdev.foundryengine.imgui.client.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.imgui.client.ImGuiManager;
import de.luckymcdev.foundryengine.network.common.packet.editor.GiveItemPacket;
import de.luckymcdev.foundryengine.network.common.packet.explorer.ClientBoundExplorerPacket;
import de.luckymcdev.foundryengine.network.common.packet.explorer.ServerBoundExplorerPacket;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.neoforge.api.event.*;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.*;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Owns the ImGui editor, its panels, hotkeys, icon exporter and reload listeners.
 */
@Mod(value = EditorMod.MODID)
public final class EditorMod implements EngineModPart {
	public static final String MODID = "foundryengine_editor";
	private static final Logger LOGGER = LogUtils.getLogger();

	public EditorMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
		var b = bus();
		modBus.addListener(this::onRegisterKeyMapping);
		modBus.addListener(this::onRegisterGuiLayers);
		modBus.addListener(this::addClientReloadListener);
		modBus.addListener(this::loadImGuiEvent);
		modBus.addListener(this::registerImGuiFonts);
		b.addListener(this::renderImGuiEvent);
		b.addListener(this::onClientTickPost);
		b.addListener(this::onRenderFramePost);
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(ServerBoundExplorerPacket.DEFINITION);
		network.register(ClientBoundExplorerPacket.DEFINITION);
		network.register(GiveItemPacket.DEFINITION);
	}

	@Override
	public void onClientSetup(FMLClientSetupEvent event) {
		Client.getEditorManager().register(
			TestPanel.INSTANCE, ExplorerPanel.INSTANCE, ConsolePanel.INSTANCE,
			MinecraftToolsPanel.INSTANCE, DevToolsPanel.INSTANCE, ProblemsPanel.INSTANCE,
			StopwatchPanel.INSTANCE, InfoPanel.INSTANCE, CataloguePanel.INSTANCE,
			ThemeSelectorPanel.INSTANCE, EffectPanel.INSTANCE, OutlinePanel.INSTANCE,
			NodeTestPanel.INSTANCE, HotkeySettingsPanel.INSTANCE, GitPanel.INSTANCE,
			PakkuPanel.INSTANCE
		);

		var hkm = Client.getHotKeyManager();
		hkm.register(Common.id("code_editor.save"), "Save", "Save active file", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_S);
		hkm.register(Common.id("code_editor.find"), "Find", "Find in active file", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_F);
		hkm.register(Common.id("code_editor.replace"), "Find/Replace", "Find and replace", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_H);
		hkm.register(Common.id("code_editor.goto_line"), "Go to Line", "Go to a specific line", null, InputConstants.KEY_LCONTROL, InputConstants.KEY_G);
		hkm.getImHotKey().setOnHotKeySet(hkm::save);
		hkm.load();

		ClientBoundExplorerPacket.CLIENT_HANDLER = p -> {
			switch (p.action()) {
				case FILE_LIST -> ExplorerPanel.INSTANCE.receiveRemoteFileList(p.entries());
				case FILE_CONTENT -> ExplorerPanel.INSTANCE.receiveRemoteFileContent(p.path(), p.payload());
				case RESOURCE_LIST -> ExplorerPanel.INSTANCE.receiveResourceList(p.resourceIds());
				case RESOURCE_CONTENT -> ExplorerPanel.INSTANCE.receiveResourceContent(p.path(), p.payload());
			}
		};
	}

	private void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
		event.registerCategory(Client.EDITOR_CATEGORY);
		event.register(Client.EDITOR_KEY);
		event.register(Client.MENU_BAR_KEY);
	}

	private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(Common.id("icon_exporter"), Client.getIconExporterLayer());
	}

	private void addClientReloadListener(AddClientReloadListenersEvent event) {
		event.addListener(Common.id("item_icon_cache"),
			createReloadListener(ImGraphicsExtractor::clearItemIconCache));
	}

	private PreparableReloadListener createReloadListener(Runnable r) {
		return (s, bg, barrier, game) ->
			CompletableFuture.<Void>supplyAsync(() -> null, bg)
				.thenCompose(barrier::wait)
				.thenAcceptAsync(v -> r.run(), game);
	}

	private void loadImGuiEvent(ImGuiLoadEventsNeoforge.Pre event) {
		Client.getImGuiManager().create();
		Client.getMainMenu().register();
	}

	private void registerImGuiFonts(RegisterImGuiFontsEventNeoforge event) {
		try (var ctx = ImGuiMC.withImGui()) {
			var font = ImGuiMC.getFont(ImGuiManager.FONT, false, false);
			font.setScale(font.getScale() * ImGuiManager.scaleOverride);
			ctx.io().setFontDefault(font);
			event.getAtlas().setFreeTypeRenderer(true);
		}
	}

	private void renderImGuiEvent(RenderImGuiEventsNeoforge.Pre event) {
		var imgui = Client.getImGuiManager();
		if (!imgui.isEnabled()) {
			return;
		}
		var mainMenu = Client.getMainMenu();
		var editor = Client.getEditorManager();
		try {
			imgui.begin();
			mainMenu.handleShortcuts();
			if (imgui.isMenuBarVisible()) {
				mainMenu.render();
			}
			editor.handleRender();
		} catch (Exception e) {
			LOGGER.error("ImGui editor frame failed", e);
		}
	}

	private void onClientTickPost(ClientTickEvent.Post event) {
		Client.getEditorManager().handleTick();
		Client.getEditorController().clientTick();
	}

	private void onRenderFramePost(RenderFrameEvent.Post event) {
		Client.getIconExporterLayer().onPostRender();
	}
}