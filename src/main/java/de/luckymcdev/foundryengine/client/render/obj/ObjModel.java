package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.render.MeshRenderer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObjModel {
	private final List<Face> faces = new ArrayList<>();
	private final Identifier modelLocation;
	private final ObjParser objParser;
	private Map<String, ObjObject> objects = Map.of();
	private Map<String, Material> materials = Map.of();

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
			objParser.parseObjFile(modelLocation, resource);
			faces.clear();
			faces.addAll(objParser.getFaces());
			this.objects = objParser.getObjects();
			this.materials = objParser.getMaterials();
			Common.LOGGER.info("Loaded {} objects and {} materials from {}", objects.size(), materials.size(), modelLocation);
			objects.keySet().forEach(name ->
				Common.LOGGER.info("  Object: {} with {} faces", name, objects.get(name).getFaces().size())
			);
		} catch (IOException e) {
			Common.LOGGER.error("Error parsing OBJ file: {}", modelLocation, e);
		}
	}

	public void renderModel(Matrix4fc modelView, PoseStack poseStack, int packedLight) {
		var opaqueFaces = new ArrayList<Face>();
		var transparentFaces = new ArrayList<Face>();

		for (Face face : faces) {
			(face.material().isTransparent() ? transparentFaces : opaqueFaces).add(face);
		}

		float wx = modelView.m30();
		float wy = modelView.m31();
		float wz = modelView.m32();

		transparentFaces.sort((a, b) -> {
			Vector3f ca = a.getCentroid();
			Vector3f cb = b.getCentroid();
			float dax = wx + ca.x, day = wy + ca.y, daz = wz + ca.z;
			float dbx = wx + cb.x, dby = wy + cb.y, dbz = wz + cb.z;
			double da = dax * dax + day * day + daz * daz;
			double db = dbx * dbx + dby * dby + dbz * dbz;
			return Double.compare(db, da);
		});

		renderFacesByMaterial(opaqueFaces, poseStack, modelView, packedLight);
		renderFacesByMaterial(transparentFaces, poseStack, modelView, packedLight);
	}

	private void renderFacesByMaterial(List<Face> group, PoseStack poseStack, Matrix4fc modelView, int packedLight) {
		if (group.isEmpty()) {
			return;
		}

		Map<Material, List<Face>> byMaterial = new LinkedHashMap<>();
		for (Face face : group) {
			byMaterial.computeIfAbsent(face.material(), k -> new ArrayList<>()).add(face);
		}

		for (var entry : byMaterial.entrySet()) {
			Material mat = entry.getKey();
			List<Face> matFaces = entry.getValue();
			RenderType rt = ObjRenderTypes.forMaterial(mat);
			try (MeshRenderer.DrawSession session = Client.getMeshRenderer().begin(rt, modelView)) {
				for (Face face : matFaces) {
					face.buildVerticesTextured(session.buffer(), poseStack, packedLight);
				}
			}
		}
	}

	public void renderModel(PoseStack poseStack, int packedLight) {
		faces.forEach(face -> face.renderFace(poseStack, packedLight));
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

	public Map<String, Material> getMaterials() {
		return materials;
	}

	public List<Face> getFaces() {
		return faces;
	}
}
