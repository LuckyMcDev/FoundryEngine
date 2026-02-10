package io.github.luckymcdev.client;

import com.mojang.blaze3d.systems.RenderSystem;

public class TbRenderSystem extends RenderSystem {
    private static TbRenderer renderer;

    public static void init() {
        renderer = new TbRenderer();
    }

    public static TbRenderer renderer() {
        return renderer;
    }
}
