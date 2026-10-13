package com.cereware.beaconsreforged.full;

import com.cereware.beaconsreforged.data.BeaconData;
import com.cereware.beaconsreforged.registry.ModComponents;
import com.cereware.beaconsreforged.registry.ModMenus;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menú del modo full con la misma disposición que el beacon vanilla: ranura de pago + inventario. */
public class FullBeaconMenu extends AbstractContainerMenu {
	private final Player player;
	private final SimpleContainer payment = new SimpleContainer(1);
	public final Slot paymentSlot;

	public FullBeaconMenu(int id, Inventory inventory) {
		super(ModMenus.FULL_BEACON, id);
		this.player = inventory.player;
		paymentSlot = addSlot(new Slot(payment, 0, 136, 110) {
			@Override public boolean mayPlace(ItemStack stack) { return isValidPayment(stack); }
			@Override public int getMaxStackSize() { return 1; }
		});
		addStandardInventorySlots(inventory, 36, 137);
	}

	/** Solo materiales que suban el nivel máximo y que la pirámide admita. */
	public boolean isValidPayment(ItemStack stack) {
		BeaconData d = FullBeacon.held(player).get(ModComponents.BEACON_DATA);
		return d != null && FullBeacon.canUnlock(d, PowerCatalog.paymentLevel(stack));
	}

	@Override
	public boolean stillValid(Player player) {
		return !FullBeacon.held(player).isEmpty();
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		clearContainer(player, payment);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index == 0) {
			if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
		} else if (!paymentSlot.hasItem() && isValidPayment(stack)) {
			if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
		} else {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
		else slot.setChanged();
		return original;
	}
}
