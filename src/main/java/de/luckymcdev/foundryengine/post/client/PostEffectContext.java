package de.luckymcdev.foundryengine.post.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;

public record PostEffectContext(Minecraft client, float deltaTick, int screenWidth, int screenHeight,
                                PostChain processor) {

}
