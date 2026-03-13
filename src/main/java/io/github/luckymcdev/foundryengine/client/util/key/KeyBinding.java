package io.github.luckymcdev.foundryengine.client.util.key;

import net.minecraft.client.KeyMapping;

/**
 * A Wrapper Around A Minecraft {@link KeyMapping}, which adds a {@link Runnable} which is run when the key is consumed.
 */
public class KeyBinding {
    private final KeyMapping keyMapping;
    private final Runnable onConsumeClick;

    public KeyBinding(KeyMapping keyMapping, Runnable onConsumeClick) {
        this.keyMapping = keyMapping;
        this.onConsumeClick = onConsumeClick;
    }

    public KeyMapping mapping() {
        return keyMapping;
    }

    public void run() {
        onConsumeClick.run();
    }

    public String getName() {
        return keyMapping.getName();
    }
}
