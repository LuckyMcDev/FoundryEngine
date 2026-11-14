package de.luckymcdev.foundryengine.editor.client;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.editor.client.panel.Panel;
import de.luckymcdev.foundryengine.editor.client.popup.PopUp;
import de.luckymcdev.foundryengine.manager.api.Render;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorManager implements Render {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<Identifier, Panel> panels = new HashMap<>();
	private final Map<Identifier, PopUp> popups = new HashMap<>();

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
			Client.getHotKeyManager().registerPanel(panel, () -> togglePanel(panel));
		}
	}

	public void register(PopUp... popups) {
		for (PopUp popup : popups) {
			register(popup);
		}
	}

	public void register(PopUp popup) {
		if (popups.containsKey(popup.getId())) {
			LOGGER.error("Tried to register popup with duplicate id: {}", popup.getId());
		} else {
			popups.put(popup.getId(), popup);
		}
	}

	public void remove(Panel panel) {
		closePanel(panel);
		panels.remove(panel.getId());
		Client.getHotKeyManager().remove(panel.getId());
	}

	public void remove(PopUp popup) {
		popup.close();
		popups.remove(popup.getId());
	}

	public @Nullable Panel getPanel(Identifier id) {
		return panels.get(id);
	}

	public @Nullable PopUp getPopup(Identifier id) {
		return popups.get(id);
	}

	public Collection<Panel> getPanels() {
		return Collections.unmodifiableCollection(panels.values());
	}

	public Collection<PopUp> getPopups() {
		return Collections.unmodifiableCollection(popups.values());
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

	public void openPopup(PopUp popup) {
		popup.open();
	}

	public void closePopup(PopUp popup) {
		popup.close();
	}

	public void togglePopup(PopUp popup) {
		if (popup.isOpen()) {
			popup.close();
		} else {
			popup.open();
		}
	}

	public boolean isOpen(PopUp popup) {
		return popup != null && popup.isOpen();
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

	public void closeAllPopups() {
		List<PopUp> snapshot = new ArrayList<>();
		for (PopUp popup : popups.values()) {
			if (popup.isOpen()) {
				snapshot.add(popup);
			}
		}
		snapshot.forEach(PopUp::close);
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

		for (PopUp popup : List.copyOf(popups.values())) {
			popup.handleRender();
		}
	}

	//? if >=26.2 {
	/*@Override
	public void render(RenderLevelStageEvent.AfterLevel event, net.minecraft.client.renderer.SubmitNodeStorage submitNodes) {
		Client.getEditorController().renderFeatures();
	}*/
	//?} else {
	@Override
	public void render(RenderLevelStageEvent.AfterLevel event) {
		Client.getEditorController().renderFeatures();
	}
	//?}
}