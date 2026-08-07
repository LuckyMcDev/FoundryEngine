package de.luckymcdev.foundryengine.client.render;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.textures.GpuTexture;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

//? if 26.2 {
/*import com.mojang.blaze3d.GpuFormat;
*///?}

public final class EngineSceneDepth extends AbstractTexture {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(Common.MODID, "engine_scene_depth");

	private static EngineSceneDepth instance;
	private TextureTarget snapshot;
	private boolean registered;

	private EngineSceneDepth() {
	}

	public static boolean update() {
		Minecraft mc = Minecraft.getInstance();
		//? if 26.1 {
		RenderTarget main = mc.getMainRenderTarget();
		 //?} else {
		/*RenderTarget main = mc.gameRenderer.mainRenderTarget();
		*///?}
		if (main == null || !main.useDepth || main.getDepthTexture() == null) {
			return false;
		}
		if (instance == null) {
			instance = new EngineSceneDepth();
		}
		if (!instance.capture(main)) {
			return false;
		}
		if (!instance.registered) {
			mc.getTextureManager().register(ID, instance);
			instance.registered = true;
		}
		return true;
	}

	public static int snapshotDepthGlId() {
		if (instance == null || instance.snapshot == null) {
			return 0;
		}
		GpuTexture depth = instance.snapshot.getDepthTexture();
		return depth instanceof GlTexture gl ? gl.glId() : 0;
	}

	private boolean capture(RenderTarget src) {
		if (snapshot == null) {
			//? if 26.1 {
			snapshot = new TextureTarget("engine_scene_depth", src.width, src.height, true);
			 //?} else {
			/*snapshot = new TextureTarget("engine_scene_depth", src.width, src.height, true, GpuFormat.RGBA8_UNORM);
			*///?}
		} else if (snapshot.width != src.width || snapshot.height != src.height) {
			snapshot.resize(src.width, src.height);
		}
		if (snapshot.getDepthTexture() == null) {
			return false;
		}
		snapshot.copyDepthFrom(src);
		this.texture = snapshot.getDepthTexture();
		this.textureView = snapshot.getDepthTextureView();
		return true;
	}

	@Override
	public void close() {
	}
}
