package com.cereware.beaconsreforged.full;

import com.cereware.beaconsreforged.data.BeaconData;
import com.cereware.beaconsreforged.portable.PortableBeacon;
import com.cereware.beaconsreforged.registry.ModComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Reglas del modo full (validadas siempre en el servidor). */
public final class FullBeacon {
	private FullBeacon() {}

	/** Beacon portable en la mano (principal primero, igual que al usarlo). */
	public static ItemStack held(Player player) {
		ItemStack main = player.getMainHandItem();
		if (PortableBeacon.isPortableBeacon(main)) return main;
		ItemStack off = player.getOffhandItem();
		return PortableBeacon.isPortableBeacon(off) ? off : ItemStack.EMPTY;
	}

	public static int levelOf(BeaconData data, int index) {
		PowerCatalog.Entry entry = PowerCatalog.ENTRIES.get(index);
		for (BeaconData.Power p : data.powers()) if (p.effect().value() == entry.effect().value()) return p.level();
		return 0;
	}

	public static int maxLevel(BeaconData data, int index) {
		PowerCatalog.Entry entry = PowerCatalog.ENTRIES.get(index);
		if (data.tier() < entry.requiredTier()) return 0;
		return Math.min(data.unlockedLevel(), entry.maxLevel());
	}

	/** ¿Se puede poner este poder a este nivel? */
	public static boolean canSet(BeaconData data, int index, int level) {
		if (index < 0 || index >= PowerCatalog.ENTRIES.size() || level < 0) return false;
		if (level > maxLevel(data, index)) return false;
		int points = data.usedPoints() - levelOf(data, index) + level;
		return points <= data.material().points;
	}

	public static void setLevel(ServerPlayer player, int index, int level) {
		ItemStack stack = held(player);
		BeaconData data = stack.get(ModComponents.BEACON_DATA);
		if (data == null || !canSet(data, index, level)) return;
		List<BeaconData.Power> powers = new ArrayList<>();
		for (int i = 0; i < PowerCatalog.ENTRIES.size(); i++) {
			int l = i == index ? level : levelOf(data, i);
			if (l > 0) powers.add(new BeaconData.Power(PowerCatalog.ENTRIES.get(i).effect(), l));
		}
		stack.set(ModComponents.BEACON_DATA, new BeaconData(data.tier(), data.material(), data.unlockedLevel(), powers));
		syncHeld(player);
	}

	/** Fuerza el reenvío del inventario al cliente: el menú lee los datos del beacon de la mano. */
	private static void syncHeld(ServerPlayer player) {
		player.containerMenu.broadcastFullState();
		player.inventoryMenu.broadcastFullState();
	}

	/** ¿Se puede desbloquear este nivel? (sin mirar si tiene el ítem) */
	public static boolean canUnlock(BeaconData data, int level) {
		return level >= 1 && level <= 5 && level <= data.material().points && level > data.unlockedLevel();
	}

	/** Paga con el ítem de la ranura del menú abierto y sube el nivel máximo. */
	public static void unlock(ServerPlayer player) {
		if (!(player.containerMenu instanceof FullBeaconMenu menu)) return;
		ItemStack stack = held(player);
		BeaconData data = stack.get(ModComponents.BEACON_DATA);
		int level = PowerCatalog.paymentLevel(menu.paymentSlot.getItem());
		if (data == null || !canUnlock(data, level)) return;
		menu.paymentSlot.remove(1);
		stack.set(ModComponents.BEACON_DATA, new BeaconData(data.tier(), data.material(), level, data.powers()));
		syncHeld(player);
		player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.0f);
		player.sendSystemMessage(Component.translatable("message.beaconsreforged.unlocked",
			Component.translatable("enchantment.level." + level)));
	}
}
