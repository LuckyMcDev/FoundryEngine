package io.github.luckymcdev.foundryengine.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.luckymcdev.foundryengine.client.util.KeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public interface Client {

    KeyBinding EDITOR_KEY = new KeyBinding(
            new KeyMapping(
                    Component.translatable("key.foundryengine.editor").getString(),
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_F6,
                    KeyMapping.Category.DEBUG
            ),
            () -> {
            }
    );
}
