package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTextureView;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.font.BuiltInFonts;
import de.luckymcdev.foundryengine.common.util.color.Color;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.impl.ImGuiMCImpl;
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
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;



public class ImGraphicsExtractor implements ImStyleVarConsumer, ImStyleColorConsumer {
	private static final OffscreenRenderer offscreenRenderer = new OffscreenRenderer();
	private static final ItemIconCache itemIconCache = new ItemIconCache();
	private VarStack stack;

	public ImGraphicsExtractor() {
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

	public static ImTexture getTexture(Identifier texture) {
		return loadTexture(TextureType.IDENTIFIER, texture);
	}

	public static ImTexture getTexture(File imageFile) {
		return loadTexture(TextureType.FILE, imageFile);
	}

	private static <T> ImTexture loadTexture(TextureType type, T idOrFile) {
		switch (type) {
			case IDENTIFIER -> {
				Identifier identifier = (Identifier) idOrFile;
				AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
				if (texture != null) {
					GpuTextureView textureView = texture.getTextureView();
					if (textureView != null) {
						int width = textureView.getWidth(textureView.baseMipLevel());
						int height = textureView.getHeight(textureView.baseMipLevel());
						return new ImTexture(texture, false, width, height);
					}
				}
				return ImTexture.EMPTY;
			}

			case FILE -> {
				File file = (File) idOrFile;
				try (InputStream is = new FileInputStream(file)) {
					NativeImage nativeImage = NativeImage.read(is);
					DynamicTexture texture = new DynamicTexture(() -> "EditorView_" + file.getName(), nativeImage);
					GpuTextureView textureView = texture.getTextureView();
					if (textureView != null) {
						int width = textureView.getWidth(textureView.baseMipLevel());
						int height = textureView.getHeight(textureView.baseMipLevel());
						return new ImTexture(texture, true, width, height);
					}
					texture.close();
					return ImTexture.EMPTY;
				} catch (IOException e) {
					Common.LOGGER.error("Failed to load texture for viewer: {}", file.getAbsolutePath(), e);
					return ImTexture.EMPTY;
				}
			}

			default -> throw new IllegalArgumentException("Unexpected texture type: " + type);
		}
	}

	public static @Nullable ImTexture getOrCreateItemIcon(ItemStack stack) {
		return itemIconCache.get(stack);
	}

	public static void processIconQueue() {
		itemIconCache.processQueue(offscreenRenderer);
	}

	public static void clearItemIconCache() {
		itemIconCache.clear();
		offscreenRenderer.close();
	}

	public static OffscreenRenderer getOffscreenRenderer() {
		return offscreenRenderer;
	}

	public static long textureId(@Nullable ImTexture image) {
		if (image == null || image.getTexture() == null || ImGuiMCImpl.handler == null) {
			return 0;
		}
		return ImGuiMCImpl.handler.getRenderer().getImGuiId(image.getProvider(), null);
	}

	public void drawImage(@Nullable ImTexture texture) {
		if (texture == null || texture.getTexture() == null) {
			return;
		}
		drawImage(texture, texture.width(), texture.height());
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

	public void component(final FormattedText text) {
		ImGuiMC.component(text);
	}

	public void drawImage(@Nullable ImTexture texture, float w, float h) {
		if (texture == null || texture.getTexture() == null) {
			return;
		}
		pushStack();
		setStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
		ImGuiMC.image(texture.getProvider(), w, h, 0, 0, 1, 1);
		popStack();
	}

	public void drawImageButton(@Nullable ImTexture image, float w, float h) {
		if (image == null || image.getTexture() == null) {
			return;
		}
		pushStack();
		setStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
		ImGuiMC.imageButton("##image", image.getProvider(), w, h, 0, 0, 1, 1);
		popStack();
	}

	enum TextureType {
		IDENTIFIER,
		FILE
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
}