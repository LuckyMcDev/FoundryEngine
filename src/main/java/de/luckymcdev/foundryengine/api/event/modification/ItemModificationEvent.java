package de.luckymcdev.foundryengine.api.event.modification;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;

public class ItemModificationEvent extends Event {

    private final Item item;

    public ItemModificationEvent(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    // TODO
}