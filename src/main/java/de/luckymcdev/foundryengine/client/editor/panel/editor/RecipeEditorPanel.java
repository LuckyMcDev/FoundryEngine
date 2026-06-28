package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.tools.CataloguePanel;
import de.luckymcdev.foundryengine.client.icons.ImageExportUtil;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.ClientConfig;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.util.*;

public class RecipeEditorPanel extends EditorPanel {
    public static final RecipeEditorPanel INSTANCE = new RecipeEditorPanel();

    private static final float TEXTURE_SCALE = 2f;
    private static final float GUI_TEX_WIDTH = 176;
    private static final float GUI_TEX_HEIGHT = 166;
    private static final float SLOT_TEX_SIZE = 18;
    private static final float SLOT_SIZE = SLOT_TEX_SIZE * TEXTURE_SCALE;

    private static final int SLOT_BG = 0xFF6B6B6B;
    private static final int SLOT_BORDER = 0xFF3F3F3F;
    private static final int SLOT_HOVER_BORDER = 0xFFFFFFA0;
    private static final int TEXT_SHADOW = 0x80000000;

    private final Map<String, SlotData> slots = new LinkedHashMap<>();
    private final Map<Identifier, Integer> itemTextureCache = new HashMap<>();
    private final Map<Identifier, ImGraphicsExtractor.Image> bgTextureCache = new HashMap<>();
    private final ImString recipeIdInput = new ImString("modid:recipe_name", 256);
    private final ImString groupInput = new ImString("", 128);
    private RecipeType selectedType = RecipeType.SHAPED;
    private float experience = 0.1f;
    private int cookingTime = 200;

    private RecipeEditorPanel() {
        super(new Builder(Common.id("recipe_editor"), "Recipe Editor")
                .icon(ImIcons.FA.FA_UTENSILS)
                .category(PanelCategory.EDITOR));
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public void content(ImGraphicsExtractor g) {
        renderTypeSelector();
        ImGui.separator();
        renderIdInput();
        ImGui.spacing();
        renderRecipeGrid();
        renderConfigFields();
        ImGui.separator();
        renderCodePreview();
    }

    private void renderTypeSelector() {
        ImGui.text("Recipe Type");
        ImGui.sameLine();
        ImGui.setNextItemWidth(200);
        if (ImGui.beginCombo("##recipe_type", selectedType.label)) {
            for (RecipeType type : RecipeType.values()) {
                boolean selected = type == selectedType;
                if (ImGui.selectable(type.label, selected)) {
                    selectedType = type;
                    slots.clear();
                }
                if (selected) ImGui.setItemDefaultFocus();
            }
            ImGui.endCombo();
        }
        if (!selectedType.hint.isEmpty()) {
            ImGui.sameLine();
            ImGui.textDisabled("(" + selectedType.hint + ")");
        }
    }

    private void renderIdInput() {
        ImGui.text("Recipe ID");
        ImGui.sameLine();
        ImGui.setNextItemWidth(250);
        ImGui.inputText("##recipe_id", recipeIdInput);
    }

    private void renderRecipeGrid() {
        RecipeLayout layout = getLayout();
        ImGraphicsExtractor.Image bg = getBgTexture(layout.tex());

        float availX = ImGui.getContentRegionAvailX();
        float texW = GUI_TEX_WIDTH * TEXTURE_SCALE;
        float texH = GUI_TEX_HEIGHT * TEXTURE_SCALE;
        float startX = ImGui.getCursorScreenPos().x + Math.max(0, (availX - texW) * 0.5f);
        float startY = ImGui.getCursorScreenPos().y;

        var drawList = ImGui.getWindowDrawList();

        if (bg != null && bg.glId() != -1) {
            float u2 = GUI_TEX_WIDTH / (float) bg.width();
            float v2 = GUI_TEX_HEIGHT / (float) bg.height();
            drawList.addImage(bg.glId(), startX, startY, startX + texW, startY + texH, 0, 0, u2, v2);
        } else {
            drawList.addRectFilled(startX, startY, startX + texW, startY + texH, 0xFF2D2D2D);
        }

        for (SlotDef def : layout.inputs()) {
            renderSlot(def.key(), startX + def.texX() * TEXTURE_SCALE, startY + def.texY() * TEXTURE_SCALE, SLOT_SIZE, true);
        }

        if (layout.result() != null) {
            SlotDef def = layout.result();
            renderSlot(def.key(), startX + def.texX() * TEXTURE_SCALE, startY + def.texY() * TEXTURE_SCALE, SLOT_SIZE, false);
        }

        ImGui.dummy(texW, texH);
    }

    private void renderSlot(String key, float screenX, float screenY, float size, boolean input) {
        var drawList = ImGui.getWindowDrawList();

        drawList.addRectFilled(screenX, screenY, screenX + size, screenY + size, SLOT_BG);
        drawList.addRect(screenX, screenY, screenX + size, screenY + size, SLOT_BORDER);

        ImGui.setCursorScreenPos(screenX, screenY);
        ImGui.invisibleButton(key, size, size);

        boolean hovered = ImGui.isItemHovered();

        if (hovered) {
            drawList.addRect(screenX, screenY, screenX + size, screenY + size, SLOT_HOVER_BORDER, 0f, 0, 2f);
            CataloguePanel.acceptDrop(data -> {
                slots.put(key, new SlotData(data.id(), 1));
                loadItemTexture(data.id());
            });

            SlotData data = slots.get(key);
            if (data != null) {
                float wheel = ImGui.getIO().getMouseWheel();
                if (wheel != 0) {
                    int newCount = Math.clamp(data.count + (wheel > 0 ? 1 : -1), 1, 64);
                    slots.put(key, new SlotData(data.item, newCount));
                }
            }
        }

        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
            slots.remove(key);
        }

        SlotData data = slots.get(key);
        if (data != null) {
            Integer texId = itemTextureCache.get(data.item);
            if (texId != null && texId != -1) {
                drawList.addImage(texId, screenX, screenY, screenX + size, screenY + size, 0, 0, 1, 1);
            } else {
                String letter = data.item.getPath().substring(0, 1).toUpperCase();
                float tw = ImGui.calcTextSize(letter).x;
                float tx = screenX + (size - tw) * 0.5f;
                float ty = screenY + (size - ImGui.getFontSize()) * 0.5f;
                drawList.addText(tx + 1, ty + 1, TEXT_SHADOW, letter);
                drawList.addText(tx, ty, 0xFFFFFFFF, letter);
            }

            if (data.count > 1) {
                String countStr = String.valueOf(data.count);
                float cw = ImGui.calcTextSize(countStr).x;
                drawList.addText(screenX + size - cw - 3, screenY + size - ImGui.getFontSize() - 2, TEXT_SHADOW, countStr);
                drawList.addText(screenX + size - cw - 4, screenY + size - ImGui.getFontSize() - 3, 0xFFFFFFFF, countStr);
            }

            if (hovered) ImGui.setTooltip(data.item + " x" + data.count);
        } else {
            if (hovered)
                ImGui.setTooltip("Drop an item here" + (input ? ", scroll to change count" : "") + ", right-click to clear");
        }
    }

    private void renderConfigFields() {
        if (selectedType.isCooking()) {
            ImGui.spacing();
            ImGui.text("Experience");
            ImGui.sameLine();
            ImGui.setNextItemWidth(120);
            var expArr = new float[]{experience};
            if (ImGui.dragFloat("##experience", expArr, 0.01f, 0, 100, "%.2f")) {
                experience = Math.max(0, expArr[0]);
            }
            ImGui.sameLine();
            ImGui.text("Cooking Time (ticks)");
            ImGui.sameLine();
            ImGui.setNextItemWidth(100);
            var timeArr = new int[]{cookingTime};
            if (ImGui.dragInt("##cooking_time", timeArr, 1, 1, 72000)) {
                cookingTime = Math.max(1, timeArr[0]);
            }
        }

        ImGui.spacing();
        ImGui.text("Group");
        ImGui.sameLine();
        ImGui.setNextItemWidth(200);
        ImGui.inputText("##group", groupInput);
    }

    private void renderCodePreview() {
        ImGui.text("Generated Groovy Code");
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_CLIPBOARD + " Copy Code")) {
            ImGui.setClipboardText(generateCode());
            setStatus("Code copied to clipboard!");
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_REFRESH + " Regenerate")) {
            setStatus("Code regenerated");
        }

        String code = generateCode();
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 8, 8);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0xFF1E1E1E);
        ImGui.inputTextMultiline("##code_preview", new ImString(code, code.length() + 1024),
                ImGui.getContentRegionAvailX(), Math.min(300, ImGui.getContentRegionAvailY()),
                ImGuiInputTextFlags.ReadOnly);
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }

    private String generateCode() {
        String recipeId = recipeIdInput.get().trim();
        if (recipeId.isEmpty()) recipeId = "modid:recipe_name";

        String[] parts = recipeId.split(":", 2);
        String ns = parts.length > 1 ? parts[0] : "modid";
        String path = parts.length > 1 ? parts[1] : recipeId;

        CodeBuilder cb = new CodeBuilder(ns, path);
        SlotData result = slots.get("result");

        switch (selectedType) {
            case SHAPED -> buildShaped(cb, result);
            case SHAPELESS -> buildShapeless(cb, result);
            case SMELTING -> buildCooking(cb, result, "smelting");
            case BLASTING -> buildCooking(cb, result, "blasting");
            case SMOKING -> buildCooking(cb, result, "smoking");
            case CAMPFIRE_COOKING -> buildCooking(cb, result, "campfireCooking");
            case STONECUTTING -> buildStonecutting(cb, result);
            case SMITHING_TRANSFORM -> buildSmithing(cb, result, "smithingTransform");
            case SMITHING_TRIM -> buildSmithingTrim(cb);
        }

        return cb.toString();
    }

    private void buildShaped(CodeBuilder cb, SlotData result) {
        cb.header("Shaped").open("shaped", result);

        Map<Identifier, Character> itemToChar = new LinkedHashMap<>();
        char next = 'A';

        for (int row = 0; row < 3; row++) {
            StringBuilder row3 = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                SlotData d = slots.get(row + "," + col);
                if (d != null && d.item != null) {
                    if (!itemToChar.containsKey(d.item)) itemToChar.put(d.item, next++);
                    row3.append(itemToChar.get(d.item));
                } else {
                    row3.append(' ');
                }
            }
            cb.chain("pattern(\"" + row3 + "\")");
        }

        itemToChar.forEach((item, c) -> cb.chain("define('" + c + "', " + itemRef(item.toString()) + ")"));

        cb.countIfNeeded(result).groupIfNeeded(groupInput.get()).end();
    }

    private void buildShapeless(CodeBuilder cb, SlotData result) {
        cb.header("Shapeless").open("shapeless", result);

        for (int i = 0; i < 9; i++) {
            SlotData d = slots.get(String.valueOf(i));
            if (d != null && d.item != null) {
                String arg = itemRef(d.item.toString()) + (d.count() > 1 ? ", " + d.count() : "");
                cb.chain("requires(" + arg + ")");
            }
        }

        cb.countIfNeeded(result).groupIfNeeded(groupInput.get()).end();
    }

    private void buildCooking(CodeBuilder cb, SlotData result, String method) {
        cb.header(capitalize(method)).open(method, result);
        slotItem("input").ifPresent(item -> cb.chain("ingredient(" + itemRef(item) + ")"));
        if (experience > 0) cb.chain("experience(" + String.format(Locale.US, "%.2ff", experience) + ")");
        cb.chain("cookingTime(" + cookingTime + ")");
        cb.countIfNeeded(result).groupIfNeeded(groupInput.get()).end();
    }

    private void buildStonecutting(CodeBuilder cb, SlotData result) {
        cb.header("Stonecutting").open("stonecutting", result);
        slotItem("input").ifPresent(item -> cb.chain("ingredient(" + itemRef(item) + ")"));
        cb.countIfNeeded(result).groupIfNeeded(groupInput.get()).end();
    }

    private void buildSmithing(CodeBuilder cb, SlotData result, String method) {
        cb.header("Smithing").open(method, result);
        slotItem("template").ifPresent(item -> cb.chain("template(" + itemRef(item) + ")"));
        slotItem("base").ifPresent(item -> cb.chain("base(" + itemRef(item) + ")"));
        slotItem("addition").ifPresent(item -> cb.chain("addition(" + itemRef(item) + ")"));
        cb.countIfNeeded(result).end();
    }

    private void buildSmithingTrim(CodeBuilder cb) {
        cb.header("Smithing Trim");
        cb.sb.append("RecipeBuilder.smithingTrim(id(\"").append(cb.ns).append("\", \"").append(cb.path).append("\"))\n");
        slotItem("template").ifPresent(item -> cb.chain("template(" + itemRef(item) + ")"));
        slotItem("base").ifPresent(item -> cb.chain("base(" + itemRef(item) + ")"));
        slotItem("addition").ifPresent(item -> cb.chain("addition(" + itemRef(item) + ")"));
        cb.end();
    }

    private Optional<String> slotItem(String key) {
        SlotData d = slots.get(key);
        return (d != null && d.item != null) ? Optional.of(d.item.toString()) : Optional.empty();
    }

    private String itemRef(String id) {
        if (id.startsWith("minecraft:")) {
            return "Items." + id.substring("minecraft:".length()).toUpperCase().replace('/', '_').replace('-', '_');
        }
        return "item(\"" + id + "\")";
    }

    private RecipeLayout getLayout() {
        return switch (selectedType) {
            case SHAPED, SHAPELESS -> selectedType == RecipeType.SHAPED ? getShapedLayout() : getShapelessLayout();
            case SMELTING, BLASTING, SMOKING -> getFurnaceLayout();
            case CAMPFIRE_COOKING -> getCampfireLayout();
            case STONECUTTING -> getStonecutterLayout();
            case SMITHING_TRANSFORM, SMITHING_TRIM -> getSmithingLayout();
        };
    }

    private RecipeLayout getShapedLayout() {
        List<SlotDef> inputs = new ArrayList<>();
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                inputs.add(new SlotDef(row + "," + col, row + "," + col,
                        29 + col * SLOT_TEX_SIZE, 16 + row * SLOT_TEX_SIZE));
        return new RecipeLayout(RecipeType.SHAPED.texture, inputs, new SlotDef("result", "Result", 123, 34));
    }

    private RecipeLayout getShapelessLayout() {
        List<SlotDef> inputs = new ArrayList<>();
        for (int i = 0; i < 9; i++)
            inputs.add(new SlotDef(String.valueOf(i), String.valueOf(i),
                    29 + (i % 3) * SLOT_TEX_SIZE, 16 + (i / 3) * SLOT_TEX_SIZE));
        return new RecipeLayout(RecipeType.SHAPELESS.texture, inputs, new SlotDef("result", "Result", 123, 34));
    }

    private RecipeLayout getFurnaceLayout() {
        return new RecipeLayout(RecipeType.SMELTING.texture, List.of(
                new SlotDef("input", "Input", 55, 16)),
                new SlotDef("result", "Result", 115, 34));
    }

    private RecipeLayout getCampfireLayout() {
        List<SlotDef> inputs = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            inputs.add(new SlotDef(String.valueOf(i), String.valueOf(i), 43 + i * SLOT_TEX_SIZE, 34));
        return new RecipeLayout(RecipeType.SHAPED.texture, inputs, null);
    }

    private RecipeLayout getStonecutterLayout() {
        return new RecipeLayout(RecipeType.STONECUTTING.texture,
                List.of(new SlotDef("input", "Input", 19, 32)),
                new SlotDef("result", "Result", 142, 32));
    }

    private RecipeLayout getSmithingLayout() {
        return new RecipeLayout(RecipeType.SMITHING_TRANSFORM.texture, List.of(
                new SlotDef("template", "Template", 7, 47),
                new SlotDef("base", "Base", 26, 47),
                new SlotDef("addition", "Addition", 45, 47)),
                new SlotDef("result", "Result", 133, 47));
    }

    private ImGraphicsExtractor.Image getBgTexture(Identifier texId) {
        return bgTextureCache.computeIfAbsent(texId, id -> {
            try {
                return ImGraphicsExtractor.getTexture(id);
            } catch (Exception e) {
                return new ImGraphicsExtractor.Image(-1, 0, 0);
            }
        });
    }

    private void loadItemTexture(Identifier itemId) {
        if (itemTextureCache.containsKey(itemId)) return;

        File iconDir = Common.CACHE.resolve("icons")
                .resolve(String.valueOf(ClientConfig.ICON_SIZE.get()))
                .resolve(itemId.getNamespace())
                .toFile();

        if (iconDir.exists()) {
            String prefix = ImageExportUtil.sanitizeFilename(itemId.getPath());
            File[] matches = iconDir.listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(".png"));
            if (matches != null && matches.length > 0) {
                ImGraphicsExtractor.Image img = ImGraphicsExtractor.getTexture(matches[0]);
                if (img.glId() > 0) {
                    itemTextureCache.put(itemId, img.glId());
                    return;
                }
            }
        }

        itemTextureCache.put(itemId, -1);
    }

    private enum RecipeType {
        SHAPED("Shaped", "minecraft:textures/gui/container/crafting_table.png", "3x3 Crafting"),
        SHAPELESS("Shapeless", "minecraft:textures/gui/container/crafting_table.png", "Up to 9 ingredients"),
        SMELTING("Smelting", "minecraft:textures/gui/container/furnace.png", ""),
        BLASTING("Blasting", "minecraft:textures/gui/container/blast_furnace.png", ""),
        SMOKING("Smoking", "minecraft:textures/gui/container/smoker.png", ""),
        CAMPFIRE_COOKING("Campfire", "minecraft:textures/gui/container/crafting_table.png", ""),
        STONECUTTING("Stonecutting", "minecraft:textures/gui/container/stonecutter.png", ""),
        SMITHING_TRANSFORM("Smithing Transform", "minecraft:textures/gui/container/smithing.png", ""),
        SMITHING_TRIM("Smithing Trim", "minecraft:textures/gui/container/smithing.png", "");

        final String label;
        final Identifier texture;
        final String hint;

        RecipeType(String label, String texture, String hint) {
            this.label = label;
            this.texture = Identifier.parse(texture);
            this.hint = hint;
        }

        boolean isCooking() {
            return this == SMELTING || this == BLASTING || this == SMOKING || this == CAMPFIRE_COOKING;
        }

        boolean isSmithing() {
            return this == SMITHING_TRANSFORM || this == SMITHING_TRIM;
        }
    }

    record SlotData(Identifier item, int count) {
    }

    record SlotDef(String key, String label, float texX, float texY) {
    }

    record RecipeLayout(Identifier tex, List<SlotDef> inputs, SlotDef result) {
    }

    private class CodeBuilder {
        final StringBuilder sb = new StringBuilder();
        final String ns, path;

        CodeBuilder(String ns, String path) {
            this.ns = ns;
            this.path = path;
        }

        CodeBuilder header(String type) {
            sb.append("// ").append(type).append(" Recipe: ").append(ns).append(":").append(path).append("\n");
            return this;
        }

        CodeBuilder open(String method, SlotData result) {
            sb.append("RecipeBuilder.").append(method)
                    .append("(Identifier.fromNamespaceAndPath(\"").append(ns).append("\", \"").append(path).append("\"), ");
            if (result != null && result.item != null) {
                sb.append(itemRef(result.item.toString()));
            } else {
                sb.append("Items.STONE // TODO: set result item");
            }
            sb.append(")\n");
            return this;
        }

        CodeBuilder chain(String call) {
            sb.append("    .").append(call).append("\n");
            return this;
        }

        CodeBuilder countIfNeeded(SlotData result) {
            if (result != null && result.count() > 1) chain("count(" + result.count() + ")");
            return this;
        }

        CodeBuilder groupIfNeeded(String group) {
            if (group != null && !group.trim().isEmpty()) chain("group(\"" + group.trim() + "\")");
            return this;
        }

        CodeBuilder end() {
            sb.append("    .build()");
            return this;
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}