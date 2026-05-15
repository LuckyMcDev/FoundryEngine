package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObjModel {
    public List<Face> faces = new ArrayList<>();
    public Map<String, ObjObject> objects;
    public Identifier modelLocation;
    protected ObjParser objParser;

    public ObjModel(Identifier modelLocation) {
        this.modelLocation = modelLocation;
        this.objParser = new ObjParser();
    }

    public void loadModel() throws EngineException {
        Common.LOGGER.info("Loading model: {}", modelLocation);
        Optional<Resource> resourceO = Minecraft.getInstance().getResourceManager().getResource(modelLocation);
        if (resourceO.isEmpty()) {
            throw new EngineException("Resource not found: " + modelLocation);
        }
        Resource resource = resourceO.get();
        try {
            this.objParser.parseObjFile(resource);
            this.faces = objParser.getFaces();
            this.objects = objParser.getObjects();
            Common.LOGGER.info("Loaded {} objects from {}", objects.size(), modelLocation);
            objects.keySet().forEach(name ->
                    Common.LOGGER.info("  Object: {} with {} faces", name, objects.get(name).getFaces().size())
            );
        } catch (IOException e) {
            Common.LOGGER.error("Error parsing OBJ file: {}", modelLocation, e);
        }
    }

    public void renderModel(PoseStack poseStack, RenderType renderType, int packedLight) {
        faces.forEach(face -> face.renderFace(poseStack, renderType, packedLight));
    }

    public void renderModel(Matrix4f viewMatrix, RenderPipeline pipeline, float r, float g, float b, float a) {
        Client.getMeshRenderer().draw(pipeline, viewMatrix, buffer -> {
            PoseStack poseStack = new PoseStack();
            for (Face face : faces) {
                face.buildVertices(buffer, poseStack, r, g, b, a);
            }
        });
    }

    public void renderModel(Matrix4f viewMatrix, RenderPipeline pipeline) {
        renderModel(viewMatrix, pipeline, 1f, 1f, 1f, 1f);
    }

    public void renderObjects(Matrix4f viewMatrix, RenderPipeline pipeline, float r, float g, float b, float a) {
        if (objects == null) return;
        objects.values().forEach(obj -> obj.render(pipeline, viewMatrix, r, g, b, a));
    }

    public void renderObjects(Matrix4f viewMatrix, RenderPipeline pipeline) {
        renderObjects(viewMatrix, pipeline, 1f, 1f, 1f, 1f);
    }

    public ObjObject getObject(String name) {
        return objects.get(name);
    }

    public Map<String, ObjObject> getObjects() {
        return objects;
    }
}