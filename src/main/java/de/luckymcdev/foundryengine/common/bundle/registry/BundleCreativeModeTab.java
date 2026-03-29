package de.luckymcdev.foundryengine.common.bundle.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class BundleCreativeModeTab {
    private @Nullable Supplier<CreativeModeTab> tab;

    public BundleCreativeModeTab(String bundleId, IEventBus modBus, BundleRegistryQuery registryQuery) {
        DeferredRegister<CreativeModeTab> register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, bundleId);

        if (registryQuery.getItems().isEmpty() || registryQuery.getBlocks().isEmpty()) return;

        this.tab = register.register(bundleId + "_creative_tab", () ->
                CreativeModeTab.builder()
                        .icon(() -> new ItemStack(registryQuery.getItems().getFirst()))
                        .title(Component.translatable("itemGroup." + bundleId + "." + bundleId + "_creative_tab"))
                        .displayItems((params, output) -> {
                            registryQuery.getItems().forEach(output::accept);
                            registryQuery.getBlocks().forEach(output::accept);
                        })
                        .build()
        );

        register.register(modBus);
    }

    public @Nullable Supplier<CreativeModeTab> getTab() {
        return tab;
    }
}