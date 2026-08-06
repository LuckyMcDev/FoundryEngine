package de.luckymcdev.foundryengine.client.editor.styles;

import de.luckymcdev.foundryengine.client.editor.styles.builtin.BessDarkTheme;
import de.luckymcdev.foundryengine.client.editor.styles.builtin.CatpuccinMochaTheme;
import de.luckymcdev.foundryengine.client.editor.styles.builtin.CherryTheme;
import de.luckymcdev.foundryengine.client.editor.styles.builtin.DarkTheme;
import de.luckymcdev.foundryengine.client.editor.styles.builtin.ModernDarkTheme;
import de.luckymcdev.foundryengine.client.editor.styles.builtin.VeilTheme;
import de.luckymcdev.foundryengine.client.editor.styles.builtin.VidlibTheme;
import org.jspecify.annotations.Nullable;

import java.util.List;

public enum ImThemes {
	BESS_DARK(new BessDarkTheme()),
	CATPUCCIN_MOCHA(new CatpuccinMochaTheme()),
	MODERN_DARK(new ModernDarkTheme()),
	DARK(new DarkTheme()),
	CHERRY(new CherryTheme()),
	VIDLIB(new VidlibTheme()),
	VEIL(new VeilTheme());

	public static final List<ImTheme> ALL = List.of(
		BESS_DARK.theme, CATPUCCIN_MOCHA.theme, MODERN_DARK.theme, DARK.theme, CHERRY.theme, VIDLIB.theme, VEIL.theme
	);

	private final ImTheme theme;

	ImThemes(ImTheme theme) {
		this.theme = theme;
	}

	public static String getAvailableThemeNames() {
		StringBuilder names = new StringBuilder();
		for (ImTheme theme : ImThemes.ALL) {
			if (!names.isEmpty()) {
				names.append(", ");
			}
			names.append(theme.getName());
		}
		return names.toString();
	}

	public static @Nullable ImThemes get(ImTheme theme) {
		for (ImThemes imThemes : ImThemes.values()) {
			if (imThemes.theme == theme) {
				return imThemes;
			}
		}
		return null;
	}

	public ImTheme getTheme() {
		return theme;
	}
}
