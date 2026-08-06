# Item Tooltips

The **tooltip** system enriches item tooltips in the inventory, powered by `TooltipManager`. With the advanced tooltip enabled (F3+H), it appends debug information to every item stack tooltip.

## What the tooltips show

- **Hold Shift** — fuel burn time (with per-second and per-item ×n estimates) and the item's registry tags, each with a small color icon by tag category.
- **Hold Alt** — a dump of every patched data component on the stack, showing the component id and value.
- **Badges** — item, block, fluid, entity type, enchantment, instrument, painting and banner-pattern tags are resolved from the stack and rendered with icons.

## Custom badges via event

The icons are collected through the `GatherItemTagIconsEvent`, which you can extend from a bundle to add your own tag badges:

```groovy
import de.luckymcdev.foundryengine.client.tooltip.GatherItemTagIconsEvent
import de.luckymcdev.foundryengine.client.tooltip.TooltipTagType
import net.neoforged.neoforge.common.NeoForge

NeoForge.EVENT_BUS.addListener(GatherItemTagIconsEvent.class) { event ->
    // Append a custom tag icon for a tag id
    event.append(TooltipTagType.ITEM, myTagId)
}
```

## Related

- [Editor](editor.md) — inventory and editor panels