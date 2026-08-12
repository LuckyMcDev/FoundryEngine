package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.imgui.text.ImGuiCoreTextEditor;
import de.luckymcdev.foundryengine.client.imgui.text.color.IEditorColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorTheme;
import de.luckymcdev.foundryengine.common.service.EngineService;
import de.luckymcdev.foundryengine.common.service.EngineServiceResult;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public abstract class EngineServicePanel<T extends EngineService> extends EditorPanel {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
	protected final ImGuiCoreTextEditor outputEditor;
	protected boolean running = false;
	protected boolean autoRefresh = true;
	protected int activeTab = 0;
	protected int previousTab = -1;

	protected EngineServicePanel(Builder builder) {
		super(builder);
		EditorTheme outputTheme = EditorTheme.dark().build();
		outputEditor = new ImGuiCoreTextEditor(createOutputColorizer(), null, outputTheme);
		outputEditor.setReadOnly(true);
		outputEditor.setText("Ready.");
	}

	protected abstract IEditorColorizer createOutputColorizer();

	protected abstract String[] getTabNames();

	protected abstract T getService();

	protected abstract void onTabChanged(int tab, T service);

	protected abstract void renderTabContent(ImGraphicsExtractor g, T service);

	protected void extraMenuItems(T service) {
		// hook for subclasses to add additional menu items
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		T service = getService();
		if (service == null) {
			g.centeredMessage(ImIcons.EXCLAMATION_TRIANGLE + "  Service not available.");
			return;
		}

		renderMenuBar(service);

		if (activeTab != previousTab && autoRefresh && !running) {
			onTabChanged(activeTab, service);
			previousTab = activeTab;
		}

		renderTabContent(g, service);

		renderOutput(g);
	}

	private void renderMenuBar(T service) {
		menuBar(() -> {
			if (ImGui.menuItem(ImIcons.REFRESH + " Refresh")) {
				onTabChanged(activeTab, service);
			}

			ImGui.separator();

			String[] tabs = getTabNames();
			for (int i = 0; i < tabs.length; i++) {
				boolean selected = activeTab == i;
				if (selected) ImGui.pushStyleColor(ImGuiCol.Text, 0xFF4CAF50);
				if (ImGui.menuItem(tabs[i])) {
					activeTab = i;
				}
				if (selected) ImGui.popStyleColor();
			}

			ImGui.separator();

			if (ImGui.menuItem(ImIcons.TRASH + " Clear Output")) {
				outputEditor.setText("");
			}

			ImGui.separator();

			if (ImGui.menuItem(ImIcons.REFRESH + " Auto Refresh", null, autoRefresh)) {
				autoRefresh = !autoRefresh;
			}

			extraMenuItems(service);
		});
	}

	protected void renderOutput(ImGraphicsExtractor g) {
		ImGui.spacing();
		ImGui.separator();

		g.section("Output");

		if (running) {
			ImGui.textDisabled(ImIcons.SPINNER + "  Running...");
		}

		float availH = ImGui.getContentRegionAvailY();
		if (availH < 60) {
			availH = 120;
		}

		outputEditor.render("##output_editor", ImGui.getContentRegionAvailX(), availH, false);
	}

	protected void run(CompletableFuture<EngineServiceResult> future, String label) {
		running = true;
		outputEditor.setText("Running " + label + "...");
		final String timestamp = "[" + LocalTime.now().format(TIME_FORMAT) + "] ";
		future.thenAccept(result -> {
			running = false;
			StringBuilder sb = new StringBuilder();
			sb.append(timestamp).append(label).append(":\n");
			if (!result.stdout().isEmpty()) {
				sb.append(result.stdout());
			}
			if (!result.stderr().isEmpty()) {
				if (!sb.isEmpty() && !sb.toString().endsWith("\n")) sb.append('\n');
				sb.append(result.stderr());
			}
			if (sb.isEmpty() || sb.toString().endsWith(":\n")) {
				sb.append(result.success() ? "Completed successfully." : "Failed (exit " + result.exitCode() + ").");
			}
			outputEditor.setText(sb.toString());
		});
	}

	protected boolean button(String label) {
		return !running && ImGui.button(label);
	}

	protected String timestamp() {
		return "[" + LocalTime.now().format(TIME_FORMAT) + "] ";
	}
}