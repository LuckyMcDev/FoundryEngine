package io.github.luckymcdev.foundryengine.mixin;

import com.mojang.blaze3d.platform.Window;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiHandler;
import io.github.luckymcdev.foundryengine.common.font.TTFFile;
import io.github.luckymcdev.foundryengine.common.opencl.task.ClWorker;
import io.github.luckymcdev.foundryengine.interfaces.TbMinecraft;
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

    @Override
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V", shift = At.Shift.BEFORE))
    public void tb$init(GameConfig gameConfig, CallbackInfo ci) {
        TTFFile.find(resourceManager);
        ImGuiHandler.create(window.handle());
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/providers/FreeTypeUtil;destroy()V", shift = At.Shift.BEFORE))
    public void tb$close(CallbackInfo ci) {
        ImGuiHandler.dispose();
        ClWorker.shutdown();
    }
}
