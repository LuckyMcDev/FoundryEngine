package io.github.luckymcdev.foundryengine.client.util;

import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class KeyBindingManager {
    private static final GenericRegistry<String, KeyBinding> KEYBINDINGS = new GenericRegistry<>();

    public void register(KeyBinding keyBinding) {
        KEYBINDINGS.register(keyBinding.getName(), keyBinding);
    }

    public void remove(KeyBinding keyBinding) {
        KEYBINDINGS.remove(keyBinding.getName());
    }

    @SubscribeEvent
    private static void handleClick(ClientTickEvent.Post event) {
        KEYBINDINGS.getValues().forEach(keyBinding -> {
            while(keyBinding.getKeyMapping().consumeClick()) {
                keyBinding.run();
            }
        });
    }
}
