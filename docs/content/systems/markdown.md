# Markdown Rendering

FoundryEngine can render GitHub-Flavored Markdown as formatted Minecraft text for in-game display.

## Displaying markdown

```groovy
import de.luckymcdev.foundryengine.common.md.MdScreen

// From a string
Minecraft.getInstance().setScreen(new MdScreen(
        Component.literal("Help"),
        "# Welcome\nThis is **bold** and *italic* text."
))

// From a file
Minecraft.getInstance().setScreen(new MdScreen(
        Component.literal("Documentation"),
        Paths.get("config/FoundryEngine/docs/readme.md")
))

// From a resource location
Minecraft.getInstance().setScreen(new MdScreen(
        Component.literal("Guide"),
        ResourceLocation.parse("foundryengine:md/guide.md")
))
```

## Supported features

| Element       | Markdown             | Result                          |
|---------------|----------------------|---------------------------------|
| Headings      | `# Title`            | Bold with decreasing brightness |
| Bold          | `**text**`           | Bold                            |
| Italic        | `*text*`             | Italic                          |
| Strikethrough | `~~text~~`           | Strikethrough                   |
| Code          | `` `code` ``         | Gray monospace                  |
| Code blocks   | ` ``` `              | Gray monospace block            |
| Links         | `[text](url)`        | Underlined blue, clickable      |
| Lists         | `- item` / `1. item` | Bullet/numbered lists           |
| Tables        | `\| col \|`          | Formatted table                 |
| Block quotes  | `> text`             | Gray with `│` prefix            |

## Use cases

- In-game documentation and help screens
- Tutorials with clickable links
- Bundle readme files displayed in-game
- Debug output formatting

## Next

- [Commands](commands.md) — `/engine dump` outputs markdown
