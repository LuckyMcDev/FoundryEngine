package de.luckymcdev.foundryengine.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.*;
import java.util.function.Consumer;

/**
 * General-purpose low-level renderer.
 *
 * <h3>Texture support</h3>
 * Both {@link #draw} and {@link DrawSession} accept {@link TextureBinding} entries that
 * map directly to the sampler names declared in your pipeline snippet
 * (e.g. {@code "Sampler0"}, {@code "Sampler2"}).
 *
 * <pre>{@code
 * // One-shot draw with a texture
 * meshRenderer.draw(
 *     EngineRenderPipelines.POSITION_TEX_COLOR,
 *     mvp,
 *     List.of(TextureBinding.of("Sampler0", myTextureView, mySampler)),
 *     buffer -> { ... }
 * );
 *
 * // Session-based draw with a texture
 * try (var session = meshRenderer.begin(EngineRenderPipelines.POSITION_TEX_COLOR, mvp)
 *         .withTexture("Sampler0", myTextureView, mySampler)) {
 *     session.buffer().addVertex(...);
 * }
 * }</pre>
 */
public class MeshRenderer implements AutoCloseable {
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private MappableRingBuffer vertexBuffer;

    private static void submit(Minecraft client, RenderPipeline pipeline, MeshData mesh,
                               MeshData.DrawState drawState, GpuBuffer vertices,
                               VertexFormat format, Matrix4fc modelView,
                               List<TextureBinding> textures) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            mesh.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
            indexType = mesh.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer seq = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = seq.getBuffer(drawState.indexCount());
            indexType = seq.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(modelView, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(
                        () -> Common.MODID + " mesh render",
                        client.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        client.getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind every texture the caller provided
            for (TextureBinding tex : textures) {
                pass.bindTexture(tex.samplerName(), tex.view(), tex.sampler());
            }

            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(0, 0, drawState.indexCount(), 1);
        }

        mesh.close();
    }

    /**
     * One-shot draw <em>without</em> textures.
     */
    public void draw(RenderPipeline pipeline, Matrix4fc modelView, Consumer<BufferBuilder> buildAction) {
        draw(pipeline, modelView, Collections.emptyList(), buildAction);
    }

    /**
     * One-shot draw <em>with</em> textures.
     */
    public void draw(RenderPipeline pipeline, Matrix4fc modelView,
                     List<TextureBinding> textures, Consumer<BufferBuilder> buildAction) {
        BufferBuilder builder = new BufferBuilder(ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        buildAction.accept(builder);

        MeshData mesh = builder.build();
        if (mesh == null) return;

        MeshData.DrawState drawState = mesh.drawState();
        VertexFormat format = drawState.format();

        GpuBuffer gpuVertices = upload(mesh, drawState, format);
        submit(Minecraft.getInstance(), pipeline, mesh, drawState, gpuVertices, format, modelView, textures);

        vertexBuffer.rotate();
    }

    /**
     * Opens a {@link DrawSession} <em>without</em> textures.
     */
    public DrawSession begin(RenderPipeline pipeline, Matrix4fc modelView) {
        return new DrawSession(this, pipeline, modelView);
    }

    /**
     * Opens a {@link DrawSession} pre-loaded with the given textures.
     */
    public DrawSession begin(RenderPipeline pipeline, Matrix4fc modelView, List<TextureBinding> textures) {
        return new DrawSession(this, pipeline, modelView, textures);
    }

    @Override
    public void close() {
        ALLOCATOR.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }

    private GpuBuffer upload(MeshData mesh, MeshData.DrawState drawState, VertexFormat format) {
        int required = drawState.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < required) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(
                    () -> Common.MODID + " mesh renderer buffer",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    required
            );
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(
                vertexBuffer.currentBuffer().slice(0, mesh.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(mesh.vertexBuffer(), view.data());
        }
        return vertexBuffer.currentBuffer();
    }

    public record TextureBinding(String samplerName, @Nullable GpuTextureView view, @Nullable GpuSampler sampler) {
        public static TextureBinding of(String samplerName, GpuTextureView view, GpuSampler sampler) {
            return new TextureBinding(samplerName, view, sampler);
        }

        public static TextureBinding of(String samplerName, GpuTextureView view) {
            return new TextureBinding(samplerName, view, null);
        }
    }

    /**
     * A batched draw session.
     * Obtain one via {@link MeshRenderer#begin}.
     */
    public static final class DrawSession implements AutoCloseable {
        private final MeshRenderer owner;
        private final RenderPipeline pipeline;
        private final Matrix4fc modelView;
        private final BufferBuilder builder;
        private final List<TextureBinding> textures;
        private boolean finished = false;

        private DrawSession(MeshRenderer owner, RenderPipeline pipeline, Matrix4fc modelView) {
            this(owner, pipeline, modelView, Collections.emptyList());
        }

        private DrawSession(MeshRenderer owner, RenderPipeline pipeline, Matrix4fc modelView,
                            List<TextureBinding> initialTextures) {
            this.owner = owner;
            this.pipeline = pipeline;
            this.modelView = modelView;
            this.builder = new BufferBuilder(ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
            this.textures = new ArrayList<>(initialTextures);
        }

        /**
         * Binds a texture to the given sampler name (e.g. {@code "Sampler0"}).
         *
         * @return {@code this} for chaining
         */
        public DrawSession withTexture(String samplerName, GpuTextureView view, GpuSampler sampler) {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            textures.add(TextureBinding.of(samplerName, view, sampler));
            return this;
        }

        /**
         * Binds a texture to the given sampler name without an explicit sampler
         * (lets the driver/pipeline pick the default).
         *
         * @return {@code this} for chaining
         */
        public DrawSession withTexture(String samplerName, GpuTextureView view) {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            textures.add(TextureBinding.of(samplerName, view));
            return this;
        }

        /**
         * Adds multiple texture bindings at once.
         *
         * @return {@code this} for chaining
         */
        public DrawSession withTextures(List<TextureBinding> bindings) {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            textures.addAll(bindings);
            return this;
        }

        /**
         * Raw buffer — push vertices directly.
         */
        public BufferBuilder buffer() {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            return builder;
        }

        /**
         * Flush and submit to the GPU. Safe to call multiple times (no-op after first).
         */
        public void finish() {
            if (finished) return;
            finished = true;

            MeshData mesh = builder.build();
            if (mesh == null) return;

            MeshData.DrawState drawState = mesh.drawState();
            VertexFormat format = drawState.format();

            GpuBuffer gpuVertices = owner.upload(mesh, drawState, format);
            submit(Minecraft.getInstance(), pipeline, mesh, drawState, gpuVertices, format, modelView, textures);
            owner.vertexBuffer.rotate();
        }

        /**
         * AutoCloseable — calls {@link #finish()} so try-with-resources works.
         */
        @Override
        public void close() {
            finish();
        }
    }
}