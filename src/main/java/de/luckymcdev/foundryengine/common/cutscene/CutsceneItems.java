package de.luckymcdev.foundryengine.common.cutscene;

import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.item.EditorItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.RegisterEvent;

public class CutsceneItems {

    public static Item EDITOR_ITEM;

    public static void onRegister(RegisterEvent event) {
        event.register(BuiltInRegistries.ITEM.key(), helper -> {
            EDITOR_ITEM = ItemBuilder.create(Common.id("editor"))
                    .factory(EditorItem::new)
                    .stacksTo(1)
                    .register(helper);
        });
    }
}
