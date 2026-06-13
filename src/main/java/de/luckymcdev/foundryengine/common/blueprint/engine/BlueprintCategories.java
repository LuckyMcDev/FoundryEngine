package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.common.util.color.Color;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BlueprintCategories {
    private static final Map<String, Color> COLORS = new LinkedHashMap<>();

    private BlueprintCategories() {
    }

    public static String define(String path, Color color) {
        COLORS.put(path, color);
        return path;
    }

    public static Color color(@Nullable String path) {
        if (path == null) return new Color(0xFF_404040);
        Color c = COLORS.get(path);
        if (c != null) return c;
        int slash = path.indexOf('/');
        String key = slash == -1 ? path : path.substring(0, slash);
        c = COLORS.get(key);
        return c != null ? c : new Color(0xFF_404040);
    }
}
