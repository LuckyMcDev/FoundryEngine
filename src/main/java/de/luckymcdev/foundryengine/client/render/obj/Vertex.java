package de.luckymcdev.foundryengine.client.render.obj;

import org.joml.Vector2f;
import org.joml.Vector3f;

public record Vertex(Vector3f position, Vector3f normal, Vector2f uv) {
}