package de.luckymcdev.foundryengine.client.render.obj;

import de.luckymcdev.foundryengine.client.render.EngineRenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ObjRenderTypes {
    private static final Identifier MISSING_TEXTURE = Identifier.withDefaultNamespace("missingno");

    private static final Map<Identifier, RenderType> CUTOUT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, RenderType> TRANSLUCENT_CACHE = new ConcurrentHashMap<>();

    private ObjRenderTypes() {
    }

    public static RenderType forMaterial(@Nullable Material material) {
        if (material == null || !material.hasTexture()) {
            return forTexture(MISSING_TEXTURE, false);
        }
        return forTexture(material.getDiffuseTexturePath(), material.isTransparent());
    }

    public static RenderType forTexture(Identifier texture, boolean transparent) {
        var cache = transparent ? TRANSLUCENT_CACHE : CUTOUT_CACHE;
        return cache.computeIfAbsent(texture, tex -> {
            var pipeline = transparent ? EngineRenderPipelines.OBJ_ENTITY_TRANSLUCENT : EngineRenderPipelines.OBJ_ENTITY_CUTOUT;
            RenderSetup setup = RenderSetup.builder(pipeline)
                    .withTexture("Sampler0", tex)
                    .useOverlay()
                    .useLightmap()
                    .createRenderSetup();
            String suffix = transparent ? "translucent" : "cutout";
            return RenderType.create("foundryengine:obj/" + suffix + "/" + tex, setup);
        });
    }
}
