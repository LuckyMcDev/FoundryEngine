package de.luckymcdev.foundryengine.client.post.internal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import de.luckymcdev.foundryengine.client.Client;

final class DepthSnapshot {

	private final String name;
	private TextureTarget snapshot;
	private boolean pendingRestore;

	DepthSnapshot(String name) {
		this.name = name;
	}

	void capture(RenderTarget source) {
		if (!source.useDepth) {
			return;
		}
		if (source.getDepthTexture() == null) {
			return;
		}

		if (snapshot == null) {
			snapshot = Client.createSnapshotTarget(name, source.width, source.height);
		} else if (snapshot.width != source.width || snapshot.height != source.height) {
			snapshot.resize(source.width, source.height);
		}

		if (snapshot.getDepthTexture() == null) {
			return;
		}

		snapshot.copyDepthFrom(source);
		pendingRestore = true;
	}

	boolean restoreInto(RenderTarget target) {
		if (!pendingRestore) {
			return false;
		}
		pendingRestore = false;

		if (snapshot == null) {
			return false;
		}
		if (!target.useDepth) {
			return false;
		}
		if (snapshot.getDepthTexture() == null || target.getDepthTexture() == null) {
			return false;
		}
		if (snapshot.width != target.width || snapshot.height != target.height) {
			return false;
		}

		target.copyDepthFrom(snapshot);
		return true;
	}

	RenderTarget getFramebuffer() {
		return snapshot;
	}
}
