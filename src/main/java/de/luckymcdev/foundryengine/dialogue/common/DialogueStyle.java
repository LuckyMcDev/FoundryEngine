package de.luckymcdev.foundryengine.dialogue.common;

import de.luckymcdev.foundryengine.util.common.color.Color;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Style configuration for dialogue rendering.
 *
 * <p>Values are stored as named entries in a stack of {@link StyleLayer}s. Getters
 * resolve a key by walking the stack from the top down, falling back to
 * {@link #DEFAULTS} when no layer overrides it. This allows temporary overrides via
 * {@link #push()} / {@link #pop()} while the tree-level base style stays intact.</p>
 */
public class DialogueStyle {
	public static final String KEY_DIALOGUE_BACKGROUND = "DialogueBackground";
	public static final String KEY_DIALOGUE_BORDER = "DialogueBorder";
	public static final String KEY_DIALOGUE_BORDER_WIDTH = "DialogueBorderWidth";
	public static final String KEY_OPTIONS_BACKGROUND = "OptionsBackground";
	public static final String KEY_OPTIONS_BORDER = "OptionsBorder";
	public static final String KEY_OPTIONS_BORDER_WIDTH = "OptionsBorderWidth";
	public static final String KEY_BUTTON_BACKGROUND = "ButtonBackground";
	public static final String KEY_BUTTON_HOVER = "ButtonHover";
	public static final String KEY_BUTTON_BORDER = "ButtonBorder";
	public static final String KEY_NAV_BUTTON_BACKGROUND = "NavButtonBackground";
	public static final String KEY_NAV_BUTTON_HOVER = "NavButtonHover";
	public static final String KEY_NAV_BUTTON_BORDER = "NavButtonBorder";
	public static final String KEY_OVERLAY_COLOR = "OverlayColor";
	public static final String KEY_SPEAKER_FONT_SIZE = "SpeakerFontSize";
	public static final String KEY_DIALOGUE_FONT_SIZE = "DialogueFontSize";
	public static final String KEY_OPTION_FONT_SIZE = "OptionFontSize";
	public static final String KEY_MARGIN = "Margin";
	public static final String KEY_PANEL_HEIGHT = "PanelHeight";
	public static final String KEY_BUTTON_HEIGHT = "ButtonHeight";
	public static final String KEY_OPTION_GAP = "OptionGap";
	public static final String KEY_TYPEWRITER_SOUND_ENABLED = "TypewriterSoundEnabled";
	public static final String KEY_TYPEWRITER_CHARS_PER_SECOND = "TypewriterCharsPerSecond";

	public static final StyleLayer DEFAULTS = createDefaults();
	private final Deque<StyleLayer> layers = new ArrayDeque<>();

	public DialogueStyle() {
		layers.push(new StyleLayer(DEFAULTS));
	}

	public static DialogueStyle fromNbt(CompoundTag tag) {
		var s = new DialogueStyle();
		var layer = s.peek();

		if (tag.contains(KEY_DIALOGUE_BACKGROUND)) {
			layer.set(KEY_DIALOGUE_BACKGROUND, new Color(tag.getIntOr(KEY_DIALOGUE_BACKGROUND, 0)));
		}
		if (tag.contains(KEY_DIALOGUE_BORDER)) {
			layer.set(KEY_DIALOGUE_BORDER, new Color(tag.getIntOr(KEY_DIALOGUE_BORDER, 0)));
		}
		if (tag.contains(KEY_DIALOGUE_BORDER_WIDTH)) {
			layer.set(KEY_DIALOGUE_BORDER_WIDTH, tag.getIntOr(KEY_DIALOGUE_BORDER_WIDTH, 0));
		}
		if (tag.contains(KEY_OPTIONS_BACKGROUND)) {
			layer.set(KEY_OPTIONS_BACKGROUND, new Color(tag.getIntOr(KEY_OPTIONS_BACKGROUND, 0)));
		}
		if (tag.contains(KEY_OPTIONS_BORDER)) {
			layer.set(KEY_OPTIONS_BORDER, new Color(tag.getIntOr(KEY_OPTIONS_BORDER, 0)));
		}
		if (tag.contains(KEY_OPTIONS_BORDER_WIDTH)) {
			layer.set(KEY_OPTIONS_BORDER_WIDTH, tag.getIntOr(KEY_OPTIONS_BORDER_WIDTH, 0));
		}
		if (tag.contains(KEY_BUTTON_BACKGROUND)) {
			layer.set(KEY_BUTTON_BACKGROUND, new Color(tag.getIntOr(KEY_BUTTON_BACKGROUND, 0)));
		}
		if (tag.contains(KEY_BUTTON_HOVER)) {
			layer.set(KEY_BUTTON_HOVER, new Color(tag.getIntOr(KEY_BUTTON_HOVER, 0)));
		}
		if (tag.contains(KEY_BUTTON_BORDER)) {
			layer.set(KEY_BUTTON_BORDER, new Color(tag.getIntOr(KEY_BUTTON_BORDER, 0)));
		}
		if (tag.contains(KEY_NAV_BUTTON_BACKGROUND)) {
			layer.set(KEY_NAV_BUTTON_BACKGROUND, new Color(tag.getIntOr(KEY_NAV_BUTTON_BACKGROUND, 0)));
		}
		if (tag.contains(KEY_NAV_BUTTON_HOVER)) {
			layer.set(KEY_NAV_BUTTON_HOVER, new Color(tag.getIntOr(KEY_NAV_BUTTON_HOVER, 0)));
		}
		if (tag.contains(KEY_NAV_BUTTON_BORDER)) {
			layer.set(KEY_NAV_BUTTON_BORDER, new Color(tag.getIntOr(KEY_NAV_BUTTON_BORDER, 0)));
		}
		if (tag.contains(KEY_OVERLAY_COLOR)) {
			layer.set(KEY_OVERLAY_COLOR, new Color(tag.getIntOr(KEY_OVERLAY_COLOR, 0)));
		}
		if (tag.contains(KEY_SPEAKER_FONT_SIZE)) {
			layer.set(KEY_SPEAKER_FONT_SIZE, tag.getIntOr(KEY_SPEAKER_FONT_SIZE, 0));
		}
		if (tag.contains(KEY_DIALOGUE_FONT_SIZE)) {
			layer.set(KEY_DIALOGUE_FONT_SIZE, tag.getIntOr(KEY_DIALOGUE_FONT_SIZE, 0));
		}
		if (tag.contains(KEY_OPTION_FONT_SIZE)) {
			layer.set(KEY_OPTION_FONT_SIZE, tag.getIntOr(KEY_OPTION_FONT_SIZE, 0));
		}
		if (tag.contains(KEY_MARGIN)) {
			layer.set(KEY_MARGIN, tag.getIntOr(KEY_MARGIN, 0));
		}
		if (tag.contains(KEY_PANEL_HEIGHT)) {
			layer.set(KEY_PANEL_HEIGHT, tag.getIntOr(KEY_PANEL_HEIGHT, 0));
		}
		if (tag.contains(KEY_BUTTON_HEIGHT)) {
			layer.set(KEY_BUTTON_HEIGHT, tag.getIntOr(KEY_BUTTON_HEIGHT, 0));
		}
		if (tag.contains(KEY_OPTION_GAP)) {
			layer.set(KEY_OPTION_GAP, tag.getIntOr(KEY_OPTION_GAP, 0));
		}
		if (tag.contains(KEY_TYPEWRITER_SOUND_ENABLED)) {
			layer.set(KEY_TYPEWRITER_SOUND_ENABLED, tag.getBooleanOr(KEY_TYPEWRITER_SOUND_ENABLED, true));
		}
		if (tag.contains(KEY_TYPEWRITER_CHARS_PER_SECOND)) {
			layer.set(KEY_TYPEWRITER_CHARS_PER_SECOND, tag.getIntOr(KEY_TYPEWRITER_CHARS_PER_SECOND, 0));
		}

		return s;
	}

	private static StyleLayer createDefaults() {
		var layer = new StyleLayer();
		layer.set(KEY_DIALOGUE_BACKGROUND, new Color(0xCC101010));
		layer.set(KEY_DIALOGUE_BORDER, new Color(0xFF666666));
		layer.set(KEY_DIALOGUE_BORDER_WIDTH, 2);
		layer.set(KEY_OPTIONS_BACKGROUND, new Color(0xCC101010));
		layer.set(KEY_OPTIONS_BORDER, new Color(0xFF666666));
		layer.set(KEY_OPTIONS_BORDER_WIDTH, 2);
		layer.set(KEY_BUTTON_BACKGROUND, new Color(0x88000000));
		layer.set(KEY_BUTTON_HOVER, new Color(0xAA444444));
		layer.set(KEY_BUTTON_BORDER, new Color(0xFF888888));
		layer.set(KEY_NAV_BUTTON_BACKGROUND, new Color(0x88000000));
		layer.set(KEY_NAV_BUTTON_HOVER, new Color(0xAA444444));
		layer.set(KEY_NAV_BUTTON_BORDER, new Color(0xFF888888));
		layer.set(KEY_OVERLAY_COLOR, new Color(0x88000000));
		layer.set(KEY_SPEAKER_FONT_SIZE, 9);
		layer.set(KEY_DIALOGUE_FONT_SIZE, 9);
		layer.set(KEY_OPTION_FONT_SIZE, 9);
		layer.set(KEY_MARGIN, 10);
		layer.set(KEY_PANEL_HEIGHT, 125);
		layer.set(KEY_BUTTON_HEIGHT, 22);
		layer.set(KEY_OPTION_GAP, 4);
		layer.set(KEY_TYPEWRITER_SOUND_ENABLED, true);
		layer.set(KEY_TYPEWRITER_CHARS_PER_SECOND, 45);
		return layer.freeze();
	}

	/**
	 * Pushes a new empty layer on top. Subsequent {@link #set(String, Object)} calls
	 * write into this layer; {@link #pop()} removes it again.
	 */
	public void push() {
		layers.push(new StyleLayer());
	}

	/**
	 * Pops the top override layer. At least one layer is always kept.
	 *
	 * @return the removed layer, or {@code null} if none could be removed
	 */
	public @Nullable StyleLayer pop() {
		return layers.size() > 1 ? layers.removeFirst() : null;
	}

	/**
	 * Discards all overrides, leaving a single base layer seeded from {@link #DEFAULTS}.
	 */
	public void reset() {
		layers.clear();
		layers.push(new StyleLayer(DEFAULTS));
	}

	/**
	 * The current top layer, used for writing.
	 */
	public StyleLayer peek() {
		return layers.getFirst();
	}

	/**
	 * Resolves a value by walking the layers, falling back to {@link #DEFAULTS}.
	 */
	public Object get(String key) {
		for (var layer : layers) {
			if (layer.contains(key)) {
				return layer.get(key);
			}
		}
		return DEFAULTS.get(key);
	}

	/**
	 * Writes a value into the current top layer.
	 */
	public void set(String key, Object value) {
		layers.getFirst().set(key, value);
	}

	/**
	 * Removes a value from the current top layer.
	 */
	public void unset(String key) {
		layers.getFirst().remove(key);
	}

	private Color color(String key) {
		var value = get(key);
		return value instanceof Color c ? c : (Color) DEFAULTS.get(key);
	}

	private int integer(String key) {
		var value = get(key);
		return value instanceof Integer i ? i : (Integer) DEFAULTS.get(key);
	}

	private boolean bool(String key) {
		var value = get(key);
		return value instanceof Boolean b ? b : (Boolean) DEFAULTS.get(key);
	}

	public Color getDialogueBackground() {
		return color(KEY_DIALOGUE_BACKGROUND);
	}

	public void setDialogueBackground(Color v) {
		set(KEY_DIALOGUE_BACKGROUND, v);
	}

	public Color getDialogueBorder() {
		return color(KEY_DIALOGUE_BORDER);
	}

	public void setDialogueBorder(Color v) {
		set(KEY_DIALOGUE_BORDER, v);
	}

	public int getDialogueBorderWidth() {
		return integer(KEY_DIALOGUE_BORDER_WIDTH);
	}

	public void setDialogueBorderWidth(int v) {
		set(KEY_DIALOGUE_BORDER_WIDTH, v);
	}

	public Color getOptionsBackground() {
		return color(KEY_OPTIONS_BACKGROUND);
	}

	public void setOptionsBackground(Color v) {
		set(KEY_OPTIONS_BACKGROUND, v);
	}

	public Color getOptionsBorder() {
		return color(KEY_OPTIONS_BORDER);
	}

	public void setOptionsBorder(Color v) {
		set(KEY_OPTIONS_BORDER, v);
	}

	public int getOptionsBorderWidth() {
		return integer(KEY_OPTIONS_BORDER_WIDTH);
	}

	public void setOptionsBorderWidth(int v) {
		set(KEY_OPTIONS_BORDER_WIDTH, v);
	}

	public Color getButtonBackground() {
		return color(KEY_BUTTON_BACKGROUND);
	}

	public void setButtonBackground(Color v) {
		set(KEY_BUTTON_BACKGROUND, v);
	}

	public Color getButtonHover() {
		return color(KEY_BUTTON_HOVER);
	}

	public void setButtonHover(Color v) {
		set(KEY_BUTTON_HOVER, v);
	}

	public Color getButtonBorder() {
		return color(KEY_BUTTON_BORDER);
	}

	public void setButtonBorder(Color v) {
		set(KEY_BUTTON_BORDER, v);
	}

	public Color getNavButtonBackground() {
		return color(KEY_NAV_BUTTON_BACKGROUND);
	}

	public void setNavButtonBackground(Color v) {
		set(KEY_NAV_BUTTON_BACKGROUND, v);
	}

	public Color getNavButtonHover() {
		return color(KEY_NAV_BUTTON_HOVER);
	}

	public void setNavButtonHover(Color v) {
		set(KEY_NAV_BUTTON_HOVER, v);
	}

	public Color getNavButtonBorder() {
		return color(KEY_NAV_BUTTON_BORDER);
	}

	public void setNavButtonBorder(Color v) {
		set(KEY_NAV_BUTTON_BORDER, v);
	}

	public Color getOverlayColor() {
		return color(KEY_OVERLAY_COLOR);
	}

	public void setOverlayColor(Color v) {
		set(KEY_OVERLAY_COLOR, v);
	}

	public int getSpeakerFontSize() {
		return integer(KEY_SPEAKER_FONT_SIZE);
	}

	public void setSpeakerFontSize(int v) {
		set(KEY_SPEAKER_FONT_SIZE, v);
	}

	public int getDialogueFontSize() {
		return integer(KEY_DIALOGUE_FONT_SIZE);
	}

	public void setDialogueFontSize(int v) {
		set(KEY_DIALOGUE_FONT_SIZE, v);
	}

	public int getOptionFontSize() {
		return integer(KEY_OPTION_FONT_SIZE);
	}

	public void setOptionFontSize(int v) {
		set(KEY_OPTION_FONT_SIZE, v);
	}

	public int getMargin() {
		return integer(KEY_MARGIN);
	}

	public void setMargin(int v) {
		set(KEY_MARGIN, v);
	}

	public int getPanelHeight() {
		return integer(KEY_PANEL_HEIGHT);
	}

	public void setPanelHeight(int v) {
		set(KEY_PANEL_HEIGHT, v);
	}

	public int getButtonHeight() {
		return integer(KEY_BUTTON_HEIGHT);
	}

	public void setButtonHeight(int v) {
		set(KEY_BUTTON_HEIGHT, v);
	}

	public int getOptionGap() {
		return integer(KEY_OPTION_GAP);
	}

	public void setOptionGap(int v) {
		set(KEY_OPTION_GAP, v);
	}

	public boolean isTypewriterSoundEnabled() {
		return bool(KEY_TYPEWRITER_SOUND_ENABLED);
	}

	public void setTypewriterSoundEnabled(boolean v) {
		set(KEY_TYPEWRITER_SOUND_ENABLED, v);
	}

	public int getTypewriterCharsPerSecond() {
		return integer(KEY_TYPEWRITER_CHARS_PER_SECOND);
	}

	public void setTypewriterCharsPerSecond(int v) {
		set(KEY_TYPEWRITER_CHARS_PER_SECOND, v);
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putInt(KEY_DIALOGUE_BACKGROUND, getDialogueBackground().argb());
		tag.putInt(KEY_DIALOGUE_BORDER, getDialogueBorder().argb());
		tag.putInt(KEY_DIALOGUE_BORDER_WIDTH, getDialogueBorderWidth());
		tag.putInt(KEY_OPTIONS_BACKGROUND, getOptionsBackground().argb());
		tag.putInt(KEY_OPTIONS_BORDER, getOptionsBorder().argb());
		tag.putInt(KEY_OPTIONS_BORDER_WIDTH, getOptionsBorderWidth());
		tag.putInt(KEY_BUTTON_BACKGROUND, getButtonBackground().argb());
		tag.putInt(KEY_BUTTON_HOVER, getButtonHover().argb());
		tag.putInt(KEY_BUTTON_BORDER, getButtonBorder().argb());
		tag.putInt(KEY_NAV_BUTTON_BACKGROUND, getNavButtonBackground().argb());
		tag.putInt(KEY_NAV_BUTTON_HOVER, getNavButtonHover().argb());
		tag.putInt(KEY_NAV_BUTTON_BORDER, getNavButtonBorder().argb());
		tag.putInt(KEY_OVERLAY_COLOR, getOverlayColor().argb());
		tag.putInt(KEY_SPEAKER_FONT_SIZE, getSpeakerFontSize());
		tag.putInt(KEY_DIALOGUE_FONT_SIZE, getDialogueFontSize());
		tag.putInt(KEY_OPTION_FONT_SIZE, getOptionFontSize());
		tag.putInt(KEY_MARGIN, getMargin());
		tag.putInt(KEY_PANEL_HEIGHT, getPanelHeight());
		tag.putInt(KEY_BUTTON_HEIGHT, getButtonHeight());
		tag.putInt(KEY_OPTION_GAP, getOptionGap());
		tag.putBoolean(KEY_TYPEWRITER_SOUND_ENABLED, isTypewriterSoundEnabled());
		tag.putInt(KEY_TYPEWRITER_CHARS_PER_SECOND, getTypewriterCharsPerSecond());
		return tag;
	}

	/**
	 * A named set of style values. Layers are resolved top-down: the first layer that
	 * contains a key wins, otherwise {@link DialogueStyle#DEFAULTS} is used.
	 */
	public static final class StyleLayer {
		private final Map<String, Object> values = new HashMap<>();
		private boolean frozen;

		public StyleLayer() {
		}

		/**
		 * Creates a mutable copy of the given layer (never frozen).
		 */
		public StyleLayer(StyleLayer source) {
			this.values.putAll(source.values);
		}

		public boolean contains(String key) {
			return values.containsKey(key);
		}

		public Object get(String key) {
			return values.get(key);
		}

		public void set(String key, Object value) {
			if (frozen) {
				throw new IllegalStateException("Cannot modify a frozen style layer");
			}
			values.put(key, value);
		}

		public void remove(String key) {
			if (frozen) {
				throw new IllegalStateException("Cannot modify a frozen style layer");
			}
			values.remove(key);
		}

		private StyleLayer freeze() {
			this.frozen = true;
			return this;
		}
	}
}