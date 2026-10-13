package com.cereware.beaconsreforged.portable;

import com.cereware.beaconsreforged.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menú del slot propio: 1 hueco de beacon + inventario del jugador. Shift-clic intercambia beacons. */
public class BeaconSlotMenu extends AbstractContainerMenu {
	public static final int SLOT_X = 80, SLOT_Y = 20;

	public BeaconSlotMenu(int id, Inventory inventory) {
		this(id, inventory, new SimpleContainer(1));
	}

	public BeaconSlotMenu(int id, Inventory inventory, Container container) {
		super(ModMenus.BEACON_SLOT, id);
		addSlot(new Slot(container, 0, SLOT_X, SLOT_Y) {
			@Override public boolean mayPlace(ItemStack stack) { return PortableBeacon.isPortableBeacon(stack); }
			@Override public int getMaxStackSize() { return 1; }
		});
		addStandardInventorySlots(inventory, 8, 51);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		if (index == 0) {
			ItemStack original = stack.copy();
			if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
			if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
			return original;
		}
		if (!PortableBeacon.isPortableBeacon(stack)) return ItemStack.EMPTY;
		Slot beaconSlot = slots.get(0);
		if (!beaconSlot.hasItem()) {
			beaconSlot.set(stack.split(1));
			slot.setChanged();
		} else if (stack.getCount() == 1) {
			ItemStack old = beaconSlot.getItem();
			beaconSlot.set(stack);
			slot.set(old);
		}
		return ItemStack.EMPTY;
	}
}
