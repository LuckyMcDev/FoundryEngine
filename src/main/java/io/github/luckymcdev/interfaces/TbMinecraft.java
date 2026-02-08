package io.github.luckymcdev.interfaces;

import net.minecraft.client.Minecraft;

public interface TbMinecraft {
    default Minecraft tb$self() {
        return (Minecraft) this;
    }
}
