package io.github.luckymcdev.client.util;

import net.minecraft.client.KeyMapping;

public class KeyBinding {
    private final KeyMapping keyMapping;
    private final Runnable onConsumeClick;

    public KeyBinding(KeyMapping keyMapping, Runnable onConsumeClick) {
        this.keyMapping = keyMapping;
        this.onConsumeClick = onConsumeClick;
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public void run() {
        onConsumeClick.run();
    }

    public String getName() {
        return keyMapping.getName();
    }
}
