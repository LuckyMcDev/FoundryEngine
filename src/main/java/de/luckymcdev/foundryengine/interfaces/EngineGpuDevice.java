package de.luckymcdev.foundryengine.interfaces;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;

public interface EngineGpuDevice extends EngineInterface<GpuDevice> {
    GpuDeviceBackend engine$getBackend();
}
