package com.cereware.beaconsreforged.full;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.effect.ModEffects;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Poderes del modo full: tamaño de pirámide requerido y nivel máximo de cada uno. */
public final class PowerCatalog {
	public record Entry(Holder<MobEffect> effect, int requiredTier) {
		/** Nivel máximo configurable (config maxLevelPerEffect). */
		public int maxLevel() {
			return ModConfig.get().maxLevelFor(effect.unwrapKey().map(k -> k.identifier().toString()).orElse(""));
		}
	}

	public static final List<Entry> ENTRIES = List.of(
		new Entry(MobEffects.SPEED, 1),
		new Entry(ModEffects.HASTE, 1),
		new Entry(MobEffects.RESISTANCE, 2),
		new Entry(MobEffects.JUMP_BOOST, 2),
		new Entry(MobEffects.STRENGTH, 3),
		new Entry(ModEffects.LUCK, 3),
		new Entry(MobEffects.REGENERATION, 4),
		new Entry(ModEffects.REACH, 4));

	/** Ítem de pago por nivel 1..5. */
	public static final List<Item> PAYMENT = List.of(Items.IRON_INGOT, Items.GOLD_INGOT, Items.EMERALD, Items.DIAMOND, Items.NETHERITE_INGOT);

	private PowerCatalog() {}

	public static int indexOf(Holder<MobEffect> effect) {
		for (int i = 0; i < ENTRIES.size(); i++) {
			if (ENTRIES.get(i).effect().value() == effect.value()) return i;
		}
		return -1;
	}

	public static Item paymentItem(int level) {
		return PAYMENT.get(level - 1);
	}

	public static int paymentLevel(ItemStack stack) {
		for (int i = 0; i < PAYMENT.size(); i++) if (stack.is(PAYMENT.get(i))) return i + 1;
		return 0;
	}
}
