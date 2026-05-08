package de.luckymcdev.foundryengine.common.scene;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * A non-spatial grouping node used by the editor tree.
 */
public final class FolderSceneNode extends AbstractSceneNode {
    private final String typeName;

    public FolderSceneNode(String uuid, String displayName, String typeName) {
        super(uuid, displayName);
        this.typeName = typeName;
    }

    @Override
    public boolean editable() {
        return false;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public Vector3f getLocalPosition() {
        return new Vector3f();
    }

    @Override
    public Vector2f getLocalRotation() {
        return new Vector2f();
    }

    @Override
    public void remove() {
        // Folders are virtual; removing them makes no sense.
    }
}

