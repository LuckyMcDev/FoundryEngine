package de.luckymcdev.foundryengine.client.render.obj;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjParser {
    public List<Vector3f> vertices = new ArrayList<>();
    public List<Vector3f> normals = new ArrayList<>();
    public List<Vector2f> uvs = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();
    public Map<String, ObjObject> objects = new LinkedHashMap<>();
    public Map<String, Material> materials = new LinkedHashMap<>();

    protected ObjObject currentObject;
    protected String currentObjectName = "default";
    protected Material currentMaterial = Material.MISSING;
    private Identifier objLocation;

    public void parseObjFile(Resource resource) throws IOException {
        parseObjFile(null, resource);
    }

    /**
     * @param objLocation the resource location of the .obj file being parsed, used to
     *                    resolve {@code mtllib} references relative to it. May be {@code null}
     *                    if the file contains no {@code mtllib} directive.
     */
    public void parseObjFile(Identifier objLocation, Resource resource) throws IOException {
        this.objLocation = objLocation;
        currentObject = new ObjObject(currentObjectName);
        objects.put(currentObjectName, currentObject);

        InputStream inputStream = resource.open();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("o ")) {
                parseO(line);
            } else if (line.startsWith("v ")) {
                parseV(line);
            } else if (line.startsWith("vn ")) {
                parseVn(line);
            } else if (line.startsWith("vt ")) {
                parseVt(line);
            } else if (line.startsWith("f ")) {
                parseF(line);
            } else if (line.startsWith("mtllib ")) {
                parseMtllib(line);
            } else if (line.startsWith("usemtl ")) {
                parseUsemtl(line);
            }
        }
        reader.close();
    }

    private void parseO(String line) {
        String[] tokens = line.split("\\s+", 2);
        currentObjectName = tokens.length > 1 ? tokens[1] : "unnamed";
        currentObject = objects.get(currentObjectName);

        if (currentObject == null) {
            currentObject = new ObjObject(currentObjectName);
            objects.put(currentObjectName, currentObject);
        }
    }

    private void parseV(String line) {
        String[] tokens = line.split("\\s+");
        float x = Float.parseFloat(tokens[1]);
        float y = Float.parseFloat(tokens[2]);
        float z = Float.parseFloat(tokens[3]);
        vertices.add(new Vector3f(x, y, z));
    }

    private void parseVn(String line) {
        String[] tokens = line.split("\\s+");
        float x = Float.parseFloat(tokens[1]);
        float y = Float.parseFloat(tokens[2]);
        float z = Float.parseFloat(tokens[3]);
        normals.add(new Vector3f(x, y, z));
    }

    private void parseVt(String line) {
        String[] tokens = line.split("\\s+");
        float u = Float.parseFloat(tokens[1]);
        float v = Float.parseFloat(tokens[2]);
        uvs.add(new Vector2f(u, v));
    }

    private void parseF(String line) {
        Face face = getFace(line);
        faces.add(face);
        currentObject.addFace(face);
    }

    private void parseMtllib(String line) {
        if (objLocation == null) {
            de.luckymcdev.foundryengine.common.Common.LOGGER.warn(
                    "mtllib directive found but no objLocation was provided to parseObjFile — skipping: {}", line);
            return;
        }
        String[] tokens = line.split("\\s+", 2);
        if (tokens.length < 2) {
            return;
        }
        // mtllib may reference multiple files separated by whitespace.
        for (String ref : tokens[1].trim().split("\\s+")) {
            MtlParser.loadFromObj(objLocation, ref).ifPresent(materials::putAll);
        }
    }

    private void parseUsemtl(String line) {
        String[] tokens = line.split("\\s+", 2);
        String name = tokens.length > 1 ? tokens[1].trim() : "";
        currentMaterial = materials.getOrDefault(name, Material.MISSING);
        if (currentMaterial == Material.MISSING && !name.isEmpty()) {
            de.luckymcdev.foundryengine.common.Common.LOGGER.warn(
                    "usemtl referenced unknown material '{}' — using default", name);
        }
    }

    private @NotNull Face getFace(String line) {
        String[] tokens = line.trim().split("\\s+");
        List<Vertex> faceVertices = new ArrayList<>();

        for (int i = 1; i < tokens.length; i++) {
            String[] parts = tokens[i].split("/");

            int vertexIndex = Integer.parseInt(parts[0]) - 1;
            int textureIndex = (parts.length > 1 && !parts[1].isEmpty()) ? Integer.parseInt(parts[1]) - 1 : -1;
            int normalIndex = (parts.length > 2 && !parts[2].isEmpty()) ? Integer.parseInt(parts[2]) - 1 : -1;

            Vector3f position = safeGetVertex(vertexIndex);
            Vector3f normal = safeGetNormal(normalIndex);
            Vector2f uv = safeGetUV(textureIndex);

            faceVertices.add(new Vertex(position, normal, uv));
        }

        return new Face(faceVertices, currentMaterial);
    }

    protected Vector3f safeGetNormal(int index) {
        if (index >= 0 && index < normals.size()) {
            return normals.get(index);
        }
        return new Vector3f(0, 0, 0);
    }

    protected Vector2f safeGetUV(int index) {
        if (index >= 0 && index < uvs.size()) {
            return uvs.get(index);
        }
        return new Vector2f(0, 0);
    }

    protected Vector3f safeGetVertex(int index) {
        if (index >= 0 && index < vertices.size()) {
            return vertices.get(index);
        }
        throw new IllegalArgumentException("Invalid vertex index: " + index);
    }

    public List<Face> getFaces() {
        return faces;
    }

    public Map<String, ObjObject> getObjects() {
        return objects;
    }

    public ObjObject getObject(String name) {
        return objects.get(name);
    }

    public Map<String, Material> getMaterials() {
        return materials;
    }
}