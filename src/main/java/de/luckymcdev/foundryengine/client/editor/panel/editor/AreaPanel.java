package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.AABBArea;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import de.luckymcdev.foundryengine.common.area.BlockArea;
import de.luckymcdev.foundryengine.common.network.packets.editor.AreaPacket;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiMouseCursor;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class AreaPanel extends EditorPanel {
    public static final AreaPanel INSTANCE = new AreaPanel();

    private static final float LEFT_PANEL_WIDTH = 240f;
    private static final float CREATE_FORM_MIN_HEIGHT = 90f;
    private static final float CREATE_FORM_MAX_HEIGHT = 500f;
    private static final float SPLITTER_HEIGHT = 6f;
    private final ImString newAreaName = new ImString(64);
    private final ImInt areaSizeX = new ImInt(5);
    private final ImInt areaSizeY = new ImInt(4);
    private final ImInt areaSizeZ = new ImInt(5);
    private final ImInt areaOffsetY = new ImInt(0);
    private final ImBoolean showCreateForm = new ImBoolean(false);
    private final ImBoolean creatingBlockArea = new ImBoolean(false);
    private float[] newAreaColor;
    private int selectedIndex = -1;
    private float createFormHeight = 240f;

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

        if (showCreateForm.get()) {
            renderCreateForm();
            ImGui.separator();
        }

        beginContent();
        List<Area> areas = getAreas();
        renderLeftPanel(areas);
        ImGui.sameLine();
        renderRightPanel(areas);
        endContent();
    }

    private void renderMenuBar() {
        menuBar(() -> {
            if (ImGui.menuItem(ImIcons.FA.FA_PLUS + " New")) {
                resetCreateForm();
                showCreateForm.set(true);
            }
            if (ImGui.menuItem(ImIcons.FA.FA_ARROW_ROTATE_RIGHT + " Sync")) {
                Common.getNetworkManager().sendToServer(AreaPacket.requestSync());
                setStatus("Sync requested.");
            }
        });
    }

    private void resetCreateForm() {
        newAreaName.set("");
        areaSizeX.set(5);
        areaSizeY.set(4);
        areaSizeZ.set(5);
        areaOffsetY.set(0);
        creatingBlockArea.set(false);
        newAreaColor = Area.DEFAULT_COLOR.toFloatArray();
    }

    private void renderCreateForm() {
        ImGui.beginChild("##create_area_form", 0, createFormHeight, true);

        ImGui.textColored(0.6f, 0.85f, 1.0f, 1.0f, ImIcons.FA.FA_PLUS + " Create New Area");
        ImGui.spacing();

        ImGui.setNextItemWidth(-1);
        ImGui.inputTextWithHint("##newname", "area_name (or namespace:path)", newAreaName,
                imgui.flag.ImGuiInputTextFlags.EnterReturnsTrue);

        ImGui.checkbox("Single Block", creatingBlockArea);
        ImGui.sameLine();
        ImGui.textDisabled("(?)");
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Creates a single-block area at your feet instead of a box area.");
        }

        if (!creatingBlockArea.get()) {
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
        }

        ImGui.colorEdit3("Color##newAreaColor", newAreaColor);

        ImGui.spacing();
        boolean validInput = !newAreaName.get().trim().isBlank()
                && (creatingBlockArea.get() || (areaSizeX.get() > 0 && areaSizeY.get() > 0 && areaSizeZ.get() > 0));

        ImGui.beginDisabled(!validInput);
        boolean createClicked = ImGui.button("Create", 120, 0);
        ImGui.endDisabled();

        if (createClicked && validInput) {
            createArea(newAreaName.get().trim());
            showCreateForm.set(false);
        }

        ImGui.sameLine();
        if (ImGui.button("Cancel", 120, 0)) {
            showCreateForm.set(false);
        }

        ImGui.endChild();

        renderCreateFormSplitter();
    }

    private void renderCreateFormSplitter() {
        ImGui.pushStyleColor(ImGuiCol.Button, 0.0f, 0.0f, 0.0f, 0.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.15f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.25f);

        ImGui.button("##create_form_splitter", -1, SPLITTER_HEIGHT);

        if (ImGui.isItemHovered() || ImGui.isItemActive()) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeNS);
        }
        if (ImGui.isItemActive()) {
            float deltaY = ImGui.getIO().getMouseDeltaY();
            createFormHeight += deltaY;
            if (createFormHeight < CREATE_FORM_MIN_HEIGHT) createFormHeight = CREATE_FORM_MIN_HEIGHT;
            if (createFormHeight > CREATE_FORM_MAX_HEIGHT) createFormHeight = CREATE_FORM_MAX_HEIGHT;
        }

        ImGui.popStyleColor(3);
    }

    private void renderLeftPanel(List<Area> areas) {
        ImGui.beginChild("##area_list", LEFT_PANEL_WIDTH, 0, true);

        if (areas.isEmpty()) {
            ImGui.textDisabled("No areas.");
        } else {
            float availWidth = ImGui.getContentRegionAvailX();

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
                // Stretch the selectable to fill remaining width so long IDs
                // get full click area and aren't visually clipped by sameLine().
                float selectableWidth = ImGui.getContentRegionAvailX();
                if (ImGui.selectable(label, sel, 0, selectableWidth, 0)) {
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

        ImGui.textWrapped(area.id().toString());
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
                ImGui.textWrapped(entry.getKey() + " -> " + entry.getValue());
            }
            ImGui.unindent();
            ImGui.spacing();
        }

        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.15f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.08f, 0.08f, 1.0f);
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

        Color color = new Color(newAreaColor);
        int colon = name.indexOf(':');
        Identifier id = colon >= 0
                ? Identifier.fromNamespaceAndPath(name.substring(0, colon), name.substring(colon + 1))
                : Common.id(name);

        Area area;
        if (creatingBlockArea.get()) {
            var playerPos = mc.player.blockPosition();
            int offsetY = areaOffsetY.get();
            area = BlockArea.of(id, new BlockPos(playerPos.getX(), playerPos.getY() + offsetY, playerPos.getZ()), mc.level.dimension(), color);
        } else {
            int sizeX = areaSizeX.get();
            int sizeY = areaSizeY.get();
            int sizeZ = areaSizeZ.get();
            int offsetY = areaOffsetY.get();

            var playerPos = mc.player.position();
            double centerY = playerPos.y + offsetY;

            var min = new Vec3(
                    playerPos.x - sizeX / 2.0,
                    centerY - sizeY / 2.0,
                    playerPos.z - sizeZ / 2.0
            );
            var max = new Vec3(
                    playerPos.x + sizeX / 2.0,
                    centerY + sizeY / 2.0,
                    playerPos.z + sizeZ / 2.0
            );

            area = AABBArea.of(id, min, max, mc.level.dimension(), color);
        }

        Common.getNetworkManager().sendToServer(AreaPacket.create(area));
        setStatus("Area creation request sent: " + id);
    }
}