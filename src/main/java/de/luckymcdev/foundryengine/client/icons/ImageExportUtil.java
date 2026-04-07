package de.luckymcdev.foundryengine.client.icons;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageExportUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static String baseFilenameFromItem(HolderLookup.Provider lookupProvider, ItemStack itemStack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        String path = id.getPath();

        if (itemStack.getComponentsPatch().isEmpty()) {
            return path;
        }

        String componentsString = componentsToString(lookupProvider, itemStack.getComponentsPatch());
        String hash = DigestUtils.md5Hex(componentsString).substring(0, 8);

        return path + "_" + hash;
    }

    public static String componentsToString(HolderLookup.Provider lookupProvider, DataComponentPatch components) {
        return DataComponentPatch.CODEC
                .encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), components)
                .getOrThrow()
                .toString();
    }

    public static void processBatchAsync(NativeImage fullImage, List<ItemExportData> batch, int columns, int imageSize, int backgroundColor) {
        try {
            for (int i = 0; i < batch.size(); i++) {
                ItemExportData data = batch.get(i);

                int col = i % columns;
                int row = i / columns;

                int physicalX = col * imageSize;
                int physicalY = row * imageSize;

                try (NativeImage cropped = cropSubImage(fullImage, physicalX, physicalY, imageSize, imageSize)) {
                    for (int cx = 0; cx < imageSize; cx++) {
                        for (int cy = 0; cy < imageSize; cy++) {
                            if (cropped.getPixel(cx, cy) == backgroundColor) {
                                cropped.setPixel(cx, cy, 0x00FFFFFF);
                            }
                        }
                    }

                    File file = new File(data.namespaceDir(), data.filename() + ".png").getCanonicalFile();
                    cropped.writeToFile(file);
                } catch (IOException e) {
                    LOGGER.error("Failed to write PNG for: {}", data.filename(), e);
                }
            }
        } finally {
            fullImage.close();
        }
    }

    public static void exportComponentsFile(HolderLookup.Provider lookupProvider, File dir, String baseFilename, DataComponentPatch components) throws IOException {
        File file = new File(dir, baseFilename + ".txt").getCanonicalFile();
        FileUtils.writeStringToFile(file, componentsToString(lookupProvider, components), Charsets.UTF_8);
    }

    private static NativeImage cropSubImage(NativeImage source, int srcX, int srcY, int width, int height) {
        NativeImage result = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            long srcOffset = ((long) (srcY + y) * source.getWidth() + srcX) * source.format().components();
            long dstOffset = (long) y * width * result.format().components();
            MemoryUtil.memCopy(source.pixels + srcOffset, result.pixels + dstOffset, (long) width * source.format().components());
        }
        return result;
    }

    public static String sanitizeFilename(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    public record ItemExportData(ItemStack stack, File namespaceDir, String filename) {
    }
}