package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

//? if 26.1 {
//?}
//? if 26.2 {
/*import com.mojang.blaze3d.GpuFormat;
 *///?}

/**
 * Owns a reusable offscreen framebuffer and provides a general API for rendering
 * arbitrary content into a {@link NativeImage} that can then be turned into a texture.
 * <p>
 * GPU readbacks are async; results are queued and must be drained on the render thread
 * by calling {@link #drainPendingResults()} at the start of each frame.
 */
public class OffscreenRenderer {
	private final Queue<PendingResult> pendingResults = new ArrayDeque<>();
	private @Nullable GpuTexture colorTex;
	private @Nullable GpuTextureView colorView;
	private @Nullable GpuTexture depthTex;
	private @Nullable GpuTextureView depthView;
	private @Nullable ProjectionMatrixBuffer projBuf;
	private int currentSize;

	/**
	 * Renders content into the offscreen framebuffer and schedules a GPU readback.
	 * The resulting {@link NativeImage} will appear in {@link #drainPendingResults()}
	 * on the next frame where the GPU has finished.
	 *
	 * @param size the pixel dimensions of the render target (square)
	 * @param key  an opaque key forwarded to the pending result for identification
	 * @param job  the render job; receives the active color/depth views and proj buffer
	 */
	public void renderAsync(int size, String key, RenderJob job) {
		ensure(size);
		var device = RenderSystem.getDevice();

		//? if 26.1 {
		device.createCommandEncoder().clearColorAndDepthTextures(colorTex, 0, depthTex, 1.0);
		//?} else {
		/*device.createCommandEncoder().clearColorAndDepthTextures(colorTex, new org.joml.Vector4f(0, 0, 0, 0), depthTex, 1.0);
		 *///?}

		RenderSystem.outputColorTextureOverride = colorView;
		RenderSystem.outputDepthTextureOverride = depthView;

		job.render(colorView, depthView, projBuf, size);

		RenderSystem.outputColorTextureOverride = null;
		RenderSystem.outputDepthTextureOverride = null;

		//? if 26.1 {
		int pixelSize = TextureFormat.RGBA8.pixelSize();
		GpuBuffer readBuffer = device.createBuffer(() -> "offscreen_read_" + key,
			GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) size * size * pixelSize);
		CommandEncoder enc = device.createCommandEncoder();
		device.createCommandEncoder().copyTextureToBuffer(colorTex, readBuffer, 0, () -> {
			try (var mapped = enc.mapBuffer(readBuffer, true, false)) {
				//?} else {
        /*int pixelSize = GpuFormat.RGBA8_UNORM.blockSize();
        GpuBuffer readBuffer = device.createBuffer(() -> "offscreen_read_" + key,
            GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) size * size * pixelSize);
        device.createCommandEncoder().copyTextureToBuffer(colorTex, readBuffer, 0, () -> {
            try (var mapped = readBuffer.map(true, false)) {
        *///?}
				NativeImage image = new NativeImage(size, size, false);
				for (int y = 0; y < size; y++) {
					for (int x = 0; x < size; x++) {
						int pixel = mapped.data().getInt((x + y * size) * pixelSize);
						image.setPixelABGR(x, size - y - 1, pixel);
					}
				}
				pendingResults.add(new PendingResult(key, image));
			} catch (Exception e) {
				Common.LOGGER.error("Offscreen readback failed for key {}", key, e);
			}
			readBuffer.close();
		}, 0);
	}

	/**
	 * Drains all GPU readback results that have arrived since the last call.
	 * Must be called on the render thread at the start of each frame, before any
	 * texture uploads or ImGui rendering.
	 *
	 * @param consumer receives each completed {@link PendingResult}
	 */
	public void drainPendingResults(Consumer<PendingResult> consumer) {
		PendingResult result;
		while ((result = pendingResults.poll()) != null) {
			consumer.accept(result);
		}
	}

	private void ensure(int size) {
		if (currentSize == size && colorTex != null && !colorTex.isClosed()) {
			return;
		}
		close();
		currentSize = size;
		var device = RenderSystem.getDevice();
		//? if 26.1 {
		colorTex = device.createTexture(() -> "offscreen_color", 13, TextureFormat.RGBA8, size, size, 1, 1);
		colorView = device.createTextureView(colorTex);
		depthTex = device.createTexture(() -> "offscreen_depth", 9, TextureFormat.DEPTH32, size, size, 1, 1);
		depthView = device.createTextureView(depthTex);
		//?} else {
        /*colorTex = device.createTexture(() -> "offscreen_color", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST, GpuFormat.RGBA8_UNORM, size, size, 1, 1);
        colorView = device.createTextureView(colorTex);
        depthTex = device.createTexture(() -> "offscreen_depth", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST, GpuFormat.D32_FLOAT, size, size, 1, 1);
        depthView = device.createTextureView(depthTex);
        *///?}
		projBuf = new ProjectionMatrixBuffer("offscreen_proj");
	}

	public void close() {
		if (colorTex != null) {
			colorTex.close();
			colorTex = null;
		}
		if (colorView != null) {
			colorView.close();
			colorView = null;
		}
		if (depthTex != null) {
			depthTex.close();
			depthTex = null;
		}
		if (depthView != null) {
			depthView.close();
			depthView = null;
		}
		if (projBuf != null) {
			projBuf.close();
			projBuf = null;
		}
		currentSize = 0;
	}

	public interface RenderJob {
		void render(GpuTextureView colorTarget, GpuTextureView depthTarget, ProjectionMatrixBuffer projBuf, int size);
	}

	public record PendingResult(String key, NativeImage image) {
	}
}