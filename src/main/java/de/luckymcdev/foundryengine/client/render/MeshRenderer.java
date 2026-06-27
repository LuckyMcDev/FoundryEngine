package de.luckymcdev.foundryengine.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MeshRenderer implements AutoCloseable {
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Map<Identifier, RenderPipeline> PIPELINES = new HashMap<>();
    private static final Map<String, RenderType> RENDER_TYPES = new HashMap<>();

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

    public void draw(RenderType renderType, Matrix4fc modelView, Consumer<BufferBuilder> buildAction) {
        RenderPipeline pipeline = renderType.pipeline();
        BufferBuilder builder = new BufferBuilder(ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        buildAction.accept(builder);

        MeshData mesh = builder.build();
        if (mesh == null) return;

        RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
        try {
            renderType.draw(mesh);
        } finally {
            RenderSystem.getModelViewStack().popMatrix();
        }
    }

    public DrawSession begin(RenderType renderType, Matrix4fc modelView) {
        return new DrawSession(renderType, modelView);
    }

    @Override
    public void close() {
        ALLOCATOR.close();
    }

    public static final class DrawSession implements AutoCloseable {
        private final RenderType renderType;
        private final Matrix4fc modelView;
        private final BufferBuilder builder;
        private boolean finished = false;

        private DrawSession(RenderType renderType, Matrix4fc modelView) {
            this.renderType = renderType;
            this.modelView = modelView;
            RenderPipeline pipeline = renderType.pipeline();
            this.builder = new BufferBuilder(ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
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

            RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
            try {
                renderType.draw(mesh);
            } finally {
                RenderSystem.getModelViewStack().popMatrix();
            }
        }

        @Override
        public void close() {
            finish();
        }
    }
}