package de.luckymcdev.foundryengine.client.render;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.mixin.render.RenderPipelinesInvoker;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static com.mojang.blaze3d.pipeline.BlendFunction.TRANSLUCENT;

//? if 26.1 {
import com.mojang.blaze3d.vertex.VertexFormat;
 
//?}
//? if 26.2 {
/*import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
*///?}

public class EngineRenderPipelines {
	public static final RenderPipeline POSITION = reg("position", Snippets.POSITION_SN);
	public static final RenderPipeline POSITION_COLOR = reg("position_color", Snippets.POSITION_COLOR_SN);
	public static final RenderPipeline POSITION_COLOR_NORMAL = reg("lit_models", Snippets.POSITION_COLOR_NORMAL_SN); // Use this for Suzanne!
	public static final RenderPipeline POSITION_TEX_COLOR = reg("textured", Snippets.POSITION_TEX_COLOR_SN);
	public static final RenderPipeline DEBUG_LINES = reg("debug_lines", Snippets.LINE_SN);
	public static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelinesInvoker.register(
		RenderPipeline.builder(Snippets.POSITION_COLOR_SN)
			.withLocation(Identifier.fromNamespaceAndPath(Common.MODID, "pipeline/debug_filled_box_through_walls"))
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.build()
	);
	public static final RenderPipeline OBJ_ENTITY_CUTOUT = RenderPipelinesInvoker.register(
		RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
			.withLocation(Common.id("pipeline/obj_entity_cutout"))
			//? if 26.1 {
			.withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
			 //?} else {
			/*.withVertexBinding(0, DefaultVertexFormat.ENTITY).withPrimitiveTopology(PrimitiveTopology.QUADS)
			*///?}
			.withVertexShader(Identifier.withDefaultNamespace("core/entity"))
			.withFragmentShader(Identifier.withDefaultNamespace("core/entity"))
			//? if 26.1 {
			.withSampler("Sampler0")
			.withSampler("Sampler1")
			.withSampler("Sampler2")
			//?} else {
			/*.withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").withSampler("Sampler1").withSampler("Sampler2").build())
			*///?}
			.withShaderDefine("ALPHA_CUTOUT", 0.1F)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.withCull(false)
			.build()
	);
	public static final RenderPipeline OBJ_ENTITY_TRANSLUCENT = RenderPipelinesInvoker.register(
		RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
			.withLocation(Common.id("pipeline/obj_entity_translucent"))
			//? if 26.1 {
			.withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
			 //?} else {
			/*.withVertexBinding(0, DefaultVertexFormat.ENTITY).withPrimitiveTopology(PrimitiveTopology.QUADS)
			*///?}
			.withVertexShader(Identifier.withDefaultNamespace("core/entity"))
			.withFragmentShader(Identifier.withDefaultNamespace("core/entity"))
			//? if 26.1 {
			.withSampler("Sampler0")
			.withSampler("Sampler1")
			.withSampler("Sampler2")
			//?} else {
			/*.withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").withSampler("Sampler1").withSampler("Sampler2").build())
			*///?}
			.withShaderDefine("ALPHA_CUTOUT", 0.1F)
			.withColorTargetState(new ColorTargetState(TRANSLUCENT))
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
			.withCull(false)
			.build()
	);

	private static RenderPipeline reg(String path, RenderPipeline.Snippet snippet) {
		return RenderPipelinesInvoker.register(
			RenderPipeline.builder(snippet)
				.withLocation(Common.id("pipeline/" + path))
				.build()
		);
	}

	public static class Snippets {
		public static final RenderPipeline.Snippet POSITION_SN = base()
			//? if 26.1 {
			.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
			 //?} else {
			/*.withVertexBinding(0, DefaultVertexFormat.POSITION).withPrimitiveTopology(PrimitiveTopology.QUADS)
			*///?}
			.withVertexShader(Common.id("core/position"))
			.withFragmentShader(Common.id("core/position"))
			.buildSnippet();
		public static final RenderPipeline.Snippet POSITION_COLOR_SN = base()
			//? if 26.1 {
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
			 //?} else {
			/*.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS)
			*///?}
			.withVertexShader(Common.id("core/position_color"))
			.withFragmentShader(Common.id("core/position_color"))
			.buildSnippet();
		public static final RenderPipeline.Snippet POSITION_COLOR_NORMAL_SN = base()
			//? if 26.1 {
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS)
			 //?} else {
			/*.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL).withPrimitiveTopology(PrimitiveTopology.QUADS)
			*///?}
			.withVertexShader(Common.id("core/position_color_lit"))
			.withFragmentShader(Common.id("core/position_color_lit"))
			.buildSnippet();
		public static final RenderPipeline.Snippet POSITION_TEX_COLOR_SN = base()
			//? if 26.1 {
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
			 //?} else {
			/*.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS)
			*///?}
			//? if 26.1 {
			.withSampler("Sampler0")
			 //?} else {
			/*.withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").build())
			*///?}
			.withVertexShader(Common.id("core/position_tex_color"))
			.withFragmentShader(Common.id("core/position_tex_color"))
			.buildSnippet();
		public static final RenderPipeline.Snippet LINE_SN = base()
			//? if 26.1 {
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
			 //?} else {
			/*.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH).withPrimitiveTopology(PrimitiveTopology.LINES)
			*///?}
			.withCull(false)
			.withVertexShader(Common.id("core/lines"))
			.withFragmentShader(Common.id("core/lines"))
			.buildSnippet();

		private static RenderPipeline.Builder base() {
			return RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
				.withColorTargetState(new ColorTargetState(TRANSLUCENT))
				.withDepthStencilState(DepthStencilState.DEFAULT);
		}
	}
}