package io.github.luckymcdev.foundryengine.client.imgui.icon;

public interface ImIcon {
    char getIconChar();

    default char toChar() {
        return getIconChar();
    }

    default String iconName() {
        return ((Enum<?>) this).name();
    }
}