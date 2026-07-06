package de.luckymcdev.foundryengine.common.bundle.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class BundleCreativeModeTab {
	private final @Nullable Supplier<CreativeModeTab> tab;

	public BundleCreativeModeTab(String bundleId, IEventBus modBus) {
		DeferredRegister<CreativeModeTab> register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, bundleId);

		this.tab = register.register(bundleId + "_creative_tab", () ->
			CreativeModeTab.builder()
				.icon(() -> BuiltInRegistries.ITEM.stream()
					.filter(item -> {
						Identifier id = BuiltInRegistries.ITEM.getKey(item);
						return id != null && id.getNamespace().equals(bundleId);
					})
					.findFirst()
					.map(ItemStack::new)
					.orElse(ItemStack.EMPTY))
				.title(Component.translatable("itemGroup." + bundleId + "." + bundleId + "_creative_tab"))
				.displayItems((params, output) -> {
					BuiltInRegistries.ITEM.stream()
						.filter(item -> {
							Identifier id = BuiltInRegistries.ITEM.getKey(item);
							return id != null && id.getNamespace().equals(bundleId);
						})
						.map(ItemStack::new)
						.forEach(output::accept);
					BuiltInRegistries.BLOCK.stream()
						.filter(block -> {
							Identifier id = BuiltInRegistries.BLOCK.getKey(block);
							return id != null && id.getNamespace().equals(bundleId);
						})
						.map(ItemStack::new)
						.forEach(output::accept);
				})
				.build()
		);

		register.register(modBus);
	}

	public @Nullable Supplier<CreativeModeTab> getTab() {
		return tab;
	}
}