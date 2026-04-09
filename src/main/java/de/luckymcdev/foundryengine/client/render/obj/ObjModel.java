package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

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
        String modID = this.modelLocation.getNamespace();
        String fileName = this.modelLocation.getPath();
        Identifier resourceLocation = Identifier.fromNamespaceAndPath(modID, "obj/" + fileName + ".obj");
        Optional<Resource> resourceO = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
        if (resourceO.isEmpty()) {
            throw new EngineException("Resource not found: " + resourceLocation);
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
            Common.LOGGER.error("Error parsing OBJ file: {}", resourceLocation, e);
        }
    }

    public void renderModel(PoseStack poseStack, RenderType renderType, int packedLight) {
        faces.forEach(face -> face.renderFace(poseStack, renderType, packedLight));
    }

    public ObjObject getObject(String name) {
        return objects.get(name);
    }

    public Map<String, ObjObject> getObjects() {
        return objects;
    }
}