package de.luckymcdev.foundryengine.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

//? if 26.1 {
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
//?} elif 26.2 {
/*import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
*///?}

public class MeshRenderer implements AutoCloseable {
	private static final int MAX_RENDER_TYPES = 256;
	private static final int MAX_PIPELINES = 256;
	private static final AtomicInteger RENDER_TYPE_INDEX = new AtomicInteger();
	private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
	private static final Map<Identifier, RenderPipeline> PIPELINES = new LinkedHashMap<>();
	private static final Map<String, RenderType> RENDER_TYPES = new LinkedHashMap<>();

	public static RenderType renderType(Identifier vertexShader, Identifier fragmentShader,
	                                    Identifier texture0, Identifier texture1) {
		String key = vertexShader + "|" + fragmentShader + "|" + texture0 + "|" + texture1;
		RenderType renderType = RENDER_TYPES.computeIfAbsent(key, k -> {
			RenderPipeline pipeline = PIPELINES.computeIfAbsent(fragmentShader,
				fsh -> buildPipeline(vertexShader, fsh));
			trimToSize(PIPELINES, MAX_PIPELINES);
			RenderSetup setup = RenderSetup.builder(pipeline)
				.withTexture("Sampler0", texture0)
				.withTexture("Sampler1", texture1)
				.withTexture("DepthSampler", EngineSceneDepth.ID)
				.createRenderSetup();
			return RenderType.create("engine_mesh/" + RENDER_TYPE_INDEX.getAndIncrement(), setup);
		});
		trimToSize(RENDER_TYPES, MAX_RENDER_TYPES);
		return renderType;
	}

	public static RenderType cutoutRenderType(Identifier vertexShader, Identifier fragmentShader,
	                                          Identifier texture0, Identifier texture1) {
		String key = "cutout|" + vertexShader + "|" + fragmentShader + "|" + texture0 + "|" + texture1;
		RenderType renderType = RENDER_TYPES.computeIfAbsent(key, k -> {
			Identifier location = Identifier.fromNamespaceAndPath(
				fragmentShader.getNamespace(), "cutout/" + fragmentShader.getPath());
			RenderPipeline pipeline = PIPELINES.computeIfAbsent(location,
				loc -> buildCutoutPipeline(loc, vertexShader, fragmentShader));
			trimToSize(PIPELINES, MAX_PIPELINES);
			RenderSetup setup = RenderSetup.builder(pipeline)
				.withTexture("Sampler0", texture0)
				.withTexture("Sampler1", texture1)
				.withTexture("DepthSampler", EngineSceneDepth.ID)
				.createRenderSetup();
			return RenderType.create("engine_mesh_cutout/" + RENDER_TYPE_INDEX.getAndIncrement(), setup);
		});
		trimToSize(RENDER_TYPES, MAX_RENDER_TYPES);
		return renderType;
	}

	private static <K, V> void trimToSize(Map<K, V> map, int maxSize) {
		if (map.size() <= maxSize) {
			return;
		}
		Iterator<K> it = map.keySet().iterator();
		while (map.size() > maxSize) {
			it.next();
			it.remove();
		}
	}

	private static RenderPipeline buildCutoutPipeline(Identifier location, Identifier vertexShader,
	                                                  Identifier fragmentShader) {
		//? if 26.1 {
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
		//?} elif 26.2 {
		/*return RenderPipeline.builder()
			.withLocation(location)
			.withVertexShader(vertexShader)
			.withFragmentShader(fragmentShader)
			.withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").withSampler("Sampler1").withSampler("DepthSampler").build())
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL).withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
			.withCull(false)
			.build();
		*///?}
	}

	private static RenderPipeline buildPipeline(Identifier vertexShader, Identifier fragmentShader) {
		//? if 26.1 {
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
		//?} elif 26.2 {
		/*return RenderPipeline.builder()
			.withLocation(fragmentShader)
			.withVertexShader(vertexShader)
			.withFragmentShader(fragmentShader)
			.withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").withSampler("Sampler1").withSampler("DepthSampler").build())
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL).withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
			.withCull(true)
			.build();
		*///?}
	}

	public void draw(RenderType renderType, Matrix4fc modelView, Consumer<BufferBuilder> buildAction) {
		//? if 26.1 {
		RenderPipeline pipeline = renderType.pipeline();
		BufferBuilder builder = new BufferBuilder(ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
		buildAction.accept(builder);

		MeshData mesh = builder.build();
		if (mesh == null) {
			return;
		}

		RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
		try {
			renderType.draw(mesh);
		} finally {
			RenderSystem.getModelViewStack().popMatrix();
		}
		//?} elif 26.2 {
		/*StagedVertexBuffer staged = new StagedVertexBuffer(() -> "engine_mesh", RenderType.SMALL_BUFFER_SIZE);
		try {
			StagedVertexBuffer.Draw draw = staged.appendDraw(renderType.format(), renderType.primitiveTopology());
			buildAction.accept((BufferBuilder) staged.getVertexBuilder(draw));
			staged.upload();
			RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
			try {
				PreparedRenderType prepared = renderType.prepare();
				StagedVertexBuffer.ExecuteInfo info = staged.getExecuteInfo(draw);
				if (info != null) {
					prepared.drawFromBuffer(info);
				}
			} finally {
				RenderSystem.getModelViewStack().popMatrix();
			}
			staged.endDraw();
		} finally {
			staged.close();
		}
		*///?}
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
		//? if 26.2 {
		/*private final StagedVertexBuffer staged;
		private final StagedVertexBuffer.Draw draw;
		*///?}
		private boolean finished = false;

		private DrawSession(RenderType renderType, Matrix4fc modelView) {
			this.renderType = renderType;
			this.modelView = modelView;
			//? if 26.1 {
			RenderPipeline pipeline = renderType.pipeline();
			this.builder = new BufferBuilder(ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
			//?} elif 26.2 {
			/*this.staged = new StagedVertexBuffer(() -> "engine_mesh", RenderType.SMALL_BUFFER_SIZE);
			this.draw = staged.appendDraw(renderType.format(), renderType.primitiveTopology());
			this.builder = (BufferBuilder) staged.getVertexBuilder(this.draw);
			*///?}
		}

		public BufferBuilder buffer() {
			if (finished) {
				throw new IllegalStateException("DrawSession already finished");
			}
			return builder;
		}

		public void finish() {
			if (finished) {
				return;
			}
			finished = true;

			//? if 26.1 {
			MeshData mesh = builder.build();
			if (mesh == null) {
				return;
			}

			RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
			try {
				renderType.draw(mesh);
			} finally {
				RenderSystem.getModelViewStack().popMatrix();
			}
			//?} elif 26.2 {
			/*staged.upload();
			RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
			try {
				StagedVertexBuffer.ExecuteInfo info = staged.getExecuteInfo(draw);
				if (info != null) {
					renderType.prepare().drawFromBuffer(info);
				}
			} finally {
				RenderSystem.getModelViewStack().popMatrix();
			}
			staged.endDraw();
			*///?}
		}

		@Override
		public void close() {
			finish();
		}
	}
}