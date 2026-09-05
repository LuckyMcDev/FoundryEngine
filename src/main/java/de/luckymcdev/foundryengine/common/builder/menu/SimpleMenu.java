package de.luckymcdev.foundryengine.common.builder.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SimpleMenu extends AbstractContainerMenu {

	private final Container inventory;
	private final ContainerData data;
	private final int inventorySize;
	private final int dataSize;

	public SimpleMenu(MenuType<?> menuType, int containerId, Inventory playerInventory) {
		this(menuType, containerId, playerInventory, 0, 0);
	}

	public SimpleMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
	                  int inventorySize, int dataSize) {
		super(menuType, containerId);
		this.inventorySize = inventorySize;
		this.dataSize = dataSize;
		this.inventory = new SimpleContainer(inventorySize);
		this.data = new SimpleContainerData(dataSize);
		if (dataSize > 0) {
			addDataSlots(this.data);
		}
	}

	public SimpleMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
	                  Container inventory, ContainerData data) {
		super(menuType, containerId);
		this.inventory = inventory;
		this.data = data;
		this.inventorySize = inventory.getContainerSize();
		this.dataSize = data.getCount();
		if (dataSize > 0) {
			addDataSlots(this.data);
		}
	}

	public static SimpleMenu chest(int containerId, Inventory playerInventory, int rows) {
		SimpleMenu menu = new SimpleMenu(null, containerId, playerInventory, rows * 9, 0);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < 9; col++) {
				int index = row * 9 + col;
				int x = 8 + col * 18;
				int y = 18 + row * 18;
				menu.addSlot(new Slot(menu.inventory, index, x, y));
			}
		}
		menu.addStandardPlayerInventory(playerInventory, 8, 84 + rows * 18);
		return menu;
	}

	@Override
	public Slot addSlot(Slot slot) {
		return super.addSlot(slot);
	}

	public void addStandardPlayerInventory(Inventory playerInventory, int startX, int startY) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int index = 9 + row * 9 + col;
				int x = startX + col * 18;
				int y = startY + row * 18;
				this.addSlot(new Slot(playerInventory, index, x, y));
			}
		}
		for (int col = 0; col < 9; col++) {
			int x = startX + col * 18;
			int y = startY + 58;
			this.addSlot(new Slot(playerInventory, col, x, y));
		}
	}

	public boolean moveItemStackToPublic(ItemStack stack, int start, int end, boolean reverse) {
		return super.moveItemStackTo(stack, start, end, reverse);
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack rawStack = slot.getItem();
		ItemStack quickMoved = rawStack.copy();

		int playerStart = this.inventorySize;
		int playerEnd = playerStart + 36;

		if (index < this.inventorySize) {
			if (!moveItemStackToPublic(rawStack, playerStart, playerEnd, false)) {
				return ItemStack.EMPTY;
			}
		} else if (index < playerEnd) {
			if (!moveItemStackToPublic(rawStack, 0, this.inventorySize, false)) {
				return ItemStack.EMPTY;
			}
		} else {
			return ItemStack.EMPTY;
		}

		if (rawStack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return quickMoved;
	}

	public Container getInventory() {
		return inventory;
	}

	public ContainerData getData() {
		return data;
	}
}