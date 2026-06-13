package de.luckymcdev.foundryengine.client.editor.feature;

public interface EditorFeature {
    default void clientTick() {
    }

    default void render() {
    }

    default boolean onScroll(double vertical) {
        return false;
    }

    default void onDeactivated() {
    }
}
