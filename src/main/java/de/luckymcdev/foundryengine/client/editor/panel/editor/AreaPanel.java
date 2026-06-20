package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import de.luckymcdev.foundryengine.common.network.packets.editor.AreaPacket;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public class AreaPanel extends EditorPanel {
    public static final AreaPanel INSTANCE = new AreaPanel();

    private final ImString newAreaName = new ImString(64);
    private final ImInt areaSizeX = new ImInt(5);
    private final ImInt areaSizeY = new ImInt(4);
    private final ImInt areaSizeZ = new ImInt(5);
    private final ImInt areaOffsetY = new ImInt(0);
    private float[] newAreaColor;

    private int selectedIndex = -1;

    private AreaPanel() {
        super(new Builder(Common.id("area_panel"), "Areas")
                .icon(ImIcons.FA.FA_MAP)
                .category(PanelCategory.EDITOR)
                .menuBar(true));
        newAreaColor = Area.DEFAULT_COLOR.toFloatArray();
    }

    @Override
    public void content() {
        if (!requireWorld("You need to join a world to manage areas.")) {
            return;
        }

        renderMenuBar();
        ImGui.separator();

        beginContent();
        List<Area> areas = getAreas();
        renderLeftPanel(areas);
        ImGui.sameLine();
        renderRightPanel(areas);
        endContent();

        renderNewAreaModal();
    }

    private void renderMenuBar() {
        menuBar(() -> {
            if (ImGui.menuItem(ImIcons.FA.FA_PLUS + " New")) {
                newAreaName.set("");
                areaSizeX.set(5);
                areaSizeY.set(4);
                areaSizeZ.set(5);
                areaOffsetY.set(0);
                newAreaColor = Area.DEFAULT_COLOR.toFloatArray();
                ImGui.openPopup("Create Area");
            }
            if (ImGui.menuItem(ImIcons.FA.FA_ARROW_ROTATE_RIGHT + " Sync")) {
                Common.getNetworkManager().sendToServer(AreaPacket.requestSync());
                setStatus("Sync requested.");
            }
        });
    }

    private void renderNewAreaModal() {
        if (ImGui.beginPopupModal("Create Area", null)) {
            ImGui.setNextItemWidth(-1);
            boolean confirm = ImGui.inputTextWithHint("##newname", "area_name (or namespace:path)", newAreaName,
                    imgui.flag.ImGuiInputTextFlags.EnterReturnsTrue);

            ImGui.text("Size");
            ImGui.setNextItemWidth(80);
            ImGui.inputInt("X##sizeX", areaSizeX);
            ImGui.sameLine();
            ImGui.setNextItemWidth(80);
            ImGui.inputInt("Y##sizeY", areaSizeY);
            ImGui.sameLine();
            ImGui.setNextItemWidth(80);
            ImGui.inputInt("Z##sizeZ", areaSizeZ);

            ImGui.setNextItemWidth(80);
            ImGui.inputInt("Y-offset##offsetY", areaOffsetY);

            ImGui.colorEdit3("Color##newAreaColor", newAreaColor);

            ImGui.spacing();
            if (ImGui.button("Create", 120, 0) || confirm) {
                String name = newAreaName.get().trim();
                if (!name.isBlank() && areaSizeX.get() > 0 && areaSizeY.get() > 0 && areaSizeZ.get() > 0) {
                    createArea(name);
                    newAreaName.set("");
                    ImGui.closeCurrentPopup();
                }
            }
            ImGui.sameLine();
            if (ImGui.button("Cancel", 120, 0)) {
                newAreaName.set("");
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void renderLeftPanel(List<Area> areas) {
        ImGui.beginChild("##area_list", 180f, 0, true);

        if (areas.isEmpty()) {
            ImGui.textDisabled("No areas.");
        } else {
            for (int i = 0; i < areas.size(); i++) {
                Area a = areas.get(i);
                boolean sel = (i == selectedIndex);

                ImGui.pushID(i);
                Color color = a.color();
                ImGui.colorButton("##area_color",
                        color.r(), color.g(), color.b(), color.a(),
                        ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoDragDrop, 12, 12);
                ImGui.sameLine();

                String label = a.id() + "##area_" + i;
                if (ImGui.selectable(label, sel)) {
                    selectedIndex = sel ? -1 : i;
                }
                ImGui.popID();
            }
        }

        ImGui.endChild();
    }

    private void renderRightPanel(List<Area> areas) {
        ImGui.beginChild("##area_detail", 0, 0, false);

        if (selectedIndex < 0 || selectedIndex >= areas.size()) {
            ImGui.textDisabled("Select an area on the left.");
            ImGui.endChild();
            return;
        }

        Area area = areas.get(selectedIndex);

        ImGui.text(area.id().toString());
        ImGui.separator();

        ImGui.text("Dimension: " + area.dimension().identifier());
        ImGui.spacing();

        var b = area.bounds();
        ImGui.text(String.format("Min: (%.0f, %.0f, %.0f)", b.minX, b.minY, b.minZ));
        ImGui.text(String.format("Max: (%.0f, %.0f, %.0f)", b.maxX, b.maxY, b.maxZ));
        ImGui.text(String.format("Size: %.0f x %.0f x %.0f",
                b.maxX - b.minX, b.maxY - b.minY, b.maxZ - b.minZ));

        ImGui.spacing();
        Color color = area.color();
        float[] col = {color.r(), color.g(), color.b(), color.a()};
        ImGui.colorEdit4("##area_detail_color", col, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoPicker);
        ImGui.sameLine();
        ImGui.text("Color");

        ImGui.spacing();

        Map<String, Identifier> links = area.linkedAreas();
        if (!links.isEmpty()) {
            ImGui.text("Linked Areas:");
            ImGui.indent();
            for (var entry : links.entrySet()) {
                ImGui.text(entry.getKey() + " -> " + entry.getValue());
            }
            ImGui.unindent();
            ImGui.spacing();
        }

        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered, 0.70f, 0.15f, 0.15f, 1.0f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive, 0.45f, 0.08f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Delete " + area.id())) {
            Common.getNetworkManager().sendToServer(AreaPacket.remove(area.id()));
            selectedIndex = -1;
            setStatus("Deleted: " + area.id());
        }
        ImGui.popStyleColor(3);

        ImGui.endChild();
    }

    private List<Area> getAreas() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return List.of();
        AreaManager manager = Common.getAreaManager();
        return manager.getAreasForDimension(mc.level.dimension());
    }

    private void createArea(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int sizeX = areaSizeX.get();
        int sizeY = areaSizeY.get();
        int sizeZ = areaSizeZ.get();
        int offsetY = areaOffsetY.get();

        var playerPos = mc.player.position();
        double centerY = playerPos.y + offsetY;

        var min = new net.minecraft.world.phys.Vec3(
                playerPos.x - sizeX / 2.0,
                centerY - sizeY / 2.0,
                playerPos.z - sizeZ / 2.0
        );
        var max = new net.minecraft.world.phys.Vec3(
                playerPos.x + sizeX / 2.0,
                centerY + sizeY / 2.0,
                playerPos.z + sizeZ / 2.0
        );

        Color color = new Color(newAreaColor);
        int colon = name.indexOf(':');
        Identifier id = colon >= 0
                ? Identifier.fromNamespaceAndPath(name.substring(0, colon), name.substring(colon + 1))
                : Common.id(name);
        Area area = Area.of(id, min, max, mc.level.dimension(), color);
        Common.getNetworkManager().sendToServer(AreaPacket.create(area));
        setStatus("Area creation request sent: " + id);
    }
}
