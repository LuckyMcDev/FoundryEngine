package de.luckymcdev.foundryengine.client.editor.builtin.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.network.packets.WaypointPacket;
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

    private static final ChatIcons.Entry[] ICON_ENTRIES = ChatIcons.values();
    private static final String[] ICON_LABELS = buildIconLabels();

    private final ImString newName = new ImString(64);
    private final ImInt selectedIconIndex = new ImInt(0);
    private int newColor = Color.TURQUOISE.argb();
    private boolean showNewForm = false;
    private Waypoint selectedWaypoint = null;
    private WaypointPanel() {
        super(Common.id("waypoint_panel"), "Waypoints", ImIcons.FA.FA_MAP_PIN, Shortcut.empty());
        this.category = PanelCategory.TOOLS;
        this.menuBar = true;
    }

    private static String[] buildIconLabels() {
        String[] labels = new String[ICON_ENTRIES.length];
        for (int i = 0; i < ICON_ENTRIES.length; i++) {
            labels[i] = ICON_ENTRIES[i].character() + "  " + ICON_ENTRIES[i].name();
        }
        return labels;
    }

    @Override
    public void content() {
        if (!ImGuiUtils.requireWorld("You need to join a world to manage waypoints.")) {
            return;
        }

        renderMenuBar();
        ImGui.separator();

        beginContent();
        renderWaypointList();
        endContent();
    }

    private void renderMenuBar() {
        if (!ImGui.beginMenuBar()) return;

        if (ImGui.menuItem(ImIcons.FA.FA_PLUS + " Create at Player Pos")) {
            showNewForm = !showNewForm;
        }

        if (ImGui.menuItem(ImIcons.FA.FA_TRASH + " Clear All")) {
            ClientPacketDistributor.sendToServer(WaypointPacket.clear());
            setStatus("Cleared all waypoints");
        }

        ImGui.endMenuBar();
    }

    private void renderWaypointList() {
        ImGui.beginChild("##waypoint_list", 0, 0, true);

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

        ImGui.endChild();
    }

    private void renderWaypointEntry(Waypoint wp) {
        float colorR = ((wp.color() >> 16) & 0xFF) / 255.0f;
        float colorG = ((wp.color() >> 8) & 0xFF) / 255.0f;
        float colorB = (wp.color() & 0xFF) / 255.0f;

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
        float[] col = {
                ((newColor >> 16) & 0xFF) / 255.0f,
                ((newColor >> 8) & 0xFF) / 255.0f,
                (newColor & 0xFF) / 255.0f
        };

        ImGui.colorButton("##color_preview", col[0], col[1], col[2], 1.0f, 0, 20, 20);
        ImGui.sameLine();
        ImGui.setNextItemWidth(200);
        if (ImGui.colorEdit3("##newcolorpicker", col, ImGuiColorEditFlags.NoInputs)) {
            newColor = (0xFF << 24)
                    | ((int) (col[0] * 255) << 16)
                    | ((int) (col[1] * 255) << 8)
                    | (int) (col[2] * 255);
        }

        ImGui.sameLine();
        for (Color preset : PRESET_COLORS) {
            if (ImGui.colorButton("##preset_" + (int) (preset.r() * 255) + "_" + (int) (preset.g() * 255),
                    preset.r(), preset.g(), preset.b(), 1.0f)) {
                newColor = preset.argb();
            }
            ImGui.sameLine();
        }
    }

    private void renderWaypointDetails() {
        if (selectedWaypoint == null) return;

        Waypoint wp = selectedWaypoint;
        ImGui.text("Waypoint Details: " + wp.name());
        ImGui.separator();

        float r = ((wp.color() >> 16) & 0xFF) / 255.0f;
        float g = ((wp.color() >> 8) & 0xFF) / 255.0f;
        float b = (wp.color() & 0xFF) / 255.0f;

        ImGui.colorButton("##detail_color", r, g, b, 1.0f, 0, 32, 32);
        ImGui.sameLine();
        ImGui.text(String.format("Icon: %s   Color: #%02X%02X%02X",
                wp.icon(), (int) (r * 255), (int) (g * 255), (int) (b * 255)));

        ImGui.text(String.format("Position: (%d, %d, %d)", wp.x(), wp.y(), wp.z()));

        ImGui.spacing();

        if (ImGui.button(ImIcons.FA.FA_LOCATION_ARROW + " Teleport")) {
            teleportTo(wp);
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Delete")) {
            deleteWaypoint(wp);
            selectedWaypoint = null;
        }
    }

    private void createWaypoint(String name) {
        Minecraft mc = Minecraft.getInstance();
        Vec3i pos = Client.getHitOrNull();
        if (pos == null) {
            pos = mc.player.blockPosition();
        }

        String icon = String.valueOf(ICON_ENTRIES[selectedIconIndex.get()].character());

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
