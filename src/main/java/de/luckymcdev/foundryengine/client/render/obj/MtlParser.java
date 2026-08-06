package de.luckymcdev.foundryengine.client.render.obj;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class MtlParser {
	private final Map<String, Material> materials = new LinkedHashMap<>();
	private Material current;
	private Identifier mtlLocation;

	public static Optional<Map<String, Material>> loadFromObj(Identifier objLocation, String mtlReference) {
		Identifier mtlLocation = resolveRelative(objLocation, mtlReference);
		Optional<Resource> resourceO = Minecraft.getInstance().getResourceManager().getResource(mtlLocation);
		if (resourceO.isEmpty()) {
			Common.LOGGER.warn("mtllib not found: {} (referenced by {})", mtlLocation, objLocation);
			return Optional.empty();
		}
		try {
			MtlParser parser = new MtlParser();
			parser.parseMtlFile(mtlLocation, resourceO.get());
			return Optional.of(parser.materials);
		} catch (IOException e) {
			Common.LOGGER.error("Error parsing mtllib: {}", mtlLocation, e);
			return Optional.empty();
		}
	}

	private static Vector3f parseVec3(String rest) {
		String[] parts = rest.split("\\s+");
		float x = parseFloatSafe(parts.length > 0 ? parts[0] : "1", 1.0f);
		float y = parseFloatSafe(parts.length > 1 ? parts[1] : String.valueOf(x), x);
		float z = parseFloatSafe(parts.length > 2 ? parts[2] : String.valueOf(x), x);
		return new Vector3f(x, y, z);
	}

	private static float parseFloatSafe(String s, float fallback) {
		try {
			return Float.parseFloat(s.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static String lastToken(String rest) {
		String[] parts = rest.trim().split("\\s+");
		return parts[parts.length - 1];
	}

	private static Identifier resolveRelative(Identifier base, String reference) {
		String cleaned = reference.replace('\\', '/').trim();
		if (cleaned.contains(":")) {
			String[] parts = cleaned.split(":", 2);
			return Identifier.fromNamespaceAndPath(parts[0], parts[1]);
		}

		String basePath = base.getPath();
		int lastSlash = basePath.lastIndexOf('/');
		String baseDir = lastSlash >= 0 ? basePath.substring(0, lastSlash + 1) : "";

		String combined = normalize(baseDir + cleaned);
		return Identifier.fromNamespaceAndPath(base.getNamespace(), combined);
	}

	private static String normalize(String path) {
		ArrayDeque<String> stack = new ArrayDeque<>();
		for (String segment : path.split("/")) {
			if (segment.isEmpty() || segment.equals(".")) {
				continue;
			}
			if (segment.equals("..")) {
				stack.pollLast();
			} else {
				stack.addLast(segment);
			}
		}
		return String.join("/", stack);
	}

	private static String fileNameWithoutExtension(String path) {
		int lastSlash = path.lastIndexOf('/');
		String file = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
		int dot = file.lastIndexOf('.');
		return dot >= 0 ? file.substring(0, dot) : file;
	}

	public void parseMtlFile(Identifier mtlLocation, Resource resource) throws IOException {
		this.mtlLocation = mtlLocation;
		InputStream inputStream = resource.open();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			String[] tokens = line.split("\\s+", 2);
			String key = tokens[0];
			String rest = tokens.length > 1 ? tokens[1].trim() : "";

			switch (key) {
				case "newmtl" -> startMaterial(rest);
				case "Ka" -> ifCurrent(m -> m.setAmbientColor(parseVec3(rest)));
				case "Kd" -> ifCurrent(m -> m.setDiffuseColor(parseVec3(rest)));
				case "Ks" -> ifCurrent(m -> m.setSpecularColor(parseVec3(rest)));
				case "Ns" -> ifCurrent(m -> m.setShininess(parseFloatSafe(rest, 0.0f)));
				case "d" -> ifCurrent(m -> m.setOpacity(parseFloatSafe(rest, 1.0f)));
				case "Tr" -> ifCurrent(m -> m.setOpacity(1.0f - parseFloatSafe(rest, 0.0f)));
				case "map_Kd" -> ifCurrent(m -> m.setDiffuseTexturePath(resolveRelative(mtlLocation, lastToken(rest))));
			}
		}
		reader.close();

		applyTextureFallback();
	}

	private void applyTextureFallback() {
		if (mtlLocation == null) {
			return;
		}
		String mtlBaseName = fileNameWithoutExtension(mtlLocation.getPath());
		Identifier fallbackTexture = Identifier.fromNamespaceAndPath(
			mtlLocation.getNamespace(),
			mtlLocation.getPath().substring(0, mtlLocation.getPath().lastIndexOf('/') + 1) + mtlBaseName + ".png"
		);
		Identifier normalizedFallback = Identifier.fromNamespaceAndPath(
			mtlLocation.getNamespace(),
			normalize(fallbackTexture.getPath())
		);

		for (Material mat : materials.values()) {
			if (!mat.hasTexture()) {
				mat.setDiffuseTexturePath(normalizedFallback);
				Common.LOGGER.debug("Applied texture fallback for material '{}': {}", mat.getName(), normalizedFallback);
			}
		}
	}

	private void startMaterial(String name) {
		current = new Material(name);
		materials.put(name, current);
	}

	private void ifCurrent(Consumer<Material> action) {
		if (current == null) {
			Common.LOGGER.warn("Material property found before any 'newmtl' — ignoring");
			return;
		}
		action.accept(current);
	}

	public Map<String, Material> getMaterials() {
		return materials;
	}
}
