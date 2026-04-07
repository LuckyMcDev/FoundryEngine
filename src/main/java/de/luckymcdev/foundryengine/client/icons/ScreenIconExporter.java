package de.luckymcdev.foundryengine.client.icons;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.config.CommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenIconExporter extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int BACKGROUND_COLOR = new Color(230, 230, 230, 255).argb();
    private final HolderLookup.Provider lookupProvider;
    private final int imageSize;
    private final double guiScale;
    private final @Nullable String modIdFilter;
    private final boolean modIdRegex;
    private final List<ImageExportUtil.ItemExportData> pendingItems;
    private final int totalItems;
    private final ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 5));
    private final AtomicInteger activeIOJobs = new AtomicInteger(0);
    private int processedItems = 0;

    public ScreenIconExporter(HolderLookup.Provider lookupProvider, double guiScale,
                              @Nullable String modIdFilter, boolean modIdRegex) {
        super(Component.literal("export_screen"));
        this.lookupProvider = lookupProvider;
        this.imageSize = ClientConfig.ICON_SIZE.get();
        this.guiScale = guiScale;
        this.modIdFilter = modIdFilter;
        this.modIdRegex = modIdRegex;
        this.pendingItems = buildTasks();
        this.totalItems = pendingItems.size();
    }

    public ScreenIconExporter(HolderLookup.Provider lookupProvider, double guiScale,
                              @Nullable List<ItemStack> manualQueue) {
        super(Component.literal("export_screen"));
        this.lookupProvider = lookupProvider;
        this.imageSize = ClientConfig.ICON_SIZE.get();
        this.guiScale = guiScale;
        this.modIdFilter = null;
        this.modIdRegex = false;
        this.pendingItems = (manualQueue != null) ? buildManualTasks(manualQueue) : buildTasks();
        this.totalItems = pendingItems.size();
    }

    private static void renderItem(GuiGraphicsExtractor gui, ItemStack stack, float x, float y, float logicalSize) {
        gui.pose().pushMatrix();
        gui.pose().translate(x, y);
        gui.pose().scale(logicalSize / 16f, logicalSize / 16f);
        gui.item(stack, 0, 0);
        gui.pose().popMatrix();
    }

    private static void flushRender() {
        Minecraft.getInstance().gameRenderer.guiRenderer.render(
                Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE)
        );
    }

    private static void reportProgress(int done, int total) {
        Minecraft.getInstance().player.sendOverlayMessage(
                Component.translatable("gui.foundryengine.icons.status", done, total));
    }

    public static void exportCustomItems(List<ItemStack> customStacks) {
        Minecraft mc = Minecraft.getInstance();

        ScreenIconExporter exporter = new ScreenIconExporter(
                mc.level.registryAccess(),
                mc.getWindow().getGuiScale(),
                customStacks
        );

        if (exporter.hasWork()) {
            mc.setScreen(exporter);
        } else {
            mc.player.sendOverlayMessage(Component.literal("Items already exported!"));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (pendingItems.isEmpty()) {
            if (activeIOJobs.get() == 0) {
                executor.shutdown();
                Minecraft.getInstance().setScreen(null);
                Minecraft.getInstance().player.sendOverlayMessage(Component.translatable("gui.foundryengine.icons.finished"));
            }
            return;
        }

        double logicalIconSize = imageSize / guiScale;
        int columns = (int) (guiGraphics.guiWidth() / logicalIconSize);
        int rows = (int) (guiGraphics.guiHeight() / logicalIconSize);
        int batchSize = columns * rows;

        if (batchSize <= 0) return;

        List<ImageExportUtil.ItemExportData> currentBatch = new ArrayList<>();
        for (int i = 0; i < batchSize && !pendingItems.isEmpty(); i++) {
            currentBatch.add(pendingItems.removeFirst());
        }

        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), BACKGROUND_COLOR);

        for (int i = 0; i < currentBatch.size(); i++) {
            ImageExportUtil.ItemExportData data = currentBatch.get(i);
            float x = (float) ((i % columns) * logicalIconSize);
            float y = (float) ((i / columns) * logicalIconSize);

            renderItem(guiGraphics, data.stack(), x, y, (float) logicalIconSize);
            exportNbtIfNeeded(data);
        }

        flushRender();
        processedItems += currentBatch.size();
        reportProgress(processedItems, totalItems);

        activeIOJobs.incrementAndGet();
        Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget(), (nativeImage) -> {
            File debugFolder = Common.CACHE.resolve("icons").resolve("screens").toFile();
            if (!debugFolder.exists()) debugFolder.mkdir();

            executor.submit(() -> {
                try {
                    File fullImg = new File(debugFolder, "screen_" + System.currentTimeMillis() + ".png");
                    nativeImage.writeToFile(fullImg);

                    ImageExportUtil.processBatchAsync(nativeImage, currentBatch, columns, imageSize, BACKGROUND_COLOR);
                } catch (IOException e) {
                    LOGGER.error("Failed to save debug screenshot", e);
                    nativeImage.close();
                } finally {
                    activeIOJobs.decrementAndGet();
                }
            });
        });
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor guiGraphics) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

    }

    private boolean shouldExport(Identifier location) {
        if (modIdFilter == null) return true;
        return modIdRegex
                ? location.getNamespace().matches(modIdFilter)
                : location.getNamespace().equals(modIdFilter);
    }

    private List<ImageExportUtil.ItemExportData> buildTasks() {
        File outputDir = Common.CACHE.resolve("icons").resolve(String.valueOf(imageSize)).toFile();
        List<ImageExportUtil.ItemExportData> list = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM.stream().toList()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            ItemStack stack = new ItemStack(item);

            if (!shouldExport(id)) continue;

            File namespaceDir = new File(outputDir, id.getNamespace());

            String filename = ImageExportUtil.baseFilenameFromItem(lookupProvider, stack);
            filename = ImageExportUtil.sanitizeFilename(filename);

            File iconFile = new File(namespaceDir, filename + ".png");
            if (iconFile.exists()) {
                continue;
            }

            if (!namespaceDir.exists()) namespaceDir.mkdirs();

            list.add(new ImageExportUtil.ItemExportData(stack, namespaceDir, filename));
        }

        LOGGER.debug("Export Cache: Found {} missing icons to generate.", list.size());
        return list;
    }

    private List<ImageExportUtil.ItemExportData> buildManualTasks(List<ItemStack> stacks) {
        File outputDir = Common.CACHE.resolve("icons").resolve(String.valueOf(imageSize)).toFile();
        List<ImageExportUtil.ItemExportData> list = new ArrayList<>();

        for (ItemStack stack : stacks) {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            File namespaceDir = new File(outputDir, id.getNamespace());

            String filename = ImageExportUtil.baseFilenameFromItem(lookupProvider, stack);
            filename = ImageExportUtil.sanitizeFilename(filename);

            if (!namespaceDir.exists()) namespaceDir.mkdirs();

            list.add(new ImageExportUtil.ItemExportData(stack, namespaceDir, filename));
        }
        return list;
    }

    private void exportNbtIfNeeded(ImageExportUtil.ItemExportData data) {
        if (!data.stack().getComponentsPatch().isEmpty() && CommonConfig.FILE_NAME_HASH_COMPONENTS.get()) {
            try {
                ImageExportUtil.exportComponentsFile(lookupProvider, data.namespaceDir(), data.filename(), data.stack().getComponentsPatch());
            } catch (IOException e) {
                LOGGER.error("Failed to write components file for: {}", data.filename(), e);
            }
        }
    }

    public boolean hasWork() {
        return !this.pendingItems.isEmpty();
    }
}