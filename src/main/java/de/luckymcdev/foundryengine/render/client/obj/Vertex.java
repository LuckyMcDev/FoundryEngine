package de.luckymcdev.foundryengine.render.client.obj;

import org.joml.Vector2f;
import org.joml.Vector3f;

public record Vertex(Vector3f position, Vector3f normal, Vector2f uv) {
}