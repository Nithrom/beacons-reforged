package com.cereware.beaconsreforged.portable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Container de 1 hueco que lee/escribe directamente el beacon equipado del jugador. */
public final class EquippedBeaconContainer implements Container {
	private final Player player;

	public EquippedBeaconContainer(Player player) {
		this.player = player;
	}

	@Override public int getContainerSize() { return 1; }
	@Override public int getMaxStackSize() { return 1; }
	@Override public boolean isEmpty() { return PortableBeacon.getEquipped(player).isEmpty(); }
	@Override public ItemStack getItem(int slot) { return PortableBeacon.getEquipped(player); }

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack s = PortableBeacon.getEquipped(player);
		if (s.isEmpty() || amount <= 0) return ItemStack.EMPTY;
		ItemStack taken = s.split(amount);
		PortableBeacon.setEquipped(player, s);
		return taken;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack s = PortableBeacon.getEquipped(player);
		PortableBeacon.setEquipped(player, ItemStack.EMPTY);
		return s;
	}

	@Override public void setItem(int slot, ItemStack stack) { PortableBeacon.setEquipped(player, stack); }
	@Override public void setChanged() {}
	@Override public boolean stillValid(Player p) { return p == player; }
	@Override public void clearContent() { PortableBeacon.setEquipped(player, ItemStack.EMPTY); }
}
