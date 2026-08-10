package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Manages the lifecycle of item icon textures rendered offscreen via {@link OffscreenRenderer}.
 * <p>
 * Call order each frame:
 * <ol>
 *   <li>{@link #processQueue(OffscreenRenderer)} — drains GPU readbacks and starts new renders</li>
 *   <li>ImGui rendering — {@link #get} returns cached {@link ImTexture}s</li>
 * </ol>
 */
public class ItemIconCache {
	private static final int MAX_PER_FRAME = 25;
	private final Map<String, ImTexture> cache = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, ImTexture> eldest) {
			boolean shouldRemove = size() > getDynamicMaxCacheSize();
			if (shouldRemove) {
				eldest.getValue().close();
				pending.remove(eldest.getKey());
			}
			return shouldRemove;
		}
	};
	private final Set<String> pending = new HashSet<>();
	private final Queue<ItemStack> queue = new ArrayDeque<>();

	private static String cacheKey(ItemStack stack, int size) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "@" + size;
	}

	private static int sizeFromKey(String key) {
		int at = key.lastIndexOf('@');
		if (at < 0) {
			return 64;
		}
		try {
			return Integer.parseInt(key.substring(at + 1));
		} catch (NumberFormatException e) {
			return 64;
		}
	}

	/**
	 * Returns the cached icon for {@code stack}, or {@code null} if it hasn't been
	 * rendered yet (it will be queued automatically).
	 */
	public @Nullable ImTexture get(ItemStack stack) {
		int size = ClientConfig.ICON_SIZE.get();
		String key = cacheKey(stack, size);

		ImTexture cached = cache.get(key);
		if (cached != null) {
			return cached;
		}

		if (pending.add(key)) {
			queue.add(stack);
		}
		return null;
	}

	/**
	 * Drains arrived GPU readbacks from {@code renderer} into the cache, then
	 * kicks off up to {@value MAX_PER_FRAME} new render jobs.
	 * Call this once per frame before any ImGui rendering.
	 */
	public void processQueue(OffscreenRenderer renderer) {
		renderer.drainPendingResults(result -> {
			NativeImage image = result.image();
			String key = result.key();
			int size = sizeFromKey(key);
			DynamicTexture tex = new DynamicTexture(() -> "item_icon_" + key, image);
			cache.put(key, new ImTexture(tex, true, size, size));
			pending.remove(key);
		});

		int dispatched = 0;
		var mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		while (!queue.isEmpty() && dispatched < MAX_PER_FRAME) {
			ItemStack stack = queue.poll();
			int size = ClientConfig.ICON_SIZE.get();
			String key = cacheKey(stack, size);
			if (cache.containsKey(key)) {
				continue;
			}

			renderer.renderAsync(size, key, (colorView, depthView, projBuf, s) -> {
				//? if 26.1 {
				Projection projection = new Projection();
				projection.setupOrtho(-1000.0F, 1000.0F, s, s, true);
				RenderSystem.backupProjectionMatrix();
				RenderSystem.setProjectionMatrix(projBuf.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

				var resolver = mc.getItemModelResolver();
				var submitNodes = mc.gameRenderer.getSubmitNodeStorage();
				var features = mc.gameRenderer.getFeatureRenderDispatcher();
				var buffers = mc.renderBuffers().bufferSource();
				var lighting = mc.gameRenderer.getLighting();

				TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
				resolver.updateForTopItem(renderState, stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);

				Lighting.Entry lightEntry = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
				lighting.setupFor(lightEntry);

				PoseStack pose = new PoseStack();
				pose.translate(s / 2.0F, s / 2.0F, 0.0F);
				pose.scale(s, -s, s);
				renderState.submit(pose, submitNodes, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);

				features.renderAllFeatures();
				buffers.endBatch();
				RenderSystem.restoreProjectionMatrix();
				//?} else {
                /*Projection projection = new Projection();
                projection.setupOrtho(-1000.0F, 1000.0F, s, s, true);
                RenderSystem.backupProjectionMatrix();
                RenderSystem.setProjectionMatrix(projBuf.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

                var resolver = mc.getItemModelResolver();
                var submitNodes = new net.minecraft.client.renderer.SubmitNodeStorage();
                var features = mc.gameRenderer.featureRenderDispatcher();
                var lighting = mc.gameRenderer.lighting();

                TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
                resolver.updateForTopItem(renderState, stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);

                Lighting.Entry lightEntry = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
                lighting.setupFor(lightEntry);

                PoseStack pose = new PoseStack();
                pose.translate(s / 2.0F, s / 2.0F, 0.0F);
                pose.scale(s, -s, s);
                renderState.submit(pose, submitNodes, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);

                features.renderAllFeatures(submitNodes);
                RenderSystem.restoreProjectionMatrix();
                *///?}
			});
			dispatched++;
		}

		if (queue.isEmpty() && pending.isEmpty()) {
			evict();
		}
	}

	public void clear() {
		queue.clear();
		pending.clear();
		for (ImTexture tex : cache.values()) {
			tex.close();
		}
		cache.clear();
	}

	private void evict() {
		int maxSize = getDynamicMaxCacheSize();
		while (cache.size() > maxSize) {
			Iterator<Map.Entry<String, ImTexture>> it = cache.entrySet().iterator();
			Map.Entry<String, ImTexture> oldest = it.next();
			it.remove();
			oldest.getValue().close();
			pending.remove(oldest.getKey());
		}
	}

	/**
	 * Idk how good this is but it exists now
	 */
	private int getDynamicMaxCacheSize() {
		int iconSize = ClientConfig.ICON_SIZE.get();
		long bytesPerIcon = (long) iconSize * iconSize * 4L + 2048L;
		long maxHeap = Runtime.getRuntime().maxMemory();
		long allowedMemory = (long) (maxHeap * 0.12);
		int calculated = (int) (allowedMemory / bytesPerIcon);
		return Math.clamp(calculated, 25, 1500);
	}
}