package de.luckymcdev.foundryengine.client.post.internal;

import com.mojang.blaze3d.pipeline.RenderTarget;

final class PostRenderDepthSnapshot {

	private static final DepthSnapshot SNAPSHOT = new DepthSnapshot("engine_post_render_depth_snapshot");

	private PostRenderDepthSnapshot() {
	}

	static void capture(RenderTarget source) {
		SNAPSHOT.capture(source);
	}

	static boolean restoreInto(RenderTarget target) {
		return SNAPSHOT.restoreInto(target);
	}
}
