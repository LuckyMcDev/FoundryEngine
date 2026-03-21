package de.luckymcdev.foundryengine.interfaces;

import com.mojang.blaze3d.systems.GpuDeviceBackend;

public interface EngineGpuDevice {
    GpuDeviceBackend engine$getBackend();
}
