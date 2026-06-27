package de.luckymcdev.foundryengine.client.render.obj;

import de.luckymcdev.foundryengine.client.render.EngineRenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches a vanilla {@link RenderType} per OBJ material texture, so faces sharing a
 * material also share (and batch into) the same {@link net.minecraft.client.renderer.MultiBufferSource.BufferSource}
 * buffer at render time.
 * <p>
 * Uses {@link RenderTypes#entityCutout} as the underlying vanilla render type: it
 * supports a transparent texture (needed for {@code d}/{@code Tr} cutout materials),
 * standard lighting, and is the closest stock match for "a textured, lit triangle mesh".
 * Swap this out for a custom {@code RenderType.create(...)} (see {@code EngineRenderPipelines}
 * for the equivalent custom-pipeline pattern) if you need different shading later.
 */
public final class ObjRenderTypes {
    private static final Map<Identifier, RenderType> CACHE = new ConcurrentHashMap<>();

    /**
     * Fallback texture used when a material has no resolved diffuse texture.
     */
    private static final Identifier MISSING_TEXTURE = Identifier.withDefaultNamespace("missingno");

    private ObjRenderTypes() {
    }

    public static RenderType forMaterial(@Nullable Material material) {
        Identifier texture = material != null && material.hasTexture() ? material.getDiffuseTexturePath() : MISSING_TEXTURE;
        return forTexture(texture);
    }

    public static RenderType forTexture(Identifier texture) {
        return CACHE.computeIfAbsent(texture, tex -> {
            RenderSetup setup = RenderSetup.builder(EngineRenderPipelines.OBJ_ENTITY)
                    .withTexture("Sampler0", tex)
                    .useOverlay()
                    .useLightmap()
                    .createRenderSetup();
            return RenderType.create("foundryengine:obj/" + tex, setup);
        });
    }
}