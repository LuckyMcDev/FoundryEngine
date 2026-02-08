package io.github.luckymcdev.mixin.imgui;

import com.mojang.blaze3d.platform.Window;
import io.github.luckymcdev.client.imgui.ImGuiImpl;
import io.github.luckymcdev.common.font.TTFFile;
import io.github.luckymcdev.interfaces.TbMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin implements TbMinecraft {

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Shadow
    @Final
    private Window window;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initImGui(GameConfig gameConfig, CallbackInfo ci) {
        TTFFile.find(resourceManager);
        ImGuiImpl.create(window.handle());
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void closeImGui(CallbackInfo ci) {
        ImGuiImpl.dispose();
    }
}
