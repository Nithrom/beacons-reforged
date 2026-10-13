package com.cereware.beaconsreforged.portable;

import com.cereware.beaconsreforged.data.BeaconData;
import com.cereware.beaconsreforged.registry.ModComponents;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

/** Aplica cada segundo los poderes del beacon equipado y retira los que ya no correspondan. */
public final class BeaconEffectTicker {
	private static final int DURATION = 260; // >200 para que el icono no parpadee
	private static final Map<UUID, Set<Holder<MobEffect>>> APPLIED = new HashMap<>();

	private BeaconEffectTicker() {}

	static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 != 0) return;
			for (ServerPlayer p : server.getPlayerList().getPlayers()) tick(p);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> APPLIED.remove(handler.getPlayer().getUUID()));
	}

	private static void tick(ServerPlayer player) {
		Set<Holder<MobEffect>> now = new HashSet<>();
		ItemStack equipped = PortableBeacon.getEquipped(player);
		BeaconData data = equipped.get(ModComponents.BEACON_DATA);
		if (data != null) {
			BeaconData clamped = data.clampToConfig();
			if (clamped != data) {
				equipped.set(ModComponents.BEACON_DATA, clamped);
				PortableBeacon.setEquipped(player, equipped);
				data = clamped;
			}
		}
		if (data != null && player.isAlive()) {
			for (BeaconData.Power power : data.powers()) {
				now.add(power.effect());
				player.addEffect(new MobEffectInstance(power.effect(), DURATION, power.level() - 1, true, false, true));
			}
		}
		Set<Holder<MobEffect>> previous = APPLIED.put(player.getUUID(), now);
		if (previous == null) return;
		for (Holder<MobEffect> effect : previous) {
			if (now.contains(effect)) continue;
			MobEffectInstance current = player.getEffect(effect);
			if (current != null && current.isAmbient() && current.getDuration() <= DURATION) player.removeEffect(effect);
		}
	}
}
