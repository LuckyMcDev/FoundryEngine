package de.luckymcdev.foundryengine.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.*;
import java.util.function.Consumer;

public class MeshRenderer implements AutoCloseable {
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final Map<Identifier, RenderPipeline> PIPELINES = new HashMap<>();
    private static final Map<String, RenderType> RENDER_TYPES = new HashMap<>();
    private MappableRingBuffer vertexBuffer;


    public static RenderType renderType(Identifier vertexShader, Identifier fragmentShader,
                                        Identifier texture0, Identifier texture1) {
        String key = vertexShader + "|" + fragmentShader + "|" + texture0 + "|" + texture1;
        return RENDER_TYPES.computeIfAbsent(key, k -> {
            RenderPipeline pipeline = PIPELINES.computeIfAbsent(fragmentShader,
                    fsh -> buildPipeline(vertexShader, fsh));
            RenderSetup setup = RenderSetup.builder(pipeline)
                    .withTexture("Sampler0", texture0)
                    .withTexture("Sampler1", texture1)
                    .withTexture("DepthSampler", EngineSceneDepth.ID)
                    .createRenderSetup();
            return RenderType.create("engine_mesh/" + RENDER_TYPES.size(), setup);
        });
    }

    public static RenderType cutoutRenderType(Identifier vertexShader, Identifier fragmentShader,
                                              Identifier texture0, Identifier texture1) {
        String key = "cutout|" + vertexShader + "|" + fragmentShader + "|" + texture0 + "|" + texture1;
        return RENDER_TYPES.computeIfAbsent(key, k -> {
            Identifier location = Identifier.fromNamespaceAndPath(
                    fragmentShader.getNamespace(), "cutout/" + fragmentShader.getPath());
            RenderPipeline pipeline = PIPELINES.computeIfAbsent(location,
                    loc -> buildCutoutPipeline(loc, vertexShader, fragmentShader));
            RenderSetup setup = RenderSetup.builder(pipeline)
                    .withTexture("Sampler0", texture0)
                    .withTexture("Sampler1", texture1)
                    .withTexture("DepthSampler", EngineSceneDepth.ID)
                    .createRenderSetup();
            return RenderType.create("engine_mesh_cutout/" + RENDER_TYPES.size(), setup);
        });
    }

    private static RenderPipeline buildCutoutPipeline(Identifier location, Identifier vertexShader,
                                                      Identifier fragmentShader) {
        return RenderPipeline.builder()
                .withLocation(location)
                .withVertexShader(vertexShader)
                .withFragmentShader(fragmentShader)
                .withSampler("Sampler0")
                .withSampler("Sampler1")
                .withSampler("DepthSampler")
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, VertexFormat.Mode.QUADS)
                .withColorTargetState(ColorTargetState.DEFAULT)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                .withCull(false)
                .build();
    }

    private static RenderPipeline buildPipeline(Identifier vertexShader, Identifier fragmentShader) {
        return RenderPipeline.builder()
                .withLocation(fragmentShader)
                .withVertexShader(vertexShader)
                .withFragmentShader(fragmentShader)
                .withSampler("Sampler0")
                .withSampler("Sampler1")
                .withSampler("DepthSampler")
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(true)
                .build();
    }

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

            for (TextureBinding tex : textures) {
                pass.bindTexture(tex.samplerName(), tex.view(), tex.sampler());
            }

            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(0, 0, drawState.indexCount(), 1);
        }

        mesh.close();
    }

    public void draw(RenderPipeline pipeline, Matrix4fc modelView, Consumer<BufferBuilder> buildAction) {
        draw(pipeline, modelView, Collections.emptyList(), buildAction);
    }

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

    public DrawSession begin(RenderPipeline pipeline, Matrix4fc modelView) {
        return new DrawSession(this, pipeline, modelView);
    }

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

    // ──────────────────────────────────────────────
    //  Supporting types
    // ──────────────────────────────────────────────

    public record TextureBinding(String samplerName, @Nullable GpuTextureView view, @Nullable GpuSampler sampler) {
        public static TextureBinding of(String samplerName, GpuTextureView view, GpuSampler sampler) {
            return new TextureBinding(samplerName, view, sampler);
        }

        public static TextureBinding of(String samplerName, GpuTextureView view) {
            return new TextureBinding(samplerName, view, null);
        }
    }

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

        public DrawSession withTexture(String samplerName, GpuTextureView view, GpuSampler sampler) {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            textures.add(TextureBinding.of(samplerName, view, sampler));
            return this;
        }

        public DrawSession withTexture(String samplerName, GpuTextureView view) {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            textures.add(TextureBinding.of(samplerName, view));
            return this;
        }

        public DrawSession withTextures(List<TextureBinding> bindings) {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            textures.addAll(bindings);
            return this;
        }

        public BufferBuilder buffer() {
            if (finished) throw new IllegalStateException("DrawSession already finished");
            return builder;
        }

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

        @Override
        public void close() {
            finish();
        }
    }
}
