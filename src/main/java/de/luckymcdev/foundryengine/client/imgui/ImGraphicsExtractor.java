package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.font.BuiltInFonts;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.config.ClientConfig;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

//? if 26.1 {
/*import com.mojang.blaze3d.textures.TextureFormat;
 */
//?}
//? if 26.2 {
//?}

public class ImGraphicsExtractor implements ImStyleVarConsumer, ImStyleColorConsumer {
	private static final int MAX_ICON_LOADS_PER_FRAME = 25;
	private static final int MAX_ICON_CACHE_SIZE = 256;
	private static final Map<String, Integer> iconCache = new LinkedHashMap<>();
	private static final Map<String, DynamicTexture> iconTextures = new HashMap<>();
	private static final Set<String> pendingKeys = new HashSet<>();
	private static final Queue<ItemStack> renderQueue = new ArrayDeque<>();
	private @Nullable
	static GpuTexture fbColorTex;
	private @Nullable
	static GpuTextureView fbColorView;
	private @Nullable
	static GpuTexture fbDepthTex;
	private @Nullable
	static GpuTextureView fbDepthView;
	private @Nullable
	static ProjectionMatrixBuffer fbProjBuf;
	private static int fbSize;
	private VarStack stack;

	public ImGraphicsExtractor() {
	}

	/**
	 * Fully renders wrapped Minecraft text into ImGui.
	 *
	 * @param text      The text to render
	 * @param wrapWidth The width to wrap to
	 * @since 2.0.0
	 */
	static void component(final FormattedText text, final float wrapWidth) {
		ImGuiManager.IMGUI_CHAR_SINK.setup();
		for (final FormattedCharSequence part : Language.getInstance().getVisualOrder(ImGuiManager.IM_GUI_SPLITTER.splitLines(text, (int) wrapWidth, Style.EMPTY))) {
			part.accept(ImGuiManager.IMGUI_CHAR_SINK);
			ImGuiManager.IMGUI_CHAR_SINK.finish();
			ImGui.newLine();
		}
		ImGuiManager.IMGUI_CHAR_SINK.reset();
	}

	/**
	 * Retrieves the ImGui font to use for the specified Minecraft style.
	 *
	 * @param style The style to get the font for
	 * @return The ImFont to use
	 * @since 2.0.0
	 */
	public static ImFont getStyleFont(final Style style) {
		var fm = Client.getImGuiManager().getFontManager();
		return fm.getFont(BuiltInFonts.face(style.isBold(), style.isItalic()));
	}

	public static int getColor(final int color) {
		final ImVec4 colors = ImGui.getStyle().getColors()[color];
		return (int) (colors.w * 255) << 24 | (int) (colors.x * 255) << 16 | (int) (colors.y * 255) << 8 | (int) (colors.z * 255);
	}

	public static String icon(ImIcon icon) {
		return "" + icon;
	}

	public static String icon(ImIcon icon, String suffix) {
		return icon + suffix;
	}

	public static Image getTexture(Identifier texture) {
		return loadTexture("identifier", texture);
	}

	public static Image getTexture(File imageFile) {
		return loadTexture("file", imageFile);
	}

	private static <T> Image loadTexture(String type, T idOrFile) {
		int textureId = -1;
		int width = -1;
		int height = -1;
		if (type.equals("identifier")) {
			AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture((Identifier) idOrFile);
			if (texture != null) {
				GpuTextureView textureView = texture.getTextureView();
				GlTexture glTexture = (GlTexture) texture.getTexture();
				textureId = glTexture.glId();
				width = textureView.getWidth(textureView.baseMipLevel());
				height = textureView.getHeight(textureView.baseMipLevel());
			}
			return new Image(textureId, width, height);
		} else {
			File file = (File) idOrFile;
			try (InputStream is = new FileInputStream(file)) {
				NativeImage nativeImage = NativeImage.read(is);
				DynamicTexture texture = new DynamicTexture(() -> "EditorView_" + file.getName(), nativeImage);
				GpuTextureView textureView = texture.getTextureView();
				GlTexture glTexture = (GlTexture) texture.getTexture();
				textureId = glTexture.glId();
				width = textureView.getWidth(textureView.baseMipLevel());
				height = textureView.getHeight(textureView.baseMipLevel());
				return new Image(textureId, width, height, texture);
			} catch (IOException e) {
				Common.LOGGER.error("Failed to load texture for viewer: {}", file.getAbsolutePath(), e);
				return new Image(-1, -1, -1);
			}
		}
	}

	public static int getOrCreateItemIcon(ItemStack stack) {
		int size = ClientConfig.ICON_SIZE.get();
		String key = BuiltInRegistries.ITEM.getKey(stack.getItem()) + "@" + size;

		Integer cached = iconCache.get(key);
		if (cached != null) {
			return cached;
		}

		if (!pendingKeys.contains(key)) {
			pendingKeys.add(key);
			renderQueue.add(stack);
		}
		return -1;
	}

	public static void processIconQueue() {
		int loads = 0;
		while (!renderQueue.isEmpty() && loads < MAX_ICON_LOADS_PER_FRAME) {
			ItemStack stack = renderQueue.poll();
			int size = ClientConfig.ICON_SIZE.get();
			String key = BuiltInRegistries.ITEM.getKey(stack.getItem()) + "@" + size;
			if (iconCache.containsKey(key)) {
				continue;
			}

			renderOne(stack, size, key);
			loads++;
		}
	}

	public static void clearItemIconCache() {
		renderQueue.clear();
		pendingKeys.clear();
		for (DynamicTexture tex : iconTextures.values()) {
			tex.close();
		}
		iconTextures.clear();
		iconCache.clear();
		closeFramebuffer();
	}

	private static void renderOne(ItemStack stack, int size, String cacheKey) {
		var mc = Minecraft.getInstance();
		var level = mc.level;
		if (level == null) {
			pendingKeys.remove(cacheKey);
			return;
		}

		ensureFramebuffer(size);
		var device = RenderSystem.getDevice();

		//? if 26.1 {
		/*device.createCommandEncoder().clearColorAndDepthTextures(fbColorTex, 0, fbDepthTex, 1.0);
		 *///?} else {
		device.createCommandEncoder().clearColorAndDepthTextures(fbColorTex, new Vector4f(0.0F, 0.0F, 0.0F, 1.0F), fbDepthTex, 1.0);
		//?}
		RenderSystem.outputColorTextureOverride = fbColorView;
		RenderSystem.outputDepthTextureOverride = fbDepthView;

		Projection projection = new Projection();
		projection.setupOrtho(-1000.0F, 1000.0F, size, size, true);

		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(fbProjBuf.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

		//? if 26.1 {
		/*var resolver = mc.getItemModelResolver();
		var submitNodeCollector = mc.gameRenderer.getSubmitNodeStorage();
		var featureDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
		var bufferSource = mc.renderBuffers().bufferSource();
		var lighting = mc.gameRenderer.getLighting();
		var player = mc.player;

		TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
		resolver.updateForTopItem(renderState, stack, ItemDisplayContext.GUI, level, player, 0);

		Lighting.Entry lightingEntry = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
		lighting.setupFor(lightingEntry);

		PoseStack poseStack = new PoseStack();
		poseStack.translate(size / 2.0F, size / 2.0F, 0.0F);
		poseStack.scale(size, -size, size);
		renderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);

		featureDispatcher.renderAllFeatures();
		bufferSource.endBatch();
		*///?} else {
		var resolver = mc.getItemModelResolver();
		var submitNodes = new net.minecraft.client.renderer.SubmitNodeStorage();
		var featureDispatcher = mc.gameRenderer.featureRenderDispatcher();
		var lighting = mc.gameRenderer.lighting();
		var player = mc.player;

		TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
		resolver.updateForTopItem(renderState, stack, ItemDisplayContext.GUI, level, player, 0);

		Lighting.Entry lightingEntry = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
		lighting.setupFor(lightingEntry);

		PoseStack poseStack = new PoseStack();
		poseStack.translate(size / 2.0F, size / 2.0F, 0.0F);
		poseStack.scale(size, -size, size);
		renderState.submit(poseStack, submitNodes, 15728880, OverlayTexture.NO_OVERLAY, 0);

		featureDispatcher.renderAllFeatures(submitNodes);
		//?}

		RenderSystem.restoreProjectionMatrix();
		RenderSystem.outputColorTextureOverride = null;
		RenderSystem.outputDepthTextureOverride = null;

//? if 26.1 {
		/*int pixelSize = TextureFormat.RGBA8.pixelSize();
		GpuBuffer readBuffer = device.createBuffer(() -> "item_icon_read", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) size * size * pixelSize);
		CommandEncoder encoder = device.createCommandEncoder();
		device.createCommandEncoder().copyTextureToBuffer(fbColorTex, readBuffer, 0, () -> {
			try (var mapped = encoder.mapBuffer(readBuffer, true, false)) {
			*///?} else {
		int pixelSize = GpuFormat.RGBA8_UNORM.blockSize();
		GpuBuffer readBuffer = device.createBuffer(() -> "item_icon_read", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) size * size * pixelSize);
		device.createCommandEncoder().copyTextureToBuffer(fbColorTex, readBuffer, 0, () -> {
			try (var mapped = readBuffer.map(true, false)) {
				//?}
				NativeImage image = new NativeImage(size, size, false);
				for (int y = 0; y < size; y++) {
					for (int x = 0; x < size; x++) {
						int pixel = mapped.data().getInt((x + y * size) * pixelSize);
						image.setPixelABGR(x, size - y - 1, pixel);
					}
				}
				DynamicTexture dynTex = new DynamicTexture(() -> "item_icon_" + cacheKey, image);
				int glId = ((GlTexture) dynTex.getTexture()).glId();
				iconCache.put(cacheKey, glId);
				iconTextures.put(cacheKey, dynTex);
				evictOldestIconIfNeeded();
				pendingKeys.remove(cacheKey);
			} catch (Exception e) {
				Common.LOGGER.error("Failed to read back item icon for {}", cacheKey, e);
				pendingKeys.remove(cacheKey);
			}
			readBuffer.close();
		}, 0);
	}

	private static void evictOldestIconIfNeeded() {
		while (iconCache.size() > MAX_ICON_CACHE_SIZE) {
			Iterator<String> it = iconCache.keySet().iterator();
			String oldestKey = it.next();
			it.remove();
			DynamicTexture texture = iconTextures.remove(oldestKey);
			if (texture != null) {
				texture.close();
			}
			pendingKeys.remove(oldestKey);
		}
	}

	private static void ensureFramebuffer(int size) {
		if (fbSize == size && fbColorTex != null && !fbColorTex.isClosed()) {
			return;
		}
		closeFramebuffer();
		fbSize = size;
		var device = RenderSystem.getDevice();
//? if 26.1 {
		/*fbColorTex = device.createTexture(() -> "item_icon_fb", 13, TextureFormat.RGBA8, size, size, 1, 1);
		fbColorView = device.createTextureView(fbColorTex);
		fbDepthTex = device.createTexture(() -> "item_icon_fb_depth", 9, TextureFormat.DEPTH32, size, size, 1, 1);
		fbDepthView = device.createTextureView(fbDepthTex);
		*///?} else {
		fbColorTex = device.createTexture(() -> "item_icon_fb", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST, GpuFormat.RGBA8_UNORM, size, size, 1, 1);
		fbColorView = device.createTextureView(fbColorTex);
		fbDepthTex = device.createTexture(() -> "item_icon_fb_depth", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST, GpuFormat.D32_FLOAT, size, size, 1, 1);
		fbDepthView = device.createTextureView(fbDepthTex);
		//?}
		fbProjBuf = new ProjectionMatrixBuffer("item_icon_fb_proj");
	}

	private static void closeFramebuffer() {
		if (fbColorTex != null) {
			fbColorTex.close();
			fbColorTex = null;
		}
		if (fbColorView != null) {
			fbColorView.close();
			fbColorView = null;
		}
		if (fbDepthTex != null) {
			fbDepthTex.close();
			fbDepthTex = null;
		}
		if (fbDepthView != null) {
			fbDepthView.close();
			fbDepthView = null;
		}
		if (fbProjBuf != null) {
			fbProjBuf.close();
			fbProjBuf = null;
		}
		fbSize = 0;
	}

	public void component(final FormattedText text) {
		component(text, Float.POSITIVE_INFINITY);
	}

	public void pushStack() {
		var newStack = new VarStack();
		newStack.parent = stack;
		stack = newStack;
	}

	public void pushRootStack() {
		pushStack();
	}

	public void popStack() {
		if (stack == null) {
			throw new RuntimeException("popStack() called without a matching pushStack()");
		}

		if (stack.pushedStyle > 0) {
			ImGui.popStyleVar(stack.pushedStyle);
		}

		if (stack.pushedColors > 0) {
			ImGui.popStyleColor(stack.pushedColors);
		}

		for (int i = 0; i < stack.pushedItemFlags; i++) {
			imgui.internal.ImGui.popItemFlag();
		}

		if (stack.pushedFontScales != null) {
			var font = ImGui.getFont();

			for (int i = stack.pushedFontScales.size() - 1; i >= 0; i--) {
				font.setScale(stack.pushedFontScales.getFloat(i));
				ImGui.popFont();
			}
		}

		stack = stack.parent;
	}

	public int getStackDepth() {
		int depth = 0;
		var s = stack;
		while (s != null) {
			depth++;
			s = s.parent;
		}
		return depth;
	}

	public void setNextItemWidth(float width) {
		ImGui.setNextItemWidth(width);
	}

	public void pushItemWidth(float width) {
		ImGui.pushItemWidth(width);
	}

	public void popItemWidth() {
		ImGui.popItemWidth();
	}

	public void setNextWindowSizeConstraints(float minW, float minH, float maxW, float maxH) {
		ImGui.setNextWindowSizeConstraints(minW, minH, maxW, maxH);
	}

	@Override
	public void setStyleVar(int key, float value) {
		ImGui.pushStyleVar(key, value);
		stack.pushedStyle++;
	}

	@Override
	public void setStyleVar(int key, float x, float y) {
		ImGui.pushStyleVar(key, x, y);
		stack.pushedStyle++;
	}

	@Override
	public void setStyleCol(int key, int r, int g, int b, int a) {
		ImGui.pushStyleColor(key, r, g, b, a);
		stack.pushedColors++;
	}

	public void setItemFlag(int key, boolean flag) {
		imgui.internal.ImGui.pushItemFlag(key, flag);
		stack.pushedItemFlags++;
	}

	public void setFontScale(float scale) {
		var font = ImGui.getFont();
		ImGui.pushFont(font, 0.0F);
		ImGui.setWindowFontScale(scale);

		if (stack.pushedFontScales == null) {
			stack.pushedFontScales = new FloatArrayList(1);
		}

		stack.pushedFontScales.add(stack.currentFontScale);
		stack.currentFontScale = scale;
	}

	public void redTextIf(String text, boolean condition) {
		if (condition) {
			pushStack();
			setErrorText();
			ImGui.text(text);
			popStack();
		} else {
			ImGui.text(text);
		}
	}

	public void smallText(String text) {
		pushStack();
		setFontScale(0.75F);
		ImGui.text(text);
		popStack();
	}

	public boolean button(String label, @Nullable ImColorVariant variant) {
		return button(label, variant, 0.0F);
	}

	public boolean button(String label, @Nullable ImColorVariant variant, float width) {
		if (variant != null) {
			pushStack();
			setButton(variant);
		}
		boolean clicked = ImGui.button(label, width, 0.0F);
		if (variant != null) {
			popStack();
		}
		return clicked;
	}

	public boolean smallButton(String label, @Nullable ImColorVariant variant) {
		if (variant != null) {
			pushStack();
			setButton(variant);
		}
		boolean clicked = ImGui.smallButton(label);
		if (variant != null) {
			popStack();
		}
		return clicked;
	}

	public boolean iconButton(ImIcon icon, String id, String tooltip, @Nullable ImColorVariant variant) {
		if (variant != null) {
			pushStack();
			setButton(variant);
		}
		boolean clicked = ImGui.button(icon(icon) + id);
		if (variant != null) {
			popStack();
		}
		if (ImGui.isItemHovered() && tooltip != null && !tooltip.isEmpty()) {
			ImGui.setTooltip(tooltip);
		}
		return clicked;
	}

	public boolean collapsingHeader(String label, int flags) {
		pushStack();
		setStyleCol(ImGuiCol.Header, 0, 0, 0, 255);
		boolean open = ImGui.collapsingHeader(label, flags);
		popStack();
		return open;
	}

	public boolean collapsingHeader(String label, ImBoolean visible, int flags) {
		pushStack();
		setStyleCol(ImGuiCol.Header, 0, 0, 0, 255);
		boolean open = ImGui.collapsingHeader(label, visible, flags);
		popStack();
		return open;
	}

	public <E> boolean combo(String label, Object[] selected, String noneLabel, Iterable<? extends E> options, Function<E, String> nameFunction) {
		var changed = false;
		if (ImGui.beginCombo(label, selected[0] == null ? (noneLabel.isEmpty() ? "None" : noneLabel) : nameFunction.apply((E) selected[0]), ImGuiInputTextFlags.None)) {
			int i = 0;
			if (!noneLabel.isEmpty()) {
				boolean isSelected = selected[0] == null;
				if (ImGui.selectable(noneLabel + "###" + i, isSelected)) {
					selected[0] = null;
					changed = true;
				}
				if (isSelected) {
					ImGui.setItemDefaultFocus();
				}
				i++;
			}
			for (var option : options) {
				boolean isSelected = selected[0] == option;
				var itemLabel = nameFunction.apply(option);
				if (ImGui.selectable(itemLabel + "###" + i, isSelected)) {
					selected[0] = option;
					changed = true;
				}
				if (isSelected) {
					ImGui.setItemDefaultFocus();
				}
				i++;
			}
			ImGui.endCombo();
		}
		return changed;
	}

	public <E> boolean combo(String label, Object[] selected, String noneLabel, E[] options, Function<E, String> nameFunction) {
		return combo(label, selected, noneLabel, Arrays.asList(options), nameFunction);
	}

	public void helpTooltip(String text) {
		ImGui.sameLine();
		ImGui.textColored(0xFF555555, "(?)");
		if (ImGui.isItemHovered()) {
			ImGui.beginTooltip();
			ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
			ImGui.textColored(0xFFFFFFFF, text);
			ImGui.popTextWrapPos();
			ImGui.endTooltip();
		}
	}

	public void displayIcon(ImIcon icon) {
		ImGui.setWindowFontScale(2.0f);
		ImGui.text(icon.iconText(""));
		ImGui.setWindowFontScale(1.0f);
	}

	public void textCentered(String text, float width) {
		ImGui.setCursorPosX(ImGui.getCursorPosX() + (width - ImGui.getFont().calcTextSizeAX(ImGui.getFontSize(), Float.MAX_VALUE, 0, text)) / 2);
		ImGui.text(text);
	}

	public void centered(Runnable runnable, float itemWidth, float totalWidth) {
		float posX = ImGui.getCursorPosX() + (totalWidth - itemWidth) / 2.0f;
		ImGui.setCursorPosX(posX);
		runnable.run();
	}

	public void identifier(Identifier loc) {
		ImGui.beginGroup();
		ImGui.textColored(colorOfMod(loc.getNamespace()).argb(), loc.getNamespace() + ":");
		ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
		ImGui.sameLine();
		ImGui.text(loc.getPath());
		ImGui.popStyleVar();
		ImGui.endGroup();

		if (ImGui.beginPopupContextItem("" + loc)) {
			if (ImGui.selectable("##copy_location")) {
				ImGui.setClipboardText(loc.toString());
			}
			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
			ImGui.setNextItemAllowOverlap();
			ImGui.sameLine();
			this.displayIcon(ImIcons.CLIPBOARD);
			ImGui.sameLine();
			ImGui.popStyleVar();
			ImGui.text("Copy Location");
			ImGui.endPopup();
		}
	}

	public void section(String title) {
		ImGui.textColored(0xFF00AAFF, title);
		ImGui.separator();
	}

	public void sectionLabel(String text) {
		ImGui.textDisabled(text);
		ImGui.spacing();
	}

	public void scrollableRegion(String id, Runnable content) {
		scrollableRegion(id, 0, 0, true, content);
	}

	public void scrollableRegion(String id, float width, float height, boolean border, Runnable content) {
		if (ImGui.beginChild(id, width, height, border)) {
			content.run();
		}
		ImGui.endChild();
	}

	public boolean collapse(String label, Runnable content) {
		return collapse(label, ImGuiTreeNodeFlags.None, content);
	}

	public boolean collapse(String label, int flags, Runnable content) {
		if (ImGui.collapsingHeader(label, flags)) {
			content.run();
			return true;
		}
		return false;
	}

	public void labeledValue(String label, String value) {
		ImGui.textColored(0xFF00AAFF, label);
		ImGui.sameLine();
		ImGui.textDisabled(value);
	}

	public void withFont(FontDescription.Resource font, Runnable body) {
		var fonts = Client.getImGuiManager().getFontManager();
		fonts.pushFont(font);
		body.run();
		fonts.popFont();
	}

	public void treeSection(String label, Runnable body) {
		int flags = ImGuiTreeNodeFlags.SpanAvailWidth
			| ImGuiTreeNodeFlags.DefaultOpen
			| ImGuiTreeNodeFlags.Framed;
		if (ImGui.treeNodeEx(label, flags, label)) {
			body.run();
			ImGui.treePop();
		}
	}

	public void centeredMessage(String text) {
		ImGui.dummy(0, 24);
		float avail = ImGui.getContentRegionAvailX();
		float textW = ImGui.calcTextSize(text).x;
		ImGui.setCursorPosX(Math.max(0, (avail - textW) / 2.0f));
		ImGui.textDisabled(text);
	}

	public void cardBegin(String id) {
		ImGui.pushID(id);
		ImGui.separator();
	}

	public void cardEnd() {
		ImGui.separator();
		ImGui.popID();
	}

	public @Nullable Color colorEdit4(String label, Color current) {
		float[] f = new float[]{current.r(), current.g(), current.b(), current.a()};
		ImGui.setNextItemWidth(180);
		if (ImGui.colorEdit4(label, f, ImGuiColorEditFlags.NoInputs)) {
			return new Color(f[0], f[1], f[2], f[3]);
		}
		return null;
	}

	public boolean colorEdit4Int(String label, int[] rgba) {
		float[] f = new float[]{
			((rgba[0] >> 16) & 0xFF) / 255.0f,
			((rgba[0] >> 8) & 0xFF) / 255.0f,
			(rgba[0] & 0xFF) / 255.0f,
			((rgba[0] >> 24) & 0xFF) / 255.0f
		};
		ImGui.setNextItemWidth(180);
		if (ImGui.colorEdit4(label, f, ImGuiColorEditFlags.NoInputs)) {
			int r = (int) (f[0] * 255);
			int g = (int) (f[1] * 255);
			int b = (int) (f[2] * 255);
			int a = (int) (f[3] * 255);
			rgba[0] = (a << 24) | (r << 16) | (g << 8) | b;
			return true;
		}
		return false;
	}

	public void drawImage(int id, float w, float h) {
		GlStateManager._bindTexture(id);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		pushStack();
		setStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
		ImGui.image(id, w, h, 0, 0, 1, 1);
		popStack();
	}

	public void drawImage(Image image) {
		drawImage(image.glId(), image.width(), image.height());
	}

	public void drawImage(Image image, float w, float h) {
		drawImage(image.glId(), w, h);
	}

	public void drawImageButton(int id, float w, float h) {
		GlStateManager._bindTexture(id);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		pushStack();
		setStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
		ImGui.imageButton(String.valueOf(id), id, w, h, 0, 0, 1, 1);
		popStack();
	}

	public String timer(long time) {
		long ms = time % 1000;
		long sec = (time / 1000) % 60;
		long min = (time / (1000 * 60)) % 60;
		long hrs = (time / (1000 * 60 * 60));

		StringBuilder sb = new StringBuilder();
		if (hrs > 0) {
			if (hrs < 10) {
				sb.append('0');
			}
			sb.append(hrs).append(':');
		}
		if (min < 10) {
			sb.append('0');
		}
		sb.append(min).append(':');
		if (sec < 10) {
			sb.append('0');
		}
		sb.append(sec).append('.');
		if (ms < 100) {
			sb.append('0');
		}
		if (ms < 10) {
			sb.append('0');
		}
		sb.append(ms);
		return sb.toString();
	}

	public Color colorOfMod(String modid) {
		if (modid == null) {
			return Color.WHITE;
		}
		int hash = modid.hashCode();
		return new Color(0xFF000000 | (hash & 0x00FFFFFF));
	}

	public String formatColored(String text, Object... args) {
		return String.format(text, args);
	}

	private static class VarStack {
		private VarStack parent;
		private int pushedStyle = 0;
		private int pushedColors = 0;
		private int pushedItemFlags = 0;
		private float currentFontScale = 1.0F;
		private FloatList pushedFontScales = null;
	}

	public record Image(int glId, int width, int height, @Nullable DynamicTexture ownedTexture) {
		public Image(int glId, int width, int height) {
			this(glId, width, height, null);
		}

		public void close() {
			if (ownedTexture != null) {
				ownedTexture.close();
			}
		}
	}
}
