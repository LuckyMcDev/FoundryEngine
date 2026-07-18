# Editor Themes

FoundryEngine's Dear ImGui editor supports custom themes via the `ImTheme` interface.

## ImTheme interface

```java
public interface ImTheme {
    String getName();
    void applyTheme(ImGuiStyle style);
}
```

Helper methods available:

| Method                      | What it does            |
|-----------------------------|-------------------------|
| `col(style, col, color)`    | Set an ImGui color      |
| `padding(style, x, y)`      | Set window padding      |
| `framePadding(style, x, y)` | Set frame padding       |
| `rounding(style, ...)`      | Set all rounding values |
| `borders(style, ...)`       | Set all border sizes    |

## Built-in themes

| Theme           | Default? |
|-----------------|----------|
| BessDark        | Yes      |
| ModernDark      | No       |
| Dark            | No       |
| Cherry          | No       |
| CatppuccinMocha | No       |
| Vidlib          | No       |
| Veil            | No       |

Switch themes from the editor's **View > Theme Selector Panel**.

## Creating a custom theme

```java
public class MyCustomTheme implements ImTheme {
    @Override
    public String getName() {
        return "MyCustomTheme";
    }

    @Override
    public void applyTheme(ImGuiStyle style) {
        col(style, ImGuiCol.WindowBg, new Color(0xFF1a1a2e));
        col(style, ImGuiCol.Button, 0.2f, 0.3f, 0.8f);
        padding(style, 8.0f, 8.0f);
        rounding(style, 4.0f, 4.0f, 2.0f, 4.0f, 4.0f, 4.0f, 4.0f);
    }
}
```

Register your theme by adding it to `ImThemes.ALL`.

## Next

- [Editor](../systems/editor.md) — full editor documentation
