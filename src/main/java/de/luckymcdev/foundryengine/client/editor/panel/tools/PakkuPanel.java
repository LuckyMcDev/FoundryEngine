package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.imgui.text.color.IEditorColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.preset.pakku.PakkuColorizer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.service.PakkuService;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;

public class PakkuPanel extends EngineServicePanel<PakkuService> {

	public static final PakkuPanel INSTANCE = new PakkuPanel();

	private static final int TAB_LIST = 0;
	private static final int TAB_ADD = 1;
	private static final int TAB_REMOVE = 2;
	private static final int TAB_UPDATE = 3;
	private static final int TAB_STATUS = 4;
	private static final int TAB_INIT = 5;

	private final ImString nameInput = new ImString(128);

	private PakkuPanel() {
		super(new Builder(Common.id("pakku"))
			.icon(ImIcons.FOLDER)
			.category(PanelCategory.TOOLS)
			.menuBar(true));
	}

	@Override
	protected IEditorColorizer createOutputColorizer() {
		return new PakkuColorizer();
	}

	@Override
	protected String[] getTabNames() {
		return new String[]{"List", "Add", "Remove", "Update", "Status", "Init"};
	}

	@Override
	protected PakkuService getService() {
		return Common.getEngineServiceManager().get(PakkuService.class).orElse(null);
	}

	@Override
	protected void onTabChanged(int tab, PakkuService pakku) {
		switch (tab) {
			case TAB_LIST -> run(pakku.list(), "List");
			case TAB_STATUS -> run(pakku.status(), "Status");
			default -> {}
		}
	}

	@Override
	protected void renderTabContent(ImGraphicsExtractor g, PakkuService pakku) {
		switch (activeTab) {
			case TAB_LIST -> renderListTab(g, pakku);
			case TAB_ADD -> renderAddTab(g, pakku);
			case TAB_REMOVE -> renderRemoveTab(g, pakku);
			case TAB_UPDATE -> renderUpdateTab(g, pakku);
			case TAB_STATUS -> renderStatusTab(g, pakku);
			case TAB_INIT -> renderInitTab(g, pakku);
		}
	}

	private void renderListTab(ImGraphicsExtractor g, PakkuService pakku) {
		g.cardBegin("##pakku_list_card");
		ImGui.text(ImIcons.LIST + "  List Projects");
		ImGui.spacing();

		if (button(ImIcons.LIST + " List")) {
			run(pakku.list(), "List");
		}
		g.cardEnd();
	}

	private void renderAddTab(ImGraphicsExtractor g, PakkuService pakku) {
		g.cardBegin("##pakku_add_card");
		ImGui.text(ImIcons.PLUS + "  Add Projects");
		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##pakku_add_name", "Project slug(s) separated by space...", nameInput, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.PLUS + " Add")) {
			if (!nameInput.isEmpty()) {
				String[] projects = nameInput.get().split("\\s+");
				run(pakku.add(projects), "Add " + nameInput.get());
				nameInput.set("");
			}
		}
		g.helpTooltip("Add new projects with automatic dependency resolution");
		g.cardEnd();
	}

	private void renderRemoveTab(ImGraphicsExtractor g, PakkuService pakku) {
		g.cardBegin("##pakku_remove_card");
		ImGui.text(ImIcons.TRASH + "  Remove Projects");
		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##pakku_remove_name", "Project slug(s) separated by space...", nameInput, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Remove")) {
			if (!nameInput.isEmpty()) {
				String[] projects = nameInput.get().split("\\s+");
				run(pakku.remove(projects), "Remove " + nameInput.get());
				nameInput.set("");
			}
		}
		g.helpTooltip("Remove projects safely with dependency checking");
		g.cardEnd();
	}

	private void renderUpdateTab(ImGraphicsExtractor g, PakkuService pakku) {
		g.cardBegin("##pakku_update_card");
		ImGui.text(ImIcons.REFRESH + "  Update Projects");
		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##pakku_update_name", "Project slug(s) (empty = all)...", nameInput, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.REFRESH + " Update")) {
			if (nameInput.isEmpty()) {
				run(pakku.updateAll(), "Update All");
			} else {
				String[] projects = nameInput.get().split("\\s+");
				run(pakku.update(projects), "Update " + nameInput.get());
				nameInput.set("");
			}
		}
		g.helpTooltip("Update projects individually or in bulk");
		g.cardEnd();
	}

	private void renderStatusTab(ImGraphicsExtractor g, PakkuService pakku) {
		g.cardBegin("##pakku_status_card");
		ImGui.text(ImIcons.INFO + "  Modpack Status");
		ImGui.spacing();

		if (button(ImIcons.INFO + " Check Status")) {
			run(pakku.status(), "Status");
		}
		g.helpTooltip("Check which projects have a new version available");
		g.cardEnd();
	}

	private void renderInitTab(ImGraphicsExtractor g, PakkuService pakku) {
		g.cardBegin("##pakku_init_card");
		ImGui.text(ImIcons.COG + "  Initialize Pakku Project");
		ImGui.spacing();

		ImGui.textWrapped("This will create a pakku-lock.json file in the FoundryEngine directory.");
		ImGui.spacing();

		if (button(ImIcons.PLUS + " Initialize")) {
			run(pakku.init(), "Init");
		}
		g.helpTooltip("Initializes a new Pakku project in the current directory");
		g.cardEnd();
	}
}