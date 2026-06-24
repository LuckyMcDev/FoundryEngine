package de.luckymcdev.foundryengine.interfaces;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;

/**
 * Provides access to GPU device backend information.
 */
public interface EngineGpuDevice extends EngineInterface<GpuDevice> {
    /**
     * Returns the GPU device backend type.
     */
    GpuDeviceBackend engine$getBackend();
}
