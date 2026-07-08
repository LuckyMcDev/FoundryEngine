# Markdown rendering

FoundryEngine can render GitHub-Flavored Markdown as formatted Minecraft `Component` text for display in a scrollable in-game screen.

## MdScreen

The `MdScreen` class provides several constructors:

```groovy
import de.luckymcdev.foundryengine.common.md.MdScreen

// From a string
Minecraft.getInstance().setScreen(new MdScreen(
    Component.literal("Help"),
    "# Welcome\nThis is **bold** and *italic* text."
))

// From a file path
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

## Supported markdown features

| Element         | Markdown      | Rendering                         |
|-----------------|---------------|-----------------------------------|
| Heading H1-H6   | `# Title`     | Bold with decreasing brightness   |
| Bold            | `**text**`    | Bold styling                      |
| Italic          | `*text*`      | Italic styling                    |
| Strikethrough   | `~~text~~`    | Strikethrough                     |
| Inline Code     | `` `code` ``  | Gray color `0xE6E6E6`             |
| Code Blocks     | ` ``` `       | Gray color, monospace             |
| Links           | `[text](url)` | Underlined blue, clickable        |
| Images          | `![alt](url)` | `[Image: alt]` with clickable URL |
| Block Quotes    | `> text`      | Prefixed with `│` in gray         |
| Unordered Lists | `- item`      | `•` bullet markers                |
| Ordered Lists   | `1. item`     | Numbered markers                  |
| Tables          | `\| col \|`   | `│` separators, bold headers      |
| Thematic Break  | `---`         | `─` repeated 50 times             |

## Use cases

- **In-game documentation** -- Display help text, guides, or changelogs
- **Tutorials** -- Show interactive tutorials with clickable links
- **Debug output** -- View formatted registry dumps
- **Bundle documentation** -- Readme files displayed from within the game

## See also

- [Commands](commands) -- `/engine dump` outputs registry data as markdown
