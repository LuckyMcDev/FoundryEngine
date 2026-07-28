package de.luckymcdev.foundryengine.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EngineBlockEntityRenderers {

	public static <T extends BlockEntity> BlockEntityRendererProvider<T, BlockEntityRenderState> noop() {
		return NoopRenderer::new;
	}

	private static class NoopRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, BlockEntityRenderState> {
		NoopRenderer(BlockEntityRendererProvider.Context ctx) {
		}

		@Override
		public BlockEntityRenderState createRenderState() {
			return new BlockEntityRenderState();
		}

		@Override
		public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		}
	}
}
