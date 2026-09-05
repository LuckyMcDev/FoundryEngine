package de.luckymcdev.foundryengine.common.builder.menu;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MenuBuilder<M extends AbstractContainerMenu> extends AbstractBuilder<MenuType<M>> {

	private BiFunction<Integer, Inventory, M> menuSupplier;
	private MenuExtraFactory<M> extraFactory;
	private FeatureFlagSet featureFlags = FeatureFlags.DEFAULT_FLAGS;

	private MenuBuilder(Identifier id) {
		super(id);
	}

	public static <M extends AbstractContainerMenu> MenuBuilder<M> create(Identifier id) {
		return new MenuBuilder<>(id);
	}

	public static MenuBuilder<SimpleMenu> chestMenu(Identifier id, int rows) {
		return MenuBuilder.<SimpleMenu>create(id)
			.supplier((containerId, playerInventory) ->
				SimpleMenu.chest(containerId, playerInventory, rows)
			);
	}

	public MenuBuilder<M> supplier(BiFunction<Integer, Inventory, M> supplier) {
		this.menuSupplier = supplier;
		return this;
	}

	public MenuBuilder<M> extraFactory(MenuExtraFactory<M> factory) {
		this.extraFactory = factory;
		return this;
	}

	public MenuBuilder<M> featureFlags(FeatureFlagSet flags) {
		this.featureFlags = flags;
		return this;
	}

	public MenuProvider createProvider(Component displayName) {
		if (menuSupplier == null) {
			throw new IllegalStateException("No server factory set.");
		}
		return new SimpleMenuProvider(
			(containerId, inv, player) -> menuSupplier.apply(containerId, inv),
			displayName
		);
	}

	public void open(ServerPlayer player, Component displayName) {
		if (extraFactory != null) {
			throw new IllegalStateException(
				"This menu requires extra data. Use open(player, displayName, extraDataWriter) instead."
			);
		}
		player.openMenu(createProvider(displayName));
	}

	public void open(ServerPlayer player, Component displayName,
	                 Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
		if (extraFactory == null) {
			throw new IllegalStateException(
				"This menu does not support extra data. Use open(player, displayName) instead."
			);
		}
		player.openMenu(createProvider(displayName), extraDataWriter);
	}

	@SuppressWarnings("unchecked")
	public MenuType<M> register(RegisterEvent.RegisterHelper<MenuType<?>> helper) {
		MenuType<M> menuType = build();
		helper.register(id, menuType);
		setObject(menuType);
		return menuType;
	}

	public MenuType<M> build() {
		if (menuSupplier == null && extraFactory == null) {
			throw new IllegalStateException("Either a supplier or an extra factory must be provided.");
		}
		if (extraFactory != null) {
			return IMenuTypeExtension.create(
				(containerId, playerInventory, extraData) ->
					extraFactory.create(containerId, playerInventory, extraData)
			);
		} else {
			return new MenuType<>(menuSupplier::apply, featureFlags);
		}
	}

	@FunctionalInterface
	public interface MenuExtraFactory<M extends AbstractContainerMenu> {
		M create(int containerId, Inventory playerInventory, FriendlyByteBuf extraData);
	}
}