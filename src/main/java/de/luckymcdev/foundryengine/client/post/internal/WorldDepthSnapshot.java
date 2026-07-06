package de.luckymcdev.foundryengine.client.post.internal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.resources.Identifier;

final class WorldDepthSnapshot {

	static final Identifier TARGET_ID = Identifier.fromNamespaceAndPath("foundryengine", "world_depth_snapshot");
	private static final DepthSnapshot SNAPSHOT = new DepthSnapshot("engine_world_depth_snapshot");

	private WorldDepthSnapshot() {
	}

	static void capture(RenderTarget source) {
		SNAPSHOT.capture(source);
	}

	static boolean restoreInto(RenderTarget target) {
		return SNAPSHOT.restoreInto(target);
	}

	static RenderTarget getFramebuffer() {
		return SNAPSHOT.getFramebuffer();
	}
}
