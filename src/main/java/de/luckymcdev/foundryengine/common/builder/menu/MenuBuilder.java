package de.luckymcdev.foundryengine.common.builder.menu;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

public class MenuBuilder<M extends AbstractContainerMenu> extends AbstractBuilder<MenuType<M>> implements MenuProvider {

	private Supplier<M> supplier;
	private ContainerFactory<M> containerFactory;
	private FeatureFlagSet featureFlags = FeatureFlags.DEFAULT_FLAGS;
	private @Nullable ScreenFactory<M> screenFactory;
	private boolean screenDisabled = false;

	private MenuBuilder(Identifier id) {
		super(id);
	}

	public static <M extends AbstractContainerMenu> MenuBuilder<M> create(Identifier id) {
		return new MenuBuilder<>(id);
	}

	public MenuBuilder<M> supplier(Supplier<M> supplier) {
		this.supplier = supplier;
		return this;
	}

	public MenuBuilder<M> containerFactory(ContainerFactory<M> factory) {
		this.containerFactory = factory;
		return this;
	}

	public MenuBuilder<M> featureFlags(FeatureFlagSet flags) {
		this.featureFlags = flags;
		return this;
	}

	public MenuBuilder<M> screen(ScreenFactory<M> screenFactory) {
		this.screenFactory = screenFactory;
		this.screenDisabled = false;
		return this;
	}

	public MenuBuilder<M> disableMenuScreen() {
		this.screenDisabled = true;
		return this;
	}

	public boolean isScreenDisabled() {
		return screenDisabled;
	}

	public @Nullable ScreenFactory<M> getScreenFactory() {
		return screenFactory;
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		MenuType<M> type = get();
		if (supplier != null) {
			return supplier.create(type, containerId, playerInventory);
		}
		if (containerFactory != null) {
			return containerFactory.create(type, containerId, playerInventory, null);
		}
		throw new IllegalStateException("No supplier or container factory set.");
	}

	@Override
	public Component getDisplayName() {
		return Component.empty();
	}

	@Override
	public MenuType<M> build() {
		if (supplier == null && containerFactory == null) {
			throw new IllegalStateException("Either a supplier or a container factory must be provided.");
		}
		if (containerFactory != null) {
			ContainerFactory<M> factory = containerFactory;
			return IMenuTypeExtension.create(
				(containerId, playerInventory, extraData) ->
					factory.create(null, containerId, playerInventory, extraData)
			);
		}
		Supplier<M> s = supplier;
		return new MenuType<>((containerId, playerInventory) ->
			s.create(null, containerId, playerInventory), featureFlags);
	}

	@SuppressWarnings("unchecked")
	public MenuType<M> register(RegisterEvent.RegisterHelper<MenuType<?>> helper) {
		MenuType<M> menuType = build();
		helper.register(id, menuType);
		setObject(menuType);
		return menuType;
	}

	@FunctionalInterface
	public interface Supplier<M extends AbstractContainerMenu> {
		M create(MenuType<M> type, int containerId, Inventory playerInventory);
	}

	@FunctionalInterface
	public interface ContainerFactory<M extends AbstractContainerMenu> {
		M create(MenuType<M> type, int containerId, Inventory playerInventory, @Nullable RegistryFriendlyByteBuf extraData);
	}

	@FunctionalInterface
	public interface ScreenFactory<M extends AbstractContainerMenu> {
		Object create(M menu, Inventory inventory, Component title);
	}
}
