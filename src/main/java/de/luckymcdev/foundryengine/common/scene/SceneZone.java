package de.luckymcdev.foundryengine.common.scene;

public record SceneZone(String name, int minX, int minZ, int maxX, int maxZ) {
    public boolean contains(float x, float z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
