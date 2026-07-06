package de.luckymcdev.foundryengine.client.editor;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorManager {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<Identifier, Panel> panels = new HashMap<>();

	public void register(Panel... panels) {
		for (Panel panel : panels) {
			register(panel);
		}
	}

	public void register(Panel panel) {
		if (panels.containsKey(panel.getId())) {
			LOGGER.error("Tried to register panel with duplicate id: {}", panel.getId());
		} else {
			panels.put(panel.getId(), panel);
		}
	}

	public void remove(Panel panel) {
		closePanel(panel);
		panels.remove(panel.getId());
	}

	public @Nullable Panel getPanel(Identifier id) {
		return panels.get(id);
	}

	public Collection<Panel> getPanels() {
		return Collections.unmodifiableCollection(panels.values());
	}

	public void openPanel(Panel panel) {
		panel.open();
	}

	public void closePanel(Panel panel) {
		panel.close();
	}

	public void togglePanel(Panel panel) {
		if (panel.isOpen()) {
			panel.close();
		} else {
			panel.open();
		}
	}

	public boolean isOpen(Panel panel) {
		return panel != null && panel.isOpen();
	}

	public void closeAllPanels() {
		List<Panel> snapshot = new ArrayList<>();
		for (Panel panel : panels.values()) {
			if (panel.isOpen()) {
				snapshot.add(panel);
			}
		}
		snapshot.forEach(Panel::close);
	}

	public void handleTick() {
		for (Panel panel : panels.values()) {
			if (panel.isOpen()) {
				panel.handleTick();
			}
		}
	}

	public void handleRender() {
		List<Panel> toClose = new ArrayList<>();

		for (Panel panel : List.copyOf(panels.values())) {
			if (panel.isOpen() && !panel.handleRender()) {
				toClose.add(panel);
			}
		}

		toClose.forEach(Panel::close);
	}
}
