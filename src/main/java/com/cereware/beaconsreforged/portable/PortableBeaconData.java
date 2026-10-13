package com.cereware.beaconsreforged.portable;

import com.cereware.beaconsreforged.data.BeaconData;
import com.cereware.beaconsreforged.registry.ModComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * ContainerData del menú vanilla del beacon, pero respaldado por el ítem en la mano.
 * 0 = nivel de pirámide, 1 = primario, 2 = secundario. Al confirmar, el menú vanilla
 * cobra el pago y llama a set(1)/set(2): ahí se escriben los poderes en el ítem.
 */
public final class PortableBeaconData implements ContainerData {
	private final Player player;
	private final InteractionHand hand;
	private final ItemStack stack;
	private int primary, secondary;

	public PortableBeaconData(Player player, InteractionHand hand, ItemStack stack) {
		this.player = player;
		this.hand = hand;
		this.stack = stack;
		BeaconData d = stack.get(ModComponents.BEACON_DATA);
		if (d != null && !d.powers().isEmpty()) {
			BeaconData.Power p0 = d.powers().get(0);
			primary = encode(p0.effect());
			if (d.powers().size() > 1) secondary = encode(d.powers().get(1).effect());
			else if (p0.level() >= 2) secondary = primary;
		}
	}

	private static int encode(Holder<MobEffect> h) {
		return h == null ? 0 : BeaconMenu.encodeEffect(h);
	}

	@Override
	public int get(int index) {
		return switch (index) {
			case 0 -> {
				BeaconData d = stack.get(ModComponents.BEACON_DATA);
				yield d == null ? 0 : d.tier();
			}
			case 1 -> primary;
			case 2 -> secondary;
			default -> 0;
		};
	}

	@Override
	public void set(int index, int value) {
		if (index == 1) primary = value;
		else if (index == 2) secondary = value;
		else return;
		write();
	}

	private void write() {
		if (player.getItemInHand(hand) != stack) return;
		BeaconData d = stack.get(ModComponents.BEACON_DATA);
		if (d == null) return;
		Holder<MobEffect> p = primary == 0 ? null : BeaconMenu.decodeEffect(primary);
		Holder<MobEffect> s = secondary == 0 ? null : BeaconMenu.decodeEffect(secondary);
		List<BeaconData.Power> powers = new ArrayList<>();
		if (p != null && p.equals(s)) {
			powers.add(new BeaconData.Power(p, 2));
		} else {
			if (p != null) powers.add(new BeaconData.Power(p, 1));
			if (s != null) powers.add(new BeaconData.Power(s, 1));
		}
		stack.set(ModComponents.BEACON_DATA, new BeaconData(d.tier(), d.material(), d.unlockedLevel(), powers));
	}

	@Override
	public int getCount() {
		return 3;
	}
}
