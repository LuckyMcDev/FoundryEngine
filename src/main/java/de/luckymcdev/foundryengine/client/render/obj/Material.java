package de.luckymcdev.foundryengine.client.render.obj;

import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

/**
 * A single material parsed from a {@code .mtl} file.
 * <p>
 * Only {@link #diffuseTexture} and {@link #opacity} currently affect rendering
 * (see {@link Face#renderFace}); the color components are retained for future use
 * (e.g. tinting, custom shaders) since the current vertex format only carries a
 * flat per-vertex color and a single diffuse sampler.
 */
public class Material {
    public static final String DEFAULT_NAME = "__default__";

    /**
     * Fallback material used for faces with no {@code usemtl}, or whose material
     * could not be resolved (missing mtllib, unknown name, missing texture).
     */
    public static final Material MISSING = new Material(DEFAULT_NAME);

    private final String name;
    private Vector3f ambientColor = new Vector3f(1, 1, 1);
    private Vector3f diffuseColor = new Vector3f(1, 1, 1);
    private Vector3f specularColor = new Vector3f(0, 0, 0);
    private float shininess = 0f;
    private float opacity = 1f;

    /**
     * Resource location of the texture file referenced by {@code map_Kd}, if any.
     */
    private Identifier diffuseTexturePath;

    public Material(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(Vector3f ambientColor) {
        this.ambientColor = ambientColor;
    }

    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Vector3f diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public Vector3f getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Vector3f specularColor) {
        this.specularColor = specularColor;
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public Identifier getDiffuseTexturePath() {
        return diffuseTexturePath;
    }

    public void setDiffuseTexturePath(Identifier diffuseTexturePath) {
        this.diffuseTexturePath = diffuseTexturePath;
    }

    public boolean hasTexture() {
        return diffuseTexturePath != null;
    }
}