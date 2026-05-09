package de.luckymcdev.foundryengine.client.editor.builtin.area;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import de.luckymcdev.foundryengine.common.network.packets.AreaPacket;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class AreaPanel extends EditorPanel {
    public static final AreaPanel INSTANCE = new AreaPanel();

    private final ImString newAreaName = new ImString(64);
    private final ImString areaSizeX = new ImString("5", 16);
    private final ImString areaSizeY = new ImString("4", 16);
    private final ImString areaSizeZ = new ImString("5", 16);
    private final ImString areaOffsetY = new ImString("0", 16);
    public boolean showDebugOutlines = false;
    private boolean showNewForm = false;
    private boolean showAreaDetails = false;
    private Area selectedArea = null;
    private String statusMessage = "";
    private long statusExpiry = 0L;

    private AreaPanel() {
        super(Common.id("area_panel"), "Areas", ImIcons.FA.FA_MAP, Shortcut.ctrl(ImGuiKey.F4));
        this.category = PanelCategory.EDITOR_SCENE;
        this.menuBar = true;
    }

    @Override
    public void content() {
        if (!ImGuiUtils.requireWorld("You need to join a world to manage areas.")) {
            return;
        }

        renderMenuBar();
        ImGui.separator();

        renderAreaList();
        renderStatus();
    }

    private void renderMenuBar() {
        if (!ImGui.beginMenuBar()) return;

        if (ImGui.menuItem(ImIcons.FA.FA_ARROW_ROTATE_RIGHT + " Refresh")) {
            refreshAreas();
        }

        if (ImGui.menuItem(ImIcons.FA.FA_EYE + " Toggle Debug Outlines")) {
            toggleDebugOutlines();
        }

        if (ImGui.menuItem("Create Area")) {
            showNewForm = !showNewForm;
        }

        ImGui.endMenuBar();
    }

    private void renderAreaList() {
        ImGui.beginChild("##area_list", 0, 0, true);

        if (showNewForm) {
            renderNewAreaForm();
            ImGui.separator();
        }

        if (showAreaDetails && selectedArea != null) {
            renderAreaDetails();
            ImGui.separator();
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        AreaManager areaManager = Common.getAreaManager();

        // Get areas for current dimension
        List<Area> areas = areaManager.getAreasForDimension(level.dimension());

        if (areas == null || areas.isEmpty()) {
            ImGui.textDisabled("No areas in current dimension");
        } else {
            ImGui.text("Areas in " + level.dimension().identifier() + ":");
            ImGui.separator();

            for (Area area : areas) {
                String areaLabel = String.format("%s (%.0f, %.0f, %.0f) to (%.0f, %.0f, %.0f)",
                        area.id(),
                        area.bounds().minX, area.bounds().minY, area.bounds().minZ,
                        area.bounds().maxX, area.bounds().maxY, area.bounds().maxZ);

                if (ImGui.selectable(areaLabel, selectedArea != null && selectedArea.id().equals(area.id()))) {
                    selectedArea = area;
                    showAreaDetails = true;
                }
            }
        }

        ImGui.endChild();
    }

    private void renderNewAreaForm() {
        ImGui.text("Create New Area");
        ImGui.separator();

        ImGui.text("Area Name:");
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Unique identifier for the area (no spaces recommended)");
        }
        ImGui.setNextItemWidth(-1);
        boolean confirm = ImGui.inputTextWithHint("##newareaname", "e.g., spawn_area, pvp_zone", newAreaName,
                ImGuiInputTextFlags.EnterReturnsTrue);

        ImGui.text("Size (blocks):");
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Dimensions of the area in blocks (X=width, Y=height, Z=depth)");
        }
        ImGui.text("X:");
        ImGui.sameLine();
        ImGui.setNextItemWidth(60);
        ImGui.inputText("##sizeX", areaSizeX, ImGuiInputTextFlags.CharsDecimal);

        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        ImGui.setNextItemWidth(60);
        ImGui.inputText("##sizeY", areaSizeY, ImGuiInputTextFlags.CharsDecimal);

        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        ImGui.setNextItemWidth(60);
        ImGui.inputText("##sizeZ", areaSizeZ, ImGuiInputTextFlags.CharsDecimal);

        ImGui.text("Vertical Offset:");
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Vertical position adjustment (0 = centered on player)");
        }
        ImGui.sameLine();
        ImGui.setNextItemWidth(60);
        ImGui.inputText("##offsetY", areaOffsetY, ImGuiInputTextFlags.CharsDecimal);
        ImGui.sameLine();
        ImGui.textDisabled("(0 = centered on player)");

        boolean createClicked = ImGui.button("Create Area");
        if (createClicked || confirm) {
            String name = newAreaName.get().trim();
            if (name.isEmpty()) {
                setStatus("Error: Area name cannot be empty");
            } else {
                try {
                    // Quick validation of size fields
                    Integer.parseInt(areaSizeX.get());
                    Integer.parseInt(areaSizeY.get());
                    Integer.parseInt(areaSizeZ.get());
                    Integer.parseInt(areaOffsetY.get());

                    createNewArea(name);
                    setStatus("Area creation request sent: " + name);
                    showNewForm = false;
                    newAreaName.set("");
                } catch (NumberFormatException e) {
                    setStatus("Error: Invalid number format in size fields");
                }
            }
        }

        ImGui.sameLine();
        if (ImGui.button("Cancel")) {
            showNewForm = false;
            newAreaName.set("");
        }
    }

    private void refreshAreas() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            var packet = AreaPacket.requestSync(mc.level.dimension().identifier());
            Common.getNetworkManager().sendToServer(packet);
            setStatus("Requesting area sync from server...");
        } else {
            setStatus("Cannot refresh: not in a world");
        }
    }

    private void toggleDebugOutlines() {
        showDebugOutlines = !showDebugOutlines;
        setStatus("Debug outlines " + (showDebugOutlines ? "enabled" : "disabled"));
    }

    private void renderAreaDetails() {
        if (selectedArea == null) {
            showAreaDetails = false;
            return;
        }

        ImGui.text("Area Details: " + selectedArea.id());
        ImGui.separator();

        AABB bounds = selectedArea.bounds();
        ImGui.text(String.format("Position: (%.1f, %.1f, %.1f) to (%.1f, %.1f, %.1f)",
                bounds.minX, bounds.minY, bounds.minZ,
                bounds.maxX, bounds.maxY, bounds.maxZ));

        ImGui.text(String.format("Size: %.1f x %.1f x %.1f",
                bounds.maxX - bounds.minX,
                bounds.maxY - bounds.minY,
                bounds.maxZ - bounds.minZ));

        ImGui.text("Dimension: " + selectedArea.dimension().identifier());

        if (ImGui.button("Teleport to Area")) {
            teleportToArea();
        }

        ImGui.sameLine();
        if (ImGui.button("Delete Area")) {
            deleteArea();
        }

        ImGui.sameLine();
        if (ImGui.button("Close")) {
            showAreaDetails = false;
        }
    }

    private void createNewArea(String name) {
        Minecraft mc = Minecraft.getInstance();
        // Level and player checks are handled at the panel level, so we can assume they're available here

        try {
            // Parse size values
            int sizeX = Integer.parseInt(areaSizeX.get());
            int sizeY = Integer.parseInt(areaSizeY.get());
            int sizeZ = Integer.parseInt(areaSizeZ.get());
            int offsetY = Integer.parseInt(areaOffsetY.get());

            // Validate sizes
            if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
                setStatus("Error: Size values must be positive");
                return;
            }

            // Create area around the player
            Vec3 playerPos = mc.player.position();
            double centerY = playerPos.y + offsetY;

            Vec3 min = new Vec3(
                    playerPos.x - sizeX / 2.0,
                    centerY - sizeY / 2.0,
                    playerPos.z - sizeZ / 2.0
            );

            Vec3 max = new Vec3(
                    playerPos.x + sizeX / 2.0,
                    centerY + sizeY / 2.0,
                    playerPos.z + sizeZ / 2.0
            );

            Area newArea = Area.of(name, min, max, mc.level.dimension());

            // Send to server to register the area
            var packet = AreaPacket.create(newArea);
            Common.getNetworkManager().sendToServer(packet);

            setStatus("Area creation request sent: " + name);

        } catch (NumberFormatException e) {
            setStatus("Error: Invalid number format in size fields");
        } catch (Exception e) {
            setStatus("Error creating area: " + e.getMessage());
        }
    }

    private void renderStatus() {
        if (!statusMessage.isEmpty()) {
            if (System.currentTimeMillis() > statusExpiry) {
                statusMessage = "";
            } else {
                ImGui.separator();
                ImGui.textDisabled(statusMessage);
            }
        }
    }

    private void teleportToArea() {
        if (selectedArea == null) return;

        Minecraft mc = Minecraft.getInstance();
        // Level and player checks are handled at the panel level

        AABB bounds = selectedArea.bounds();
        Vector3f center = new Vector3f(
                (float) ((bounds.minX + bounds.maxX) / 2.0),
                (float) (bounds.minY + 1.0),
                (float) ((bounds.minZ + bounds.maxZ) / 2.0)
        );
        if (Client.getConnection() != null) {
            Client.getConnection().send(new ServerBoundTeleportPacket(center));
        }
        setStatus("Teleported to center of area: " + selectedArea.id());
    }

    private void deleteArea() {
        if (selectedArea == null) return;

        Minecraft mc = Minecraft.getInstance();
        // Level check is handled at the panel level

        // Send removal request to server
        var packet = AreaPacket.remove(selectedArea.id(), selectedArea.dimension().identifier());
        Common.getNetworkManager().sendToServer(packet);

        setStatus("Area deletion request sent: " + selectedArea.id());
        showAreaDetails = false;
        selectedArea = null;
    }


    private void setStatus(String message) {
        statusMessage = message;
        statusExpiry = System.currentTimeMillis() + 4000L;
    }
}