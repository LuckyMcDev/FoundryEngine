package de.luckymcdev.foundryengine.common.dialogue;

import de.luckymcdev.foundryengine.common.util.color.Color;
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
	public static final StyleLayer DEFAULTS = createDefaults();
	private final Deque<StyleLayer> layers = new ArrayDeque<>();

	public DialogueStyle() {
		layers.push(new StyleLayer(DEFAULTS));
	}

	public static DialogueStyle fromNbt(CompoundTag tag) {
		var s = new DialogueStyle();
		var layer = s.peek();

		if (tag.contains("DialogueBackground")) {
			layer.set("DialogueBackground", new Color(tag.getIntOr("DialogueBackground", 0)));
		}
		if (tag.contains("DialogueBorder")) {
			layer.set("DialogueBorder", new Color(tag.getIntOr("DialogueBorder", 0)));
		}
		if (tag.contains("DialogueBorderWidth")) {
			layer.set("DialogueBorderWidth", tag.getIntOr("DialogueBorderWidth", 0));
		}
		if (tag.contains("OptionsBackground")) {
			layer.set("OptionsBackground", new Color(tag.getIntOr("OptionsBackground", 0)));
		}
		if (tag.contains("OptionsBorder")) {
			layer.set("OptionsBorder", new Color(tag.getIntOr("OptionsBorder", 0)));
		}
		if (tag.contains("OptionsBorderWidth")) {
			layer.set("OptionsBorderWidth", tag.getIntOr("OptionsBorderWidth", 0));
		}
		if (tag.contains("ButtonBackground")) {
			layer.set("ButtonBackground", new Color(tag.getIntOr("ButtonBackground", 0)));
		}
		if (tag.contains("ButtonHover")) {
			layer.set("ButtonHover", new Color(tag.getIntOr("ButtonHover", 0)));
		}
		if (tag.contains("ButtonBorder")) {
			layer.set("ButtonBorder", new Color(tag.getIntOr("ButtonBorder", 0)));
		}
		if (tag.contains("NavButtonBackground")) {
			layer.set("NavButtonBackground", new Color(tag.getIntOr("NavButtonBackground", 0)));
		}
		if (tag.contains("NavButtonHover")) {
			layer.set("NavButtonHover", new Color(tag.getIntOr("NavButtonHover", 0)));
		}
		if (tag.contains("NavButtonBorder")) {
			layer.set("NavButtonBorder", new Color(tag.getIntOr("NavButtonBorder", 0)));
		}
		if (tag.contains("OverlayColor")) {
			layer.set("OverlayColor", new Color(tag.getIntOr("OverlayColor", 0)));
		}
		if (tag.contains("SpeakerFontSize")) {
			layer.set("SpeakerFontSize", tag.getIntOr("SpeakerFontSize", 0));
		}
		if (tag.contains("DialogueFontSize")) {
			layer.set("DialogueFontSize", tag.getIntOr("DialogueFontSize", 0));
		}
		if (tag.contains("OptionFontSize")) {
			layer.set("OptionFontSize", tag.getIntOr("OptionFontSize", 0));
		}
		if (tag.contains("Margin")) {
			layer.set("Margin", tag.getIntOr("Margin", 0));
		}
		if (tag.contains("PanelHeight")) {
			layer.set("PanelHeight", tag.getIntOr("PanelHeight", 0));
		}
		if (tag.contains("ButtonHeight")) {
			layer.set("ButtonHeight", tag.getIntOr("ButtonHeight", 0));
		}
		if (tag.contains("OptionGap")) {
			layer.set("OptionGap", tag.getIntOr("OptionGap", 0));
		}
		if (tag.contains("TypewriterSoundEnabled")) {
			layer.set("TypewriterSoundEnabled", tag.getBooleanOr("TypewriterSoundEnabled", true));
		}
		if (tag.contains("TypewriterCharsPerSecond")) {
			layer.set("TypewriterCharsPerSecond", tag.getIntOr("TypewriterCharsPerSecond", 0));
		}

		return s;
	}

	private static StyleLayer createDefaults() {
		var layer = new StyleLayer();
		layer.set("DialogueBackground", new Color(0xCC101010));
		layer.set("DialogueBorder", new Color(0xFF666666));
		layer.set("DialogueBorderWidth", 2);
		layer.set("OptionsBackground", new Color(0xCC101010));
		layer.set("OptionsBorder", new Color(0xFF666666));
		layer.set("OptionsBorderWidth", 2);
		layer.set("ButtonBackground", new Color(0x88000000));
		layer.set("ButtonHover", new Color(0xAA444444));
		layer.set("ButtonBorder", new Color(0xFF888888));
		layer.set("NavButtonBackground", new Color(0x88000000));
		layer.set("NavButtonHover", new Color(0xAA444444));
		layer.set("NavButtonBorder", new Color(0xFF888888));
		layer.set("OverlayColor", new Color(0x88000000));
		layer.set("SpeakerFontSize", 9);
		layer.set("DialogueFontSize", 9);
		layer.set("OptionFontSize", 9);
		layer.set("Margin", 10);
		layer.set("PanelHeight", 125);
		layer.set("ButtonHeight", 22);
		layer.set("OptionGap", 4);
		layer.set("TypewriterSoundEnabled", true);
		layer.set("TypewriterCharsPerSecond", 45);
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
		return color("DialogueBackground");
	}

	public void setDialogueBackground(Color v) {
		set("DialogueBackground", v);
	}

	public Color getDialogueBorder() {
		return color("DialogueBorder");
	}

	public void setDialogueBorder(Color v) {
		set("DialogueBorder", v);
	}

	public int getDialogueBorderWidth() {
		return integer("DialogueBorderWidth");
	}

	public void setDialogueBorderWidth(int v) {
		set("DialogueBorderWidth", v);
	}

	public Color getOptionsBackground() {
		return color("OptionsBackground");
	}

	public void setOptionsBackground(Color v) {
		set("OptionsBackground", v);
	}

	public Color getOptionsBorder() {
		return color("OptionsBorder");
	}

	public void setOptionsBorder(Color v) {
		set("OptionsBorder", v);
	}

	public int getOptionsBorderWidth() {
		return integer("OptionsBorderWidth");
	}

	public void setOptionsBorderWidth(int v) {
		set("OptionsBorderWidth", v);
	}

	public Color getButtonBackground() {
		return color("ButtonBackground");
	}

	public void setButtonBackground(Color v) {
		set("ButtonBackground", v);
	}

	public Color getButtonHover() {
		return color("ButtonHover");
	}

	public void setButtonHover(Color v) {
		set("ButtonHover", v);
	}

	public Color getButtonBorder() {
		return color("ButtonBorder");
	}

	public void setButtonBorder(Color v) {
		set("ButtonBorder", v);
	}

	public Color getNavButtonBackground() {
		return color("NavButtonBackground");
	}

	public void setNavButtonBackground(Color v) {
		set("NavButtonBackground", v);
	}

	public Color getNavButtonHover() {
		return color("NavButtonHover");
	}

	public void setNavButtonHover(Color v) {
		set("NavButtonHover", v);
	}

	public Color getNavButtonBorder() {
		return color("NavButtonBorder");
	}

	public void setNavButtonBorder(Color v) {
		set("NavButtonBorder", v);
	}

	public Color getOverlayColor() {
		return color("OverlayColor");
	}

	public void setOverlayColor(Color v) {
		set("OverlayColor", v);
	}

	public int getSpeakerFontSize() {
		return integer("SpeakerFontSize");
	}

	public void setSpeakerFontSize(int v) {
		set("SpeakerFontSize", v);
	}

	public int getDialogueFontSize() {
		return integer("DialogueFontSize");
	}

	public void setDialogueFontSize(int v) {
		set("DialogueFontSize", v);
	}

	public int getOptionFontSize() {
		return integer("OptionFontSize");
	}

	public void setOptionFontSize(int v) {
		set("OptionFontSize", v);
	}

	public int getMargin() {
		return integer("Margin");
	}

	public void setMargin(int v) {
		set("Margin", v);
	}

	public int getPanelHeight() {
		return integer("PanelHeight");
	}

	public void setPanelHeight(int v) {
		set("PanelHeight", v);
	}

	public int getButtonHeight() {
		return integer("ButtonHeight");
	}

	public void setButtonHeight(int v) {
		set("ButtonHeight", v);
	}

	public int getOptionGap() {
		return integer("OptionGap");
	}

	public void setOptionGap(int v) {
		set("OptionGap", v);
	}

	public boolean isTypewriterSoundEnabled() {
		return bool("TypewriterSoundEnabled");
	}

	public void setTypewriterSoundEnabled(boolean v) {
		set("TypewriterSoundEnabled", v);
	}

	public int getTypewriterCharsPerSecond() {
		return integer("TypewriterCharsPerSecond");
	}

	public void setTypewriterCharsPerSecond(int v) {
		set("TypewriterCharsPerSecond", v);
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putInt("DialogueBackground", getDialogueBackground().argb());
		tag.putInt("DialogueBorder", getDialogueBorder().argb());
		tag.putInt("DialogueBorderWidth", getDialogueBorderWidth());
		tag.putInt("OptionsBackground", getOptionsBackground().argb());
		tag.putInt("OptionsBorder", getOptionsBorder().argb());
		tag.putInt("OptionsBorderWidth", getOptionsBorderWidth());
		tag.putInt("ButtonBackground", getButtonBackground().argb());
		tag.putInt("ButtonHover", getButtonHover().argb());
		tag.putInt("ButtonBorder", getButtonBorder().argb());
		tag.putInt("NavButtonBackground", getNavButtonBackground().argb());
		tag.putInt("NavButtonHover", getNavButtonHover().argb());
		tag.putInt("NavButtonBorder", getNavButtonBorder().argb());
		tag.putInt("OverlayColor", getOverlayColor().argb());
		tag.putInt("SpeakerFontSize", getSpeakerFontSize());
		tag.putInt("DialogueFontSize", getDialogueFontSize());
		tag.putInt("OptionFontSize", getOptionFontSize());
		tag.putInt("Margin", getMargin());
		tag.putInt("PanelHeight", getPanelHeight());
		tag.putInt("ButtonHeight", getButtonHeight());
		tag.putInt("OptionGap", getOptionGap());
		tag.putBoolean("TypewriterSoundEnabled", isTypewriterSoundEnabled());
		tag.putInt("TypewriterCharsPerSecond", getTypewriterCharsPerSecond());
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
