package de.luckymcdev.foundryengine.interfaces.render;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;

/**
 * Provides access to GPU device backend information.
 */
public interface EngineGpuDevice extends EngineInterface<GpuDevice> {
	/**
	 * Returns the GPU device backend type.
	 */
	default GpuDeviceBackend engine$getBackend() {
		throw new NoMixinException(this);
	}
}
