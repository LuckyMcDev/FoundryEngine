# Editor themes

FoundryEngine's Dear ImGui editor supports theming via the `ImTheme` interface. Seven built-in themes are available, and custom themes can be created and registered.

## ImTheme Interface

**Package:** `de.luckymcdev.foundryengine.client.editor.styles.ImTheme`

Every theme implements this interface:

| Method                   | Description                            |
|--------------------------|----------------------------------------|
| `getName()`              | Returns the display name of the theme  |
| `applyTheme(ImGuiStyle)` | Applies the theme's colors and styling |

Helper methods available in the interface:

| Method                                                               | Description                                  |
|----------------------------------------------------------------------|----------------------------------------------|
| `col(style, col, color)`                                             | Set an ImGui color (from Color or ARGB/rgba) |
| `padding(style, x, y)`                                               | Set window padding                           |
| `framePadding(style, x, y)`                                          | Set frame padding                            |
| `itemSpacing(style, x, y)`                                           | Set item spacing                             |
| `itemInnerSpacing(style, x, y)`                                      | Set item inner spacing                       |
| `rounding(style, window, frame, grab, tab, popup, scrollbar, child)` | Set all rounding values                      |
| `borders(style, window, frame, popup, child, tab)`                   | Set all border sizes                         |

## Built-in Themes

All themes are registered in `ImThemes` (package `de.luckymcdev.foundryengine.client.editor.styles`):

| Theme               | Class                 | Default? |
|---------------------|-----------------------|----------|
| **BessDark**        | `BessDarkTheme`       | Yes      |
| **ModernDark**      | `ModernDarkTheme`     | No       |
| **Dark**            | `DarkTheme`           | No       |
| **Cherry**          | `CherryTheme`         | No       |
| **CatppuccinMocha** | `CatpuccinMochaTheme` | No       |
| **Vidlib**          | `VidlibTheme`         | No       |
| **Veil**            | `VeilTheme`           | No       |

### ThemeSelectorPanel

The **Theme Selector Panel** (menu: `View > Theme Selector Panel`) lets users switch between all built-in themes at runtime. The selected theme is persisted through `ClientConfig.SELECTED_THEME`.

## Config Persistence

The active theme is saved in the client config:

```java
// ClientConfig.java
public static final ModConfigSpec.ConfigValue<String> SELECTED_THEME;
```

Default value is the name of `BessDarkTheme`. Available theme names are retrieved via `ImThemes.getAvailableThemeNames()`.

## Creating a Custom Theme

Implement the `ImTheme` interface and register it:

```java
public class MyCustomTheme implements ImTheme {
    @Override
    public String getName() {
        return "MyCustomTheme";
    }

    @Override
    public void applyTheme(ImGuiStyle style) {
        // Set colors
        col(style, ImGuiCol.WindowBg, new Color(0xFF1a1a2e));
        col(style, ImGuiCol.Button, 0.2f, 0.3f, 0.8f);

        // Set padding
        padding(style, 8.0f, 8.0f);

        // Set rounding
        rounding(style, 4.0f, 4.0f, 2.0f, 4.0f, 4.0f, 4.0f, 4.0f);

        // Set borders
        borders(style, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
```

Themes can be registered by adding to `ImThemes.ALL` or via the theme registration system.

## See also

- [Editor](../systems/editor) -- Full editor documentation and panel listing
- [Config](../core/config) -- ClientConfig reference
