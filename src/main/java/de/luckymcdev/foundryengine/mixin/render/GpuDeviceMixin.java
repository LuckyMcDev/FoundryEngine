package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import de.luckymcdev.foundryengine.interfaces.EngineGpuDevice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuDevice.class)
public class GpuDeviceMixin implements EngineGpuDevice {

    @Shadow
    @Final
    private GpuDeviceBackend backend;

    @Override
    public GpuDeviceBackend engine$getBackend() {
        return backend;
    }
}
