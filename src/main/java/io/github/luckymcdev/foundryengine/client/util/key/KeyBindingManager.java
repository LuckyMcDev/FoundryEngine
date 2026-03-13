package io.github.luckymcdev.foundryengine.client.util.key;

import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

/**
 * A Manager for registering and handling {@link KeyBinding}
 */
@EventBusSubscriber(Dist.CLIENT)
public class KeyBindingManager {
    private static final GenericRegistry<String, KeyBinding> KEYBINDINGS = new GenericRegistry<>();

    public KeyBindingManager() {
    }

    @SubscribeEvent
    private static void handleClick(ClientTickEvent.Post event) {
        KEYBINDINGS.forEach(keyBinding -> {
            while (keyBinding.mapping().consumeClick()) {
                keyBinding.run();
            }
        });
    }

    /**
     * Register a KeyBinding.
     *
     * @param keyBinding registrar
     */
    public void register(KeyBinding keyBinding) {
        KEYBINDINGS.register(keyBinding.getName(), keyBinding);
    }

    /**
     * remove a KeyBinding
     *
     * @param keyBinding removed
     */
    public void remove(KeyBinding keyBinding) {
        KEYBINDINGS.remove(keyBinding.getName());
    }

    /**
     * Returns a {@link List} of all registered {@link KeyBinding}.
     *
     * @return {@link List} of all registered {@link KeyBinding}
     */
    public List<KeyBinding> getKeyBindings() {
        return KEYBINDINGS.stream().toList();
    }
}
