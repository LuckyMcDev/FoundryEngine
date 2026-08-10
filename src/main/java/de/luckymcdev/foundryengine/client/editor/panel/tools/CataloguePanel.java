package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.ImTexture;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.editor.GiveItemPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSpawnEntityPacket;
import de.luckymcdev.foundryengine.config.ClientConfig;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class CataloguePanel extends EditorPanel {
	public static final CataloguePanel INSTANCE = new CataloguePanel();
	private static final float ITEM_SIZE = 64.0f;
	private static final Identifier NAMETAG_ID = Identifier.parse("minecraft:name_tag");
	private static final Identifier BUCKET_ID = Identifier.parse("minecraft:water_bucket");
	private static final Identifier SPAWNER_ID = Identifier.parse("minecraft:spawner");
	private static final Identifier CRAFTING_TABLE_ID = Identifier.parse("minecraft:crafting_table");
	private final Set<Identifier> failedLoads = new HashSet<>();
	private final ImString searchBuffer = new ImString(256);

	public CataloguePanel() {
		super(new Builder(Common.id("catalogue"))
			.icon(ImIcons.LIST)
			.category(PanelCategory.TOOLS));
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
	public void content(ImGraphicsExtractor g) {
		if (!requireWorld()) {
			return;
		}
		processTextureQueue();
		renderSearchHeader();

		if (ImGui.beginTabBar("CatalogueTabs")) {

			if (ImGui.beginTabItem(ImIcons.BOX + " Items")) {
				var list = BuiltInRegistries.ITEM.keySet().stream()
					.sorted(Comparator.comparing(Identifier::getPath)).toList();
				renderRegistryGrid(g, "items", list, id -> id, false, id -> {
					if (Minecraft.getInstance().level == null) {
						return;
					}
					ClientPacketDistributor.sendToServer(new GiveItemPacket(id.toString()));
				});
				ImGui.endTabItem();
			}

			if (ImGui.beginTabItem(ImIcons.CUBE + " Blocks")) {
				List<Identifier> blockItems = BuiltInRegistries.BLOCK.stream()
					.map(block -> BuiltInRegistries.ITEM.getKey(block.asItem()))
					.distinct()
					.sorted(Comparator.comparing(Identifier::getPath)).toList();
				renderRegistryGrid(g, "blocks", blockItems, id -> id, false, id -> {
				});
				ImGui.endTabItem();
			}

			if (ImGui.beginTabItem(ImIcons.PAW + " Entities")) {
				var list = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
					.sorted(Comparator.comparing(Identifier::getPath)).toList();

				renderRegistryGrid(g, "entities", list,
					entityId -> {
						var optType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
						if (optType.isEmpty()) {
							return SPAWNER_ID;
						}
						var eggHolder = SpawnEggItem.byId(optType.get());
						return eggHolder.map(itemHolder -> BuiltInRegistries.ITEM.getKey(itemHolder.value())).orElse(SPAWNER_ID);
					},
					false,
					id -> {
						if (Minecraft.getInstance().level == null) {
							return;
						}
						LocalPlayer player = Minecraft.getInstance().player;
						var pos = player.position();
						ClientPacketDistributor.sendToServer(new ServerBoundSpawnEntityPacket(
							id.toString(), pos.x, pos.y, pos.z, player.getYRot(), player.getXRot()
						));
					});
				ImGui.endTabItem();
			}

			if (ImGui.beginTabItem(ImIcons.DROPLET + " Fluids")) {
				var list = BuiltInRegistries.FLUID.keySet().stream()
					.sorted(Comparator.comparing(Identifier::getPath)).toList();

				renderRegistryGrid(g, "fluids", list,
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

			if (ImGui.beginTabItem(ImIcons.TAG + " Tags")) {
				List<Identifier> allTagIds = BuiltInRegistries.REGISTRY.entrySet().stream()
					.flatMap(entry -> {
						var tags = entry.getValue().getTags().toList();
						if (tags.isEmpty()) {
							return Stream.empty();
						}
						return tags.stream().map(t -> t.key().location());
					})
					.sorted(Comparator.comparing(Identifier::getPath))
					.distinct()
					.toList();
				renderRegistryGrid(g, "tags", allTagIds, id -> NAMETAG_ID, true, id -> {
				});
				ImGui.endTabItem();
			}

			if (ImGui.beginTabItem(ImIcons.BOOK + " Recipes")) {
				RecipeManager recipeManager = Client.getRecipeManager();
				if (recipeManager == null) {
					ImGui.textDisabled("No recipe manager available (join a world)");
				} else {
					List<Identifier> recipeIds = new ArrayList<>();
					for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
						recipeIds.add(holder.id().identifier());
					}
					recipeIds.sort(Comparator.comparing(Identifier::getPath));

					renderRegistryGrid(g, "recipes", recipeIds,
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

			if (ImGui.beginTabItem(ImIcons.CODE + " Classes")) {
				ImGui.textDisabled("Class browser has been removed.");
				ImGui.endTabItem();
			}

			ImGui.endTabBar();
		}
	}

	private void renderClassesTab() {
		ImGui.textDisabled("Class browser has been removed.");
	}

	private void renderSearchHeader() {
		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##catalogue_search",
			ImIcons.MAGNIFYING_GLASS + " Search registries...",
			searchBuffer);
		ImGui.separator();
	}

	private void renderRegistryGrid(ImGraphicsExtractor g, String typeId, List<Identifier> entries, UnaryOperator<Identifier> iconProvider, boolean textOnIcon, Consumer<Identifier> onRightClick) {
		String filter = searchBuffer.get().toLowerCase();

		ImGui.beginChild("##grid_" + typeId, 0, 0, false);
		ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4, 4);
		try {

			float windowVisibleX2 = ImGui.getWindowPos().x + ImGui.getWindowContentRegionMax().x;
			float styleSpacingX = ImGui.getStyle().getItemSpacingX();

			for (Identifier location : entries) {
				String name = location.toString();
				if (!filter.isEmpty() && !name.contains(filter)) {
					continue;
				}

				ImGui.pushID(name);

				Identifier iconToLoad = iconProvider.apply(location);
				var texture = getOrLoadIcon(iconToLoad);

				if (texture != null) {
					g.drawImageButton(texture, ITEM_SIZE, ITEM_SIZE);
					if (textOnIcon) {
						drawLetterOverlay(location);
					}
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
					texture,
					location.toString()
				);

				if (ImGui.beginDragDropSource()) {
					ImGui.setDragDropPayload("CATALOGUE_ENTRY", payload);
					ImGui.text("Placing " + typeId + ": " + name);
					if (texture != null) {
						g.drawImageButton(texture, 32, 32);
					}
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

	private @Nullable ImTexture getOrLoadIcon(Identifier location) {
		var item = BuiltInRegistries.ITEM.getOptional(location);
		if (item.isEmpty()) {
			return null;
		}
		return ImGraphicsExtractor.getOrCreateItemIcon(new ItemStack(item.get()));
	}

	private void processTextureQueue() {
		ImGraphicsExtractor.processIconQueue();
	}

	public record CataloguePayload(
		Identifier id,
		String type,
		List<String> tags,
		Identifier iconLocation,
		ImTexture texture,
		String displayName
	) {
		public boolean hasTexture() {
			return texture != null && texture.getTexture() != null;
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
