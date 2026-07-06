package de.luckymcdev.foundryengine.client.imgui;

import de.luckymcdev.foundryengine.common.util.color.Color;

public enum ImColorVariant {
	DEFAULT("default", "Default", new Color(0x664296FA), new Color(0xFF4296FA), new Color(0xFF0F87FA), Color.WHITE),
	GRAY("gray", "Gray", new Color(166, 166, 166, 150), new Color(166, 166, 166, 255), new Color(191, 191, 191, 255), Color.WHITE),
	RED("red", "Red", Color.hsb(0, 0.8F, 0.8F, 150), Color.hsb(0, 0.8F, 0.8F, 255), Color.hsb(0, 0.9F, 0.9F, 255), Color.hsb(0, 0.67F, 1.0F, 255)),
	ORANGE("orange", "Orange", Color.hsb(30.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(30.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(30.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(30.0F / 360.0F, 0.67F, 1.0F, 255)),
	YELLOW("yellow", "Yellow", Color.hsb(55.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(55.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(55.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(55.0F / 360.0F, 0.67F, 1.0F, 255)),
	LIME("lime", "Lime", Color.hsb(84.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(84.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(84.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(84.0F / 360.0F, 0.67F, 1.0F, 255)),
	GREEN("green", "Green", Color.hsb(124.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(124.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(124.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(124.0F / 360.0F, 0.67F, 1.0F, 255)),
	TEAL("teal", "Teal", Color.hsb(165.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(165.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(165.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(165.0F / 360.0F, 0.67F, 1.0F, 255)),
	CYAN("cyan", "Cyan", Color.hsb(180.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(180.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(180.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(180.0F / 360.0F, 0.67F, 1.0F, 255)),
	BLUE("blue", "Blue", Color.hsb(205.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(205.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(205.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(205.0F / 360.0F, 0.67F, 1.0F, 255)),
	DARK_BLUE("dark_blue", "Dark Blue", Color.hsb(225.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(225.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(225.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(225.0F / 360.0F, 0.67F, 1.0F, 255)),
	DARK_PURPLE("dark_purple", "Dark Purple", Color.hsb(255.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(255.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(255.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(255.0F / 360.0F, 0.67F, 1.0F, 255)),
	PURPLE("purple", "Purple", Color.hsb(280.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(280.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(280.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(280.0F / 360.0F, 0.67F, 1.0F, 255)),
	MAGENTA("magenta", "Magenta", Color.hsb(300.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(300.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(300.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(300.0F / 360.0F, 0.67F, 1.0F, 255)),
	ROSE("rose", "Rose", Color.hsb(330.0F / 360.0F, 0.8F, 0.8F, 150), Color.hsb(330.0F / 360.0F, 0.8F, 0.8F, 255), Color.hsb(330.0F / 360.0F, 0.9F, 0.9F, 255), Color.hsb(330.0F / 360.0F, 0.67F, 1.0F, 255));

	public static final ImColorVariant[] VALUES = values();

	public final String id;
	public final String displayName;
	public final Color color;
	public final Color hoverColor;
	public final Color activeColor;
	public final Color textColor;

	ImColorVariant(String id, String displayName, Color color, Color hoverColor, Color activeColor, Color textColor) {
		this.id = id;
		this.displayName = displayName;
		this.color = color;
		this.hoverColor = hoverColor;
		this.activeColor = activeColor;
		this.textColor = textColor;
	}
}
