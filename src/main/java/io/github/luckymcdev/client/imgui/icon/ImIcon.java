package io.github.luckymcdev.client.imgui.icon;

public interface ImIcon {
    char getIconChar();

    default char toChar() {
        return getIconChar();
    }

    default String iconName() {
        return ((Enum<?>) this).name();
    }
}