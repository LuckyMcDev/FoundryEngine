package de.luckymcdev.foundryengine.common.bundle.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BundleCreativeModeTab {
    private final DeferredRegister<CreativeModeTab> register;
    private final Supplier<CreativeModeTab> tab;

    public BundleCreativeModeTab(String bundleId, IEventBus modBus, BundleRegistryQuery registryQuery) {
        this.register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, bundleId);

        this.tab = register.register(bundleId + "_creative_tab", () ->
                CreativeModeTab.builder()
                        .icon(() -> new ItemStack(Items.PAPER))
                        .title(Component.translatable("itemGroup." + bundleId + "." + bundleId + "_creative_tab"))
                        .displayItems((params, output) -> {
                            registryQuery.getItems().forEach(output::accept);
                            registryQuery.getBlocks().forEach(output::accept);
                        })
                        .build()
        );

        this.register.register(modBus);
    }

    public Supplier<CreativeModeTab> getTab() {
        return tab;
    }
}