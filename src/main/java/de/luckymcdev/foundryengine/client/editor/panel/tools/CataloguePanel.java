package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.icons.ImageExportUtil;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.reflect.ClassInspector;
import de.luckymcdev.foundryengine.config.ClientConfig;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class CataloguePanel extends EditorPanel {
    public static final CataloguePanel INSTANCE = new CataloguePanel();
    private static final int MAX_LOADS_PER_FRAME = 25;
    private static final float ITEM_SIZE = 64f;
    private static final Identifier NAMETAG_ID = Identifier.parse("minecraft:name_tag");
    private static final Identifier BUCKET_ID = Identifier.parse("minecraft:water_bucket");
    private static final Identifier SPAWNER_ID = Identifier.parse("minecraft:spawner");
    private static final Identifier CRAFTING_TABLE_ID = Identifier.parse("minecraft:crafting_table");
    private final Map<Identifier, Integer> textureCache = new HashMap<>();
    private final Set<Identifier> failedLoads = new HashSet<>();
    private final Queue<Identifier> loadQueue = new ArrayDeque<>();
    private final Set<Identifier> queued = new HashSet<>();
    private final ImString searchBuffer = new ImString(256);

    public CataloguePanel() {
        super(Common.id("catalogue"), "Catalogue", ImIcons.FA.FA_LIST, PanelCategory.TOOLS);
    }

    public static void acceptDrop(Consumer<CataloguePayload> callback) {
        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("CATALOGUE_ENTRY");
            if (payload instanceof CataloguePayload data) {
                callback.accept(data);
            }
            ImGui.endDragDropTarget();
        }
    }

    public static void acceptClassMemberDrop(Consumer<ClassMemberPayload> callback) {
        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("CLASS_MEMBER");
            if (payload instanceof ClassMemberPayload data) {
                callback.accept(data);
            }
            ImGui.endDragDropTarget();
        }
    }

    @Override
    public void content() {
        processTextureQueue();
        renderSearchHeader();

        if (ImGui.beginTabBar("CatalogueTabs")) {

            if (ImGui.beginTabItem(ImIcons.FA.FA_BOX + " Items")) {
                var list = BuiltInRegistries.ITEM.keySet().stream()
                        .sorted(Comparator.comparing(Identifier::getPath)).toList();
                renderRegistryGrid("items", list, id -> id, false, id -> {
                    if (Minecraft.getInstance().level == null) return;
                    Minecraft.getInstance().player.connection.sendCommand("give @s " + id);
                });
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem(ImIcons.FA.FA_CUBE + " Blocks")) {
                List<Identifier> blockItems = BuiltInRegistries.BLOCK.stream()
                        .map(block -> BuiltInRegistries.ITEM.getKey(block.asItem()))
                        .distinct()
                        .sorted(Comparator.comparing(Identifier::getPath)).toList();
                renderRegistryGrid("blocks", blockItems, id -> id, false, id -> {
                });
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem(ImIcons.FA.FA_PAW + " Entities")) {
                var list = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                        .sorted(Comparator.comparing(Identifier::getPath)).toList();

                renderRegistryGrid("entities", list,
                        entityId -> {
                            var optType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
                            if (optType.isEmpty()) return SPAWNER_ID;
                            var eggHolder = SpawnEggItem.byId(optType.get());
                            return eggHolder.map(itemHolder -> BuiltInRegistries.ITEM.getKey(itemHolder.value())).orElse(SPAWNER_ID);
                        },
                        false,
                        id -> {
                            if (Minecraft.getInstance().level == null) return;
                            var pos = Minecraft.getInstance().player.position();
                            Minecraft.getInstance().player.connection.sendCommand(
                                    String.format("summon %s %.2f %.2f %.2f", id, pos.x, pos.y, pos.z).replace(",", ".")
                            );
                        });
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem(ImIcons.FA.FA_DROPLET + " Fluids")) {
                var list = BuiltInRegistries.FLUID.keySet().stream()
                        .sorted(Comparator.comparing(Identifier::getPath)).toList();

                renderRegistryGrid("fluids", list,
                        fluidId -> {
                            String path = fluidId.getPath();
                            String namespace = fluidId.getNamespace();

                            if (fluidId.equals(Identifier.parse("minecraft:empty"))) {
                                return Identifier.withDefaultNamespace("bucket");
                            }

                            if (path.contains("flowing")) {
                                return fluidId;
                            }

                            Identifier bucketId = Identifier.fromNamespaceAndPath(namespace, path + "_bucket");
                            if (BuiltInRegistries.ITEM.containsKey(bucketId)) {
                                return bucketId;
                            }

                            return BUCKET_ID;
                        },
                        false,
                        id -> {
                        }
                );
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem(ImIcons.FA.FA_TAG + " Tags")) {
                List<Identifier> allTagIds = BuiltInRegistries.REGISTRY.entrySet().stream()
                        .flatMap(entry -> {
                            var tags = entry.getValue().getTags().toList();
                            if (tags.isEmpty()) return java.util.stream.Stream.empty();
                            return tags.stream().map(t -> t.key().location());
                        })
                        .sorted(Comparator.comparing(Identifier::getPath))
                        .distinct()
                        .toList();
                renderRegistryGrid("tags", allTagIds, id -> NAMETAG_ID, true, id -> {
                });
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem(ImIcons.FA.FA_BOOK + " Recipes")) {
                RecipeManager recipeManager = Client.getRecipeManager();
                if (recipeManager == null) {
                    ImGui.textDisabled("No recipe manager available (join a world)");
                } else {
                    List<Identifier> recipeIds = new ArrayList<>();
                    for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
                        recipeIds.add(holder.id().identifier());
                    }
                    recipeIds.sort(Comparator.comparing(Identifier::getPath));

                    renderRegistryGrid("recipes", recipeIds,
                            recipeId -> CRAFTING_TABLE_ID,
                            false,
                            recipeId -> {
                                if (Client.getPlayer() != null) {
                                    Client.getPlayer().sendSystemMessage(
                                            Component.literal("Recipe: " + recipeId)
                                    );
                                }
                            }
                    );
                }
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem(ImIcons.FA.FA_CODE + " Classes")) {
                renderClassesTab();
                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }
    }

    private void renderClassesTab() {
        String filter = searchBuffer.get().toLowerCase();
        boolean hasFilter = !filter.isEmpty();

        ImGui.beginChild("##class_browser", 0, 0, false);
        try {
            for (Class<?> clazz : ClassInspector.getCommonClasses()) {
                String className = clazz.getSimpleName();
                String fullName = clazz.getName();
                ClassInspector.ClassInfo info = ClassInspector.inspect(clazz);

                boolean classMatches = className.toLowerCase().contains(filter)
                        || fullName.toLowerCase().contains(filter);
                boolean anyMethodMatches = false;
                boolean anyFieldMatches = false;

                if (!classMatches && hasFilter) {
                    for (var m : info.methods()) {
                        if (m.name().toLowerCase().contains(filter)
                                || m.displayName().toLowerCase().contains(filter)) {
                            anyMethodMatches = true;
                            break;
                        }
                    }
                    if (!anyMethodMatches) {
                        for (var f : info.fields()) {
                            if (f.name().toLowerCase().contains(filter)
                                    || f.displayName().toLowerCase().contains(filter)) {
                                anyFieldMatches = true;
                                break;
                            }
                        }
                    }
                }

                if (hasFilter && !classMatches && !anyMethodMatches && !anyFieldMatches) continue;

                ImGui.pushID(fullName);

                int flags = hasFilter && !classMatches ? ImGuiTreeNodeFlags.DefaultOpen : ImGuiTreeNodeFlags.None;
                boolean open = ImGui.treeNodeEx(className, flags);
                if (ImGui.isItemHovered()) ImGui.setTooltip(fullName);

                if (open) {
                    if (info.methods().length > 0) {
                        int mFlags = hasFilter ? ImGuiTreeNodeFlags.DefaultOpen : ImGuiTreeNodeFlags.None;
                        if (ImGui.treeNodeEx("Methods (" + info.methods().length + ")##" + fullName, mFlags)) {
                            for (var method : info.methods()) {
                                if (hasFilter && !classMatches && !method.name().toLowerCase().contains(filter)
                                        && !method.displayName().toLowerCase().contains(filter)) continue;

                                ClassMemberPayload payload = new ClassMemberPayload(
                                        fullName, method.name(), "method",
                                        method.paramTypes(), method.returnType()
                                );
                                String label = method.displayName();
                                ImGui.pushID(label + method.name());

                                if (ImGui.selectable(label)) {
                                }
                                if (ImGui.beginDragDropSource()) {
                                    ImGui.setDragDropPayload("CLASS_MEMBER", payload);
                                    ImGui.text("Method: " + className + "." + label);
                                    ImGui.endDragDropSource();
                                }
                                if (ImGui.isItemHovered()) {
                                    String ret = method.returnType().contains(".")
                                            ? method.returnType().substring(method.returnType().lastIndexOf('.') + 1)
                                            : method.returnType();
                                    ImGui.setTooltip("Returns: " + ret);
                                }
                                ImGui.popID();
                            }
                            ImGui.treePop();
                        }
                    }

                    if (info.fields().length > 0) {
                        int fFlags = hasFilter ? ImGuiTreeNodeFlags.DefaultOpen : ImGuiTreeNodeFlags.None;
                        if (ImGui.treeNodeEx("Fields (" + info.fields().length + ")##" + fullName, fFlags)) {
                            for (var field : info.fields()) {
                                if (hasFilter && !classMatches && !field.name().toLowerCase().contains(filter)
                                        && !field.displayName().toLowerCase().contains(filter)) continue;

                                ClassMemberPayload payload = new ClassMemberPayload(
                                        fullName, field.name(), "field",
                                        new String[0], field.type()
                                );
                                String label = field.displayName();
                                ImGui.pushID(label);

                                if (ImGui.selectable(label)) {
                                }
                                if (ImGui.beginDragDropSource()) {
                                    ImGui.setDragDropPayload("CLASS_MEMBER", payload);
                                    ImGui.text("Field: " + className + "." + field.name());
                                    ImGui.endDragDropSource();
                                }
                                if (ImGui.isItemHovered()) {
                                    String ft = field.type().contains(".")
                                            ? field.type().substring(field.type().lastIndexOf('.') + 1)
                                            : field.type();
                                    ImGui.setTooltip("Type: " + ft);
                                }
                                ImGui.popID();
                            }
                            ImGui.treePop();
                        }
                    }

                    ImGui.treePop();
                }

                ImGui.popID();
            }
        } finally {
            ImGui.endChild();
        }
    }

    private void renderSearchHeader() {
        ImGui.setNextItemWidth(-1);
        ImGui.inputTextWithHint("##catalogue_search",
                ImIcons.FA.FA_MAGNIFYING_GLASS + " Search registries...",
                searchBuffer);
        ImGui.separator();
    }

    private void renderRegistryGrid(String typeId, List<Identifier> entries, UnaryOperator<Identifier> iconProvider, boolean textOnIcon, Consumer<Identifier> onRightClick) {
        String filter = searchBuffer.get().toLowerCase();

        ImGui.beginChild("##grid_" + typeId, 0, 0, false);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4, 4);
        try {

            float windowVisibleX2 = ImGui.getWindowPos().x + ImGui.getWindowContentRegionMax().x;
            float styleSpacingX = ImGui.getStyle().getItemSpacingX();

            for (Identifier location : entries) {
                String name = location.toString();
                if (!filter.isEmpty() && !name.contains(filter)) continue;

                ImGui.pushID(name);

                Identifier iconToLoad = iconProvider.apply(location);
                int textureId = getOrLoadIcon(iconToLoad);

                if (textureId != -1) {
                    ImGuiUtils.drawImageButton(textureId, ITEM_SIZE, ITEM_SIZE);
                    if (textOnIcon) drawLetterOverlay(location);
                } else {
                    drawFallback(iconToLoad);
                }

                if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                    onRightClick.accept(location);
                }

                CataloguePayload payload = new CataloguePayload(
                        location,
                        typeId,
                        List.of(location.getNamespace()),
                        iconToLoad,
                        new ImGuiUtils.Image(textureId, ClientConfig.ICON_SIZE.get(), ClientConfig.ICON_SIZE.get()),
                        location.toString()
                );

                if (ImGui.beginDragDropSource()) {
                    ImGui.setDragDropPayload("CATALOGUE_ENTRY", payload);
                    ImGui.text("Placing " + typeId + ": " + name);
                    if (textureId != -1) ImGuiUtils.drawImageButton(textureId, 32, 32);
                    ImGui.endDragDropSource();
                }

                if (ImGui.isItemHovered()) {
                    ImGui.setTooltip(name);
                }

                float lastButtonX2 = ImGui.getItemRectMax().x;
                float nextButtonX2 = lastButtonX2 + styleSpacingX + ITEM_SIZE;
                if (nextButtonX2 < windowVisibleX2) {
                    ImGui.sameLine();
                }

                ImGui.popID();
            }
        } finally {
            ImGui.popStyleVar();
            ImGui.endChild();
        }
    }

    private void drawLetterOverlay(Identifier location) {
        String letter = location.getNamespace().substring(0, 1).toUpperCase();

        float textWidth = ImGui.calcTextSize(letter).x;
        float textHeight = ImGui.calcTextSize(letter).y;

        float minX = ImGui.getItemRectMin().x;
        float minY = ImGui.getItemRectMin().y;

        float xOffset = minX + (ITEM_SIZE - textWidth) * 0.85f;
        float yOffset = minY + (ITEM_SIZE - textHeight) * 0.80f;

        ImGui.getWindowDrawList().addText(xOffset + 1, yOffset + 1, 0xFF000000, letter);
        ImGui.getWindowDrawList().addText(xOffset, yOffset, 0xFFFFFFFF, letter);
    }

    private void drawFallback(Identifier location) {
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);

        ImGui.button("##fallback", ITEM_SIZE, ITEM_SIZE);

        String letter = location.getPath().substring(0, 1).toUpperCase();
        float textWidth = ImGui.calcTextSize(letter).x;
        float textHeight = ImGui.calcTextSize(letter).y;

        float minX = ImGui.getItemRectMin().x;
        float minY = ImGui.getItemRectMin().y;

        ImGui.getWindowDrawList().addText(
                minX + (ITEM_SIZE - textWidth) * 0.5f,
                minY + (ITEM_SIZE - textHeight) * 0.5f,
                0xFFFFFFFF,
                letter
        );

        ImGui.popStyleVar();
    }

    private int getOrLoadIcon(Identifier location) {
        if (textureCache.containsKey(location)) return textureCache.get(location);
        if (failedLoads.contains(location)) return -1;

        if (!queued.contains(location)) {
            loadQueue.add(location);
            queued.add(location);
        }

        return -1;
    }

    private void processTextureQueue() {
        int loads = 0;

        while (!loadQueue.isEmpty() && loads < MAX_LOADS_PER_FRAME) {
            Identifier location = loadQueue.poll();
            queued.remove(location);

            int texture = loadTextureNow(location);
            if (texture != -1) {
                textureCache.put(location, texture);
            } else {
                failedLoads.add(location);
            }

            loads++;
        }
    }

    private int loadTextureNow(Identifier location) {
        File outputDir = Common.CACHE.resolve("icons")
                .resolve(String.valueOf(ClientConfig.ICON_SIZE.get()))
                .toFile();

        File namespaceDir = new File(outputDir, location.getNamespace());

        if (namespaceDir.exists()) {
            String prefix = ImageExportUtil.sanitizeFilename(location.getPath());

            File[] matches = namespaceDir.listFiles((dir, fileName) ->
                    fileName.startsWith(prefix) && fileName.endsWith(".png"));

            if (matches != null && matches.length > 0) {
                ImGuiUtils.Image img = ImGuiUtils.getTexture(matches[0]);
                if (img.glId() > 0) {
                    return img.glId();
                }
            }
        }

        return -1;
    }

    public record CataloguePayload(
            Identifier id,
            String type,
            List<String> tags,
            Identifier iconLocation,
            ImGuiUtils.Image texture,
            String displayName
    ) {
        public boolean hasTexture() {
            return texture.glId() != -1;
        }
    }

    public record ClassMemberPayload(
            String className,
            String memberName,
            String memberType,
            String[] paramTypes,
            String returnType
    ) {
        public boolean isMethod() {
            return "method".equals(memberType);
        }

        public boolean isField() {
            return "field".equals(memberType);
        }
    }
}
