package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.PanelRequirements;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import de.luckymcdev.foundryengine.common.network.packets.AreaPacket;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.type.ImInt;
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
    private final ImInt areaSizeX = new ImInt(5);
    private final ImInt areaSizeY = new ImInt(4);
    private final ImInt areaSizeZ = new ImInt(5);
    private final ImInt areaOffsetY = new ImInt(0);
    private final ImInt editMinX = new ImInt();
    private final ImInt editMinY = new ImInt();
    private final ImInt editMinZ = new ImInt();
    private final ImInt editMaxX = new ImInt();
    private final ImInt editMaxY = new ImInt();
    private final ImInt editMaxZ = new ImInt();

    private final float[] newAreaColor = new float[3];
    private final float[] editColor = new float[3];

    private boolean showNewForm = false;
    private boolean showAreaDetails = false;
    private Area selectedArea = null;
    private String selectedAreaId = null;

    private AreaPanel() {
        super(Common.id("area_panel"), "Areas", ImIcons.FA.FA_MAP);
        this.category = PanelCategory.EDITOR;
        this.menuBar = true;
        colorToFloats(Area.DEFAULT_COLOR, newAreaColor);
    }

    private static void colorToFloats(int argb, float[] out) {
        Color c = new Color(argb);
        out[0] = c.r();
        out[1] = c.g();
        out[2] = c.b();
    }

    private static int floatsToColor(float[] rgb) {
        return new Color(rgb[0], rgb[1], rgb[2], 1.0f).argb();
    }

    @Override
    public void content() {
        if (!PanelRequirements.requireWorld("You need to join a world to manage areas.")) {
            return;
        }

        renderMenuBar();
        ImGui.separator();

        beginContent();
        renderAreaList();
        endContent();
    }

    private void renderMenuBar() {
        if (!ImGui.beginMenuBar()) return;

        if (ImGui.menuItem(ImIcons.FA.FA_ARROW_ROTATE_RIGHT + " Refresh")) {
            refreshAreas();
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
                    if (!area.id().equals(selectedAreaId)) {
                        selectedAreaId = area.id();
                        populateEditFields(area);
                    }
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
        ImGui.inputTextWithHint("##newareaname", "e.g., spawn_area, pvp_zone", newAreaName);
        boolean nameConfirmed = ImGui.isItemDeactivatedAfterEdit();

        ImGui.text("Size (blocks):");
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Dimensions of the area in blocks (X=width, Y=height, Z=depth)");
        }

        ImGui.text("X:");
        ImGui.sameLine();
        ImGui.setNextItemWidth(80);
        ImGui.inputInt("##sizeX", areaSizeX);

        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        ImGui.setNextItemWidth(80);
        ImGui.inputInt("##sizeY", areaSizeY);

        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        ImGui.setNextItemWidth(80);
        ImGui.inputInt("##sizeZ", areaSizeZ);

        ImGui.text("Vertical Offset:");
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Vertical position adjustment (0 = centered on player)");
        }
        ImGui.sameLine();
        ImGui.setNextItemWidth(80);
        ImGui.inputInt("##offsetY", areaOffsetY);
        ImGui.sameLine();
        ImGui.textDisabled("(0 = centered on player)");

        ImGui.text("Color:");
        ImGui.sameLine();
        ImGui.colorEdit3("##newAreaColor", newAreaColor);

        boolean createClicked = ImGui.button("Create Area");

        if (createClicked || nameConfirmed) {
            String name = newAreaName.get().trim();
            if (name.isEmpty()) {
                setStatus("Error: Area name cannot be empty");
            } else if (areaSizeX.get() <= 0 || areaSizeY.get() <= 0 || areaSizeZ.get() <= 0) {
                setStatus("Error: Size values must be positive");
            } else {
                createNewArea(name);
                showNewForm = false;
                newAreaName.set("");
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

    private void renderAreaDetails() {
        if (selectedArea == null) {
            showAreaDetails = false;
            return;
        }

        ImGui.text("Area Details: " + selectedArea.id());
        ImGui.separator();

        ImGui.text("Dimension: " + selectedArea.dimension().identifier());
        ImGui.separator();

        ImGui.text("Position (Min):");
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("X##editMinX", editMinX);
        ImGui.sameLine();
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Y##editMinY", editMinY);
        ImGui.sameLine();
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Z##editMinZ", editMinZ);

        ImGui.text("Position (Max):");
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("X##editMaxX", editMaxX);
        ImGui.sameLine();
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Y##editMaxY", editMaxY);
        ImGui.sameLine();
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Z##editMaxZ", editMaxZ);

        AABB bounds = selectedArea.bounds();
        ImGui.text(String.format("Size: %.1f x %.1f x %.1f",
                bounds.maxX - bounds.minX,
                bounds.maxY - bounds.minY,
                bounds.maxZ - bounds.minZ));

        ImGui.text("Color:");
        ImGui.sameLine();
        ImGui.colorEdit3("##editAreaColor", editColor);

        if (ImGui.button("Save Changes")) {
            saveAreaChanges();
        }

        ImGui.sameLine();
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

    private void populateEditFields(Area area) {
        AABB b = area.bounds();
        editMinX.set((int) Math.floor(b.minX));
        editMinY.set((int) Math.floor(b.minY));
        editMinZ.set((int) Math.floor(b.minZ));
        editMaxX.set((int) Math.ceil(b.maxX));
        editMaxY.set((int) Math.ceil(b.maxY));
        editMaxZ.set((int) Math.ceil(b.maxZ));
        colorToFloats(area.color(), editColor);
    }

    private void saveAreaChanges() {
        if (selectedArea == null) return;

        Vec3 min = new Vec3(editMinX.get(), editMinY.get(), editMinZ.get());
        Vec3 max = new Vec3(editMaxX.get(), editMaxY.get(), editMaxZ.get());

        int color = floatsToColor(editColor);
        Area updatedArea = Area.of(selectedArea.id(), min, max, selectedArea.dimension(), color);

        var packet = AreaPacket.update(updatedArea);
        Common.getNetworkManager().sendToServer(packet);

        setStatus("Area update request sent: " + selectedArea.id());
    }

    private void createNewArea(String name) {
        Minecraft mc = Minecraft.getInstance();

        int sizeX = areaSizeX.get();
        int sizeY = areaSizeY.get();
        int sizeZ = areaSizeZ.get();
        int offsetY = areaOffsetY.get();

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

        int color = floatsToColor(newAreaColor);
        Area newArea = Area.of(name, min, max, mc.level.dimension(), color);

        var packet = AreaPacket.create(newArea);
        Common.getNetworkManager().sendToServer(packet);

        setStatus("Area creation request sent: " + name);
    }

    private void teleportToArea() {
        if (selectedArea == null) return;

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

        var packet = AreaPacket.remove(selectedArea.id(), selectedArea.dimension().identifier());
        Common.getNetworkManager().sendToServer(packet);

        setStatus("Area deletion request sent: " + selectedArea.id());
        showAreaDetails = false;
        selectedArea = null;
    }
}
