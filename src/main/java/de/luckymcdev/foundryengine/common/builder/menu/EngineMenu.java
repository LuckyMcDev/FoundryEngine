package de.luckymcdev.foundryengine.common.builder.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A flexible, easily customizable {@link AbstractContainerMenu}.
 * <p>Configure with {@link #builder()}. Default layout matches a vanilla chest
 * (container slots at {@code (8, 18)} with 18px spacing; player inventory below).</p>
 */
public class EngineMenu extends AbstractContainerMenu {
	protected final Container container;
	protected final int rows;
	protected final int columns;
	protected final int containerSlotCount;

	protected EngineMenu(@Nullable MenuType<?> menuType, int containerId,
	                     Inventory playerInventory, Builder builder) {
		super(menuType, containerId);
		this.rows = builder.rows;
		this.columns = builder.columns;
		this.containerSlotCount = this.rows * this.columns;
		this.container = builder.container != null
			? builder.container
			: new SimpleContainer(this.containerSlotCount);

		for (int row = 0; row < this.rows; row++) {
			for (int col = 0; col < this.columns; col++) {
				int index = col + row * this.columns;
				int x = builder.slotOriginX + col * 18;
				int y = builder.slotOriginY + row * 18;
				addSlot(builder.slotFactory.create(this.container, index, x, y));
			}
		}

		if (builder.withPlayerInventory) {
			int invY = builder.playerInventoryY >= 0
				? builder.playerInventoryY
				: builder.slotOriginY + this.rows * 18 + 13;
			addPlayerInventory(playerInventory, builder.playerInventoryX, invY);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public Container getContainer() {
		return this.container;
	}

	public int getRows() {
		return this.rows;
	}

	public int getColumns() {
		return this.columns;
	}

	public int getContainerSlotCount() {
		return this.containerSlotCount;
	}

	/**
	 * Adds the standard 3-row + hotbar player inventory block at the given origin.
	 */
	protected void addPlayerInventory(Inventory playerInventory, int startX, int startY) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, 9 + row * 9 + col,
					startX + col * 18, startY + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(playerInventory, col, startX + col * 18, startY + 58));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack rawStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return rawStack;
		}
		ItemStack slotStack = slot.getItem();
		rawStack = slotStack.copy();

		int playerStart = this.containerSlotCount;
		int playerEnd = playerStart + 36;

		if (index < this.containerSlotCount) {
			if (!moveItemStackTo(slotStack, playerStart, playerEnd, true)) {
				return ItemStack.EMPTY;
			}
		} else if (index < playerEnd) {
			if (!moveItemStackTo(slotStack, 0, this.containerSlotCount, false)) {
				if (index < playerStart + 27) {
					if (!moveItemStackTo(slotStack, playerEnd - 9, playerEnd, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!moveItemStackTo(slotStack, playerStart, playerEnd - 9, false)) {
					return ItemStack.EMPTY;
				}
			}
		} else {
			return ItemStack.EMPTY;
		}

		if (slotStack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return rawStack;
	}

	@FunctionalInterface
	public interface SlotFactory {
		Slot create(Container container, int index, int x, int y);
	}

	public static class Builder {
		private Container container = null;
		private int rows = 3;
		private int columns = 9;
		private int slotOriginX = 8;
		private int slotOriginY = 18;
		private boolean withPlayerInventory = true;
		private int playerInventoryX = 8;
		private int playerInventoryY = -1; // -1 = auto
		private SlotFactory slotFactory = Slot::new;

		public Builder container(Container container) {
			this.container = container;
			return this;
		}

		public Builder rows(int rows) {
			this.rows = rows;
			return this;
		}

		public Builder columns(int columns) {
			this.columns = columns;
			return this;
		}

		public Builder size(int rows, int columns) {
			return rows(rows).columns(columns);
		}

		/**
		 * Top-left pixel position of the first container slot.
		 */
		public Builder slotOrigin(int x, int y) {
			this.slotOriginX = x;
			this.slotOriginY = y;
			return this;
		}

		public Builder withPlayerInventory(boolean v) {
			this.withPlayerInventory = v;
			return this;
		}

		/**
		 * Explicit player inventory origin. Auto-positioned below the container slots when unset.
		 */
		public Builder playerInventoryOrigin(int x, int y) {
			this.playerInventoryX = x;
			this.playerInventoryY = y;
			return this;
		}

		/**
		 * Custom slot factory for specialized slots (input-only, fluid, etc).
		 */
		public Builder slotFactory(SlotFactory factory) {
			this.slotFactory = factory;
			return this;
		}

		public EngineMenu build(@Nullable MenuType<?> type, int containerId, Inventory playerInventory) {
			return new EngineMenu(type, containerId, playerInventory, this);
		}
	}
}