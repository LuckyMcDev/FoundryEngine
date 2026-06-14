package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.common.util.color.Color;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BlueprintCategories {
    private static final Map<String, Color> COLORS = new LinkedHashMap<>();

    static {
        define("Events", new Color(0xFF_5B7FBA));
        define("Events/Setup", new Color(0xFF_6B8FCA));
        define("Events/Server", new Color(0xFF_E67E22));
        define("Events/Client", new Color(0xFF_3498DB));
        define("Events/Block", new Color(0xFF_8E44AD));
        define("Events/Entity", new Color(0xFF_27AE60));
        define("Events/Player", new Color(0xFF_2ECC71));
        define("Events/Item", new Color(0xFF_E74C3C));
        define("Events/Level", new Color(0xFF_1ABC9C));
        define("Events/Misc", new Color(0xFF_95A5A6));
        define("Execute", new Color(0xFF_E67E22));
        define("Teleport", new Color(0xFF_9B59B6));
        define("Interaction", new Color(0xFF_3498DB));
        define("World", new Color(0xFF_27AE60));
        define("Info", new Color(0xFF_1ABC9C));
        define("Admin", new Color(0xFF_E74C3C));
        define("Flow", new Color(0xFF_F39C12));
        define("Math", new Color(0xFF_2ECC71));
        define("String", new Color(0xFF_3498DB));
        define("Variables", new Color(0xFF_9B59B6));
        define("Comparison", new Color(0xFF_E74C3C));
        define("Utility", new Color(0xFF_95A5A6));
        define("Entity", new Color(0xFF_27AE60));
        define("Data", new Color(0xFF_2980B9));
        define("Triggers", new Color(0xFF_E74C3C));
        define("Reflection", new Color(0xFF_8E44AD));
        define("Builder", new Color(0xFF_E67E22));
        define("Builder/Item", new Color(0xFF_E67E22));
        define("Builder/Block", new Color(0xFF_D35400));
        define("Builder/Recipe", new Color(0xFF_F39C12));
        define("Builder/Sound", new Color(0xFF_1ABC9C));
        define("Builder/Particle", new Color(0xFF_9B59B6));
    }

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
