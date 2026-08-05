package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.editor.WaypointPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.util.ChatIcons;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.common.waypoint.Waypoint;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Vector3f;

import java.util.List;

public class WaypointPanel extends EditorPanel {
	public static final WaypointPanel INSTANCE = new WaypointPanel();
	private static final Color[] PRESET_COLORS = {
		Color.WHITE, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN,
		Color.CYAN, Color.BLUE, Color.PURPLE, Color.PINK, Color.TURQUOISE
	};

	private static final List<ChatIcons.Entry> ICON_ENTRIES = ChatIcons.values();
	private static final String[] ICON_LABELS = buildIconLabels();

	private final ImString newName = new ImString(64);
	private final ImInt selectedIconIndex = new ImInt(0);
	private Color newColor = Color.TURQUOISE;
	private boolean showNewForm = false;
	private Waypoint selectedWaypoint = null;

	private WaypointPanel() {
		super(new Builder(Common.id("waypoint_panel"))
			.icon(ImIcons.MAP_PIN)
			.category(PanelCategory.TOOLS)
			.menuBar(true));
	}

	private static String[] buildIconLabels() {
		String[] labels = new String[ICON_ENTRIES.size()];
		for (int i = 0; i < ICON_ENTRIES.size(); i++) {
			labels[i] = ICON_ENTRIES.get(i).character() + "  " + ICON_ENTRIES.get(i).name();
		}
		return labels;
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		if (!requireWorld("You need to join a world to manage waypoints.")) {
			return;
		}

		renderMenuBar();

		renderWaypointList(g);
	}

	private void renderMenuBar() {
		menuBar(() -> {
			if (ImGui.menuItem(ImIcons.PLUS + " Create at Player Pos")) {
				showNewForm = !showNewForm;
			}

			if (ImGui.menuItem(ImIcons.TRASH + " Clear All")) {
				ClientPacketDistributor.sendToServer(WaypointPacket.clear());
				setStatus("Cleared all waypoints");
			}
		});
	}

	private void renderWaypointList(ImGraphicsExtractor g) {
		g.scrollableRegion("##waypoint_list", () -> {
			if (showNewForm) {
				renderNewWaypointForm();
				ImGui.separator();
			}

			Minecraft mc = Minecraft.getInstance();
			ClientLevel level = mc.level;
			List<Waypoint> waypoints = Common.getWaypointManager().getWaypoints(level.dimension());

			if (waypoints.isEmpty()) {
				ImGui.textDisabled("No waypoints in current dimension");
			} else {
				ImGui.text("Waypoints in " + level.dimension().identifier() + ":");
				ImGui.separator();

				for (Waypoint wp : waypoints) {
					renderWaypointEntry(wp);
				}
			}

			if (selectedWaypoint != null) {
				ImGui.separator();
				renderWaypointDetails();
			}
		});
	}

	private void renderWaypointEntry(Waypoint wp) {
		float colorR = wp.color().r();
		float colorG = wp.color().g();
		float colorB = wp.color().b();

		ImGui.colorButton("##color_" + wp.x() + "_" + wp.y() + "_" + wp.z(),
			colorR, colorG, colorB, 1.0f);

		ImGui.sameLine();

		String label = String.format("%s [%s] (%d, %d, %d)",
			wp.name(), wp.icon(), wp.x(), wp.y(), wp.z());

		if (ImGui.selectable(label, selectedWaypoint == wp)) {
			selectedWaypoint = wp;
		}

		if (ImGui.beginPopupContextItem("##wp_context_" + wp.x() + "_" + wp.y() + "_" + wp.z())) {
			if (ImGui.menuItem("Teleport")) {
				teleportTo(wp);
			}
			if (ImGui.menuItem("Delete")) {
				deleteWaypoint(wp);
			}
			ImGui.endPopup();
		}
	}

	private void renderNewWaypointForm() {
		ImGui.text("Create New Waypoint");
		ImGui.separator();

		ImGui.text("Name:");
		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##newname", "e.g., Home, Spawn, Mine", newName);

		ImGui.text("Icon:");
		ImGui.setNextItemWidth(-1);
		if (ImGui.combo("##icon_combo", selectedIconIndex, ICON_LABELS)) {
			// icon selection changed
		}

		ImGui.text("Color:");
		renderNewColorPicker();

		boolean createClicked = ImGui.button("Create Waypoint");

		if (createClicked) {
			String name = newName.get().trim();
			if (name.isEmpty()) {
				setStatus("Error: Waypoint name cannot be empty");
			} else {
				createWaypoint(name);
				showNewForm = false;
				newName.set("");
			}
		}

		ImGui.sameLine();
		if (ImGui.button("Cancel")) {
			showNewForm = false;
			newName.set("");
		}
	}

	private void renderNewColorPicker() {
		float[] col = {newColor.r(), newColor.g(), newColor.b()};

		ImGui.setNextItemWidth(400);
		if (ImGui.colorEdit3("##newcolorpicker", col, ImGuiColorEditFlags.NoInputs)) {
			newColor = new Color(col[0], col[1], col[2], 1.0f);
		}

		ImGui.sameLine();
		for (Color preset : PRESET_COLORS) {
			if (ImGui.colorButton("##preset_" + (int) (preset.r() * 255) + "_" + (int) (preset.g() * 255),
				preset.r(), preset.g(), preset.b(), 1.0f)) {
				newColor = preset;
			}
			ImGui.sameLine();
		}
	}

	private void renderWaypointDetails() {
		if (selectedWaypoint == null) {
			return;
		}

		Waypoint wp = selectedWaypoint;
		ImGui.text("Waypoint Details: " + wp.name());
		ImGui.separator();

		float r = wp.color().r();
		float g = wp.color().g();
		float b = wp.color().b();

		ImGui.colorButton("##detail_color", r, g, b, 1.0f, 0, 32, 32);
		ImGui.sameLine();
		ImGui.text(String.format("Icon: %s   Color: #%02X%02X%02X",
			wp.icon(), (int) (r * 255), (int) (g * 255), (int) (b * 255)));

		ImGui.text(String.format("Position: (%d, %d, %d)", wp.x(), wp.y(), wp.z()));

		ImGui.spacing();

		if (ImGui.button(ImIcons.LOCATION_ARROW + " Teleport")) {
			teleportTo(wp);
		}
		ImGui.sameLine();
		if (ImGui.button(ImIcons.TRASH + " Delete")) {
			deleteWaypoint(wp);
			selectedWaypoint = null;
		}
	}

	private void createWaypoint(String name) {
		Minecraft mc = Minecraft.getInstance();
		Vec3i pos = Client.getBlockHitOrNull();
		if (pos == null) {
			pos = mc.player.blockPosition();
		}

		String icon = String.valueOf(ICON_ENTRIES.get(selectedIconIndex.get()).character());

		ClientPacketDistributor.sendToServer(WaypointPacket.add(
			pos.getX(), pos.getY(), pos.getZ(), name, icon, newColor
		));
		setStatus("Waypoint creation request sent: " + name);
	}

	private void teleportTo(Waypoint wp) {
		if (Client.getConnection() != null) {
			Client.getConnection().send(new ServerBoundTeleportPacket(
				new Vector3f(wp.x() + 0.5f, wp.y() + 1.0f, wp.z() + 0.5f)
			));
			setStatus("Teleported to waypoint: " + wp.name());
		}
	}

	private void deleteWaypoint(Waypoint wp) {
		ClientPacketDistributor.sendToServer(WaypointPacket.remove(wp.x(), wp.y(), wp.z()));
		setStatus("Waypoint deletion request sent: " + wp.name());
		if (selectedWaypoint == wp) {
			selectedWaypoint = null;
		}
	}
}
