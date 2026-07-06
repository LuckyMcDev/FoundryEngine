package de.luckymcdev.foundryengine.mixin;

import com.mojang.blaze3d.platform.Window;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.interfaces.EngineMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to enable things when Minecraft starts, and to remove things when Minecraft closes.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin implements EngineMinecraft {

	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Shadow
	@Final
	private Window window;

	@Shadow
	@Final
	private SimpleGizmoCollector perTickGizmos;

	@Override
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeGui()V", shift = At.Shift.BEFORE))
	public void engine$init(GameConfig gameConfig, CallbackInfo ci) {
		Client.getImGuiManager().create(window.handle(), resourceManager);
		Client.getMainMenu().register();
	}

	@Override
	@Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/providers/FreeTypeUtil;destroy()V", shift = At.Shift.BEFORE))
	public void engine$close(CallbackInfo ci) {
		Client.getImGuiManager().free();
	}

	@Override
	public SimpleGizmoCollector engine$perTickGizmos() {
		return perTickGizmos;
	}
}
