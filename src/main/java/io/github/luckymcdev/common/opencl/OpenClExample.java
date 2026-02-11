package io.github.luckymcdev.common.opencl;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.opencl.buffer.ClBuffer;
import io.github.luckymcdev.common.opencl.task.ClComputeTask;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryUtil.*;

public class OpenClExample {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Measures the performance difference between CPU and GPU implementations.
     */
    public static void comparePerformance(int width, int height, float scale, int octaves, float persistence, int iterations) {
        LOGGER.info("Starting Benchmark [{}x{}, {} octaves, {} iterations]", width, height, octaves, iterations);

        // GPU Benchmark
        long gpuStartTime = System.nanoTime();
        runBenchmarkGPU(width, height, scale, octaves, persistence, iterations);
        long gpuTime = System.nanoTime() - gpuStartTime;

        // CPU Benchmark
        long cpuStartTime = System.nanoTime();
        runBenchmarkCPU(width, height, scale, octaves, persistence, iterations);
        long cpuTime = System.nanoTime() - cpuStartTime;

        double gpuAvg = gpuTime / (double) iterations / 1_000_000.0;
        double cpuAvg = cpuTime / (double) iterations / 1_000_000.0;
        double speedup = (double) cpuTime / gpuTime;

        LOGGER.info("--- Benchmark Results ---");
        LOGGER.info("GPU Average: {} ms", String.format("%.3f", gpuAvg));
        LOGGER.info("CPU Average: {} ms", String.format("%.3f", cpuAvg));
        LOGGER.info("Performance: {}x faster on GPU", String.format("%.2f", speedup));
    }

    /**
     * Generates a Perlin noise map and saves it to a file.
     */
    public static void visualize(int width, int height, float scale, int octaves, boolean colorized) {
        String fileName = colorized ? "perlin_terrain.png" : "perlin_noise.png";
        LOGGER.info("Generating visualization: {}...", fileName);

        ClComputeTask task = ClComputeTask.create(Commons.id("kernels/kernel.cl"), "perlinNoise2D").build();
        FloatBuffer buffer = memAllocFloat(width * height);

        try {
            executeKernel(task, buffer, width, height, scale, octaves, 0.5f);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < width * height; i++) {
                float val = buffer.get(i);
                int x = i % width;
                int y = i / width;
                image.setRGB(x, y, colorized ? getTerrainColor(val) : getGrayscale(val));
            }

            File outputFile = new File(fileName);
            ImageIO.write(image, "png", outputFile);
            LOGGER.info("Saved to: {}", outputFile.getAbsolutePath());

        } catch (Exception e) {
            LOGGER.error("Failed to save visualization", e);
        } finally {
            memFree(buffer);
            task.cleanup();
        }
    }

    // --- Internal Logic ---

    private static void executeKernel(ClComputeTask task, FloatBuffer out, int w, int h, float s, int o, float p) {
        ClBuffer clBuf = task.addOutputBuffer(w * h);
        task.setArg(1, w)
                .setArg(2, h)
                .setArg(3, s)
                .setArg(4, o)
                .setArg(5, p);
        task.execute(w, h);
        clBuf.read(out);
    }

    private static void runBenchmarkGPU(int w, int h, float s, int o, float p, int iter) {
        ClComputeTask task = ClComputeTask.create(Commons.id("kernels/kernel.cl"), "perlinNoise2D").build();
        FloatBuffer noiseOutput = memAllocFloat(w * h);
        try {
            for (int i = 0; i < iter; i++) {
                executeKernel(task, noiseOutput, w, h, s, o, p);
            }
        } finally {
            memFree(noiseOutput);
            task.cleanup();
        }
    }

    private static void runBenchmarkCPU(int w, int h, float s, int o, float p, int iter) {
        float[] output = new float[w * h];
        for (int i = 0; i < iter; i++) {
            generateCPUNoise(output, w, h, s, o, p);
        }
    }

    private static void generateCPUNoise(float[] output, int width, int height, float scale, int octaves, float persistence) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float total = 0, freq = 1, amp = 1, maxV = 0;
                for (int i = 0; i < octaves; i++) {
                    float noise = (float) Math.sin((x / scale * freq) * 12.9898f + (y / scale * freq) * 78.233f) * 43758.5453f;
                    total += ( (noise - (float) Math.floor(noise)) * 2.0f - 1.0f ) * amp;
                    maxV += amp;
                    amp *= persistence;
                    freq *= 2.0f;
                }
                output[y * width + x] = total / maxV;
            }
        }
    }

    private static int getGrayscale(float value) {
        int gray = Math.max(0, Math.min(255, (int) ((value + 1.0f) * 127.5f)));
        return (gray << 16) | (gray << 8) | gray;
    }

    private static int getTerrainColor(float h) {
        h = (h + 1.0f) / 2.0f;
        if (h < 0.3f) return 0x001144;      // Deep
        if (h < 0.45f) return 0xDDCC88;     // Sand
        if (h < 0.7f) return 0x22AA22;      // Grass
        if (h < 0.85f) return 0x888888;     // Stone
        return 0xFFFFFF;                    // Snow
    }
}