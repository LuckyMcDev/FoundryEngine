package io.github.luckymcdev.foundryengine.common.vpacks.json.advancement;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class DisplayInfoBuilder {
    private Component title;
    private Component description;
    private ItemStack icon;
    private ClientAsset.ResourceTexture background;
    private AdvancementType type;
    private boolean showToast;
    private boolean announceChat;
    private boolean hidden;

    public DisplayInfoBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public DisplayInfoBuilder description(Component description) {
        this.description = description;
        return this;
    }

    public DisplayInfoBuilder icon(ItemStack itemStack) {
        this.icon = itemStack;
        return this;
    }

    public DisplayInfoBuilder icon(Item item) {
        this.icon = new ItemStack(item);
        return this;
    }

    public DisplayInfoBuilder background(ClientAsset.ResourceTexture background) {
        this.background = background;
        return this;
    }

    public DisplayInfoBuilder type(AdvancementType type) {
        this.type = type;
        return this;
    }

    public DisplayInfoBuilder showToast(boolean showToast) {
        this.showToast = showToast;
        return this;
    }

    public DisplayInfoBuilder announceToChat(boolean announceToChat) {
        this.announceChat = announceToChat;
        return this;
    }

    public DisplayInfoBuilder hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public DisplayInfo build() {
        return new DisplayInfo(this.icon, this.title, this.description, Optional.ofNullable(this.background), this.type,
                this.showToast, this.announceChat, this.hidden);
    }
}
