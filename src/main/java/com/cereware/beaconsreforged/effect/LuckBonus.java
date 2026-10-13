package com.cereware.beaconsreforged.effect;

import com.cereware.beaconsreforged.config.ModConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Suerte nivel N = +N a Fortuna/Botín efectivos, sin pasar del tope (y sin bajar el encantamiento). */
public final class LuckBonus {
	private LuckBonus() {}

	public static int apply(int base, Entity entity) {
		if (!(entity instanceof LivingEntity living)) return base;
		MobEffectInstance luck = living.getEffect(ModEffects.LUCK);
		if (luck == null) return base;
		int boosted = Math.min(base + luck.getAmplifier() + 1, ModConfig.get().luckTotalCap);
		return Math.max(base, boosted);
	}
}
