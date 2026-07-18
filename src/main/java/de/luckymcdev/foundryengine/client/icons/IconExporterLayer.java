package de.luckymcdev.foundryengine.client.icons;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.config.CommonConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class IconExporterLayer implements GuiLayer {
	private static final int ITEMS_PER_BATCH = 256;
	private static IconExporterLayer instance;
	private final Queue<ImageExportUtil.ItemExportData> pendingItems = new ArrayDeque<>();
	private int totalItems;
	private boolean exporting;
	private int imageSize;

	public IconExporterLayer() {
		instance = this;
	}

	public static IconExporterLayer getInstance() {
		return instance;
	}

	public boolean isExporting() {
		return exporting;
	}

	public boolean hasWork() {
		return !pendingItems.isEmpty();
	}

	public void startExport(HolderLookup.Provider lookup, @Nullable String modIdFilter, boolean modIdRegex) {
		this.imageSize = ClientConfig.ICON_SIZE.get();
		this.pendingItems.clear();
		this.exporting = true;

		File outputDir = Common.CACHE.resolve("icons").resolve(String.valueOf(imageSize)).toFile();

		for (Item item : BuiltInRegistries.ITEM.stream().toList()) {
			Identifier id = BuiltInRegistries.ITEM.getKey(item);
			ItemStack stack = new ItemStack(item);

			if (modIdFilter != null) {
				boolean matches = modIdRegex
					? id.getNamespace().matches(modIdFilter)
					: id.getNamespace().equals(modIdFilter);
				if (!matches) {
					continue;
				}
			}

			File namespaceDir = new File(outputDir, id.getNamespace());
			String filename = ImageExportUtil.baseFilenameFromItem(lookup, stack);
			filename = ImageExportUtil.sanitizeFilename(filename);

			File iconFile = new File(namespaceDir, filename + ".png");
			if (iconFile.exists()) {
				continue;
			}

			if (!namespaceDir.exists()) {
				namespaceDir.mkdirs();
			}
			pendingItems.add(new ImageExportUtil.ItemExportData(stack, namespaceDir, filename));
		}

		this.totalItems = pendingItems.size();
	}

	public void exportCustomItems(List<ItemStack> customStacks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}
		startExport(mc.level.registryAccess(), customStacks);
		if (!hasWork() && mc.player != null) {
			mc.player.sendSystemMessage(Component.literal("Items already exported!"));
		}
	}

	public void startExport(HolderLookup.Provider lookup, List<ItemStack> customItems) {
		this.imageSize = ClientConfig.ICON_SIZE.get();
		this.pendingItems.clear();
		this.exporting = true;

		File outputDir = Common.CACHE.resolve("icons").resolve(String.valueOf(imageSize)).toFile();

		for (ItemStack stack : customItems) {
			Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
			File namespaceDir = new File(outputDir, id.getNamespace());
			String filename = ImageExportUtil.baseFilenameFromItem(lookup, stack);
			filename = ImageExportUtil.sanitizeFilename(filename);

			if (!namespaceDir.exists()) {
				namespaceDir.mkdirs();
			}
			pendingItems.add(new ImageExportUtil.ItemExportData(stack, namespaceDir, filename));
		}

		this.totalItems = pendingItems.size();
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
	}

	public void onPostRender() {
		if (!exporting) {
			return;
		}
		if (pendingItems.isEmpty()) {
			exporting = false;
			var mc = Minecraft.getInstance();
			if (mc.player != null) {
				mc.player.sendSystemMessage(Component.translatable("gui.foundryengine.icons.finished"));
			}
			return;
		}

		var mc = Minecraft.getInstance();
		var level = mc.level;
		if (level == null) {
			return;
		}

		int batchCount = Math.min(pendingItems.size(), ITEMS_PER_BATCH);
		List<ImageExportUtil.ItemExportData> batch = new ArrayList<>();
		for (int i = 0; i < batchCount; i++) {
			batch.add(pendingItems.poll());
		}

		int cols = (int) Math.ceil(Math.sqrt(batch.size()));
		int rows = (int) Math.ceil((double) batch.size() / cols);
		int texWidth = cols * imageSize;
		int texHeight = rows * imageSize;

		renderBatch(mc, level, batch, cols, rows, texWidth, texHeight);
	}

	private void renderBatch(Minecraft mc, Level level, List<ImageExportUtil.ItemExportData> batch, int columns, int rows, int texWidth, int texHeight) {
		var device = RenderSystem.getDevice();

		GpuTexture colorTex = device.createTexture(() -> "Icons color", 13, TextureFormat.RGBA8, texWidth, texHeight, 1, 1);
		GpuTextureView colorView = device.createTextureView(colorTex);
		GpuTexture depthTex = device.createTexture(() -> "Icons depth", 9, TextureFormat.DEPTH32, texWidth, texHeight, 1, 1);
		GpuTextureView depthView = device.createTextureView(depthTex);
		device.createCommandEncoder().clearColorAndDepthTextures(colorTex, 0, depthTex, 1.0);

		RenderSystem.outputColorTextureOverride = colorView;
		RenderSystem.outputDepthTextureOverride = depthView;

		Projection projection = new Projection();
		try (colorTex; colorView; depthTex; depthView; var projBuf = new ProjectionMatrixBuffer("icons")) {
			projection.setupOrtho(-1000.0F, 1000.0F, texWidth, texHeight, true);

			var gameRenderer = mc.gameRenderer;
			RenderSystem.backupProjectionMatrix();
			RenderSystem.setProjectionMatrix(projBuf.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

			var lighting = gameRenderer.getLighting();
			var submitNodeCollector = gameRenderer.getSubmitNodeStorage();
			var featureDispatcher = gameRenderer.getFeatureRenderDispatcher();
			var bufferSource = mc.renderBuffers().bufferSource();
			var resolver = mc.getItemModelResolver();
			var player = mc.player;

			PoseStack poseStack = new PoseStack();

			for (int i = 0; i < batch.size(); i++) {
				var data = batch.get(i);
				int col = i % columns;
				int row = i / columns;

				int left = col * imageSize;
				int top = row * imageSize;

				RenderSystem.enableScissorForRenderTypeDraws(left, texHeight - (top + imageSize), imageSize, imageSize);

				TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
				resolver.updateForTopItem(renderState, data.stack(), ItemDisplayContext.GUI, level, player, 0);

				Lighting.Entry lightingEntry = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
				lighting.setupFor(lightingEntry);

				poseStack.pushPose();
				poseStack.translate(left + (float) imageSize / 2.0F, top + (float) imageSize / 2.0F, 0.0F);
				poseStack.scale(imageSize, -imageSize, imageSize);
				renderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
				poseStack.popPose();

				RenderSystem.disableScissorForRenderTypeDraws();

				exportNbtIfNeeded(data, level.registryAccess());
			}

			featureDispatcher.renderAllFeatures();
			bufferSource.endBatch();

			RenderSystem.restoreProjectionMatrix();
			RenderSystem.outputColorTextureOverride = null;
			RenderSystem.outputDepthTextureOverride = null;

			int pixelSize = TextureFormat.RGBA8.pixelSize();
			GpuBuffer readBuffer = device.createBuffer(() -> "Icons read", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) texWidth * texHeight * pixelSize);
			CommandEncoder encoder = device.createCommandEncoder();
			device.createCommandEncoder().copyTextureToBuffer(colorTex, readBuffer, 0, () -> {
				try (var mapped = encoder.mapBuffer(readBuffer, true, false)) {
					NativeImage image = new NativeImage(texWidth, texHeight, false);
					for (int y = 0; y < texHeight; y++) {
						for (int x = 0; x < texWidth; x++) {
							int pixel = mapped.data().getInt((x + y * texWidth) * pixelSize);
							image.setPixelABGR(x, texHeight - y - 1, pixel);
						}
					}
					ImageExportUtil.processBatchAsync(image, batch, columns, imageSize, 0);
				} catch (Exception e) {
					ImageExportUtil.LOGGER.error("Failed to process icon batch", e);
				}
				readBuffer.close();
			}, 0);
		}
		int processed = totalItems - pendingItems.size();
		if (mc.player != null) {
			mc.player.sendSystemMessage(Component.translatable("gui.foundryengine.icons.status", processed, totalItems));
		}
	}

	private void exportNbtIfNeeded(ImageExportUtil.ItemExportData data, HolderLookup.Provider lookup) {
		if (!data.stack().getComponentsPatch().isEmpty() && CommonConfig.FILE_NAME_HASH_COMPONENTS.get()) {
			try {
				ImageExportUtil.exportComponentsFile(lookup, data.namespaceDir(), data.filename(), data.stack().getComponentsPatch());
			} catch (IOException e) {
				ImageExportUtil.LOGGER.error("Failed to write components file for: {}", data.filename(), e);
			}
		}
	}
}
