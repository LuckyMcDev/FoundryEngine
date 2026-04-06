package de.luckymcdev.foundryengine.client.editor.styles;

import de.luckymcdev.foundryengine.client.editor.styles.builtin.*;

import java.util.List;

public class ImThemes {
    //This is the default theme.
    public static final BessDarkTheme BESS_DARK_IM_THEME = new BessDarkTheme();
    public static final CatpuccinMochaTheme CATPUCCIN_MOCHA_IM_THEME = new CatpuccinMochaTheme();
    public static final ModernDarkTheme MODERN_DARK_IM_THEME = new ModernDarkTheme();
    public static final DarkTheme DARK_IM_THEME = new DarkTheme();
    public static final CherryTheme CHERRY_IM_THEME = new CherryTheme();
    public static final VidlibTheme VIDLIB_IM_THEME = new VidlibTheme();
    public static final VeilTheme VEIL_IM_THEME = new VeilTheme();

    public static final List<ImTheme> ALL = List.of(
            BESS_DARK_IM_THEME, CATPUCCIN_MOCHA_IM_THEME, MODERN_DARK_IM_THEME, DARK_IM_THEME, CHERRY_IM_THEME, VIDLIB_IM_THEME, VEIL_IM_THEME
    );

    public static String getAvailableThemeNames() {
        StringBuilder names = new StringBuilder();
        for (ImTheme theme : ImThemes.ALL) {
            if (!names.isEmpty()) names.append(", ");
            names.append(theme.getName());
        }
        return names.toString();
    }

    public static ImTheme getThemeByName(String themeName) {
        for (ImTheme theme : ImThemes.ALL) {
            if (theme.getName().equalsIgnoreCase(themeName)) {
                return theme;
            }
        }
        return ImThemes.BESS_DARK_IM_THEME;
    }
}
