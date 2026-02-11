package io.github.luckymcdev.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface TbMinecraft {
    default Minecraft tb$self() {
        return (Minecraft) this;
    }
    void tb$init(GameConfig gameConfig, CallbackInfo ci);
}
