package com.cereware.beaconsreforged.mixin;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prisa del beacon: multiplica el progreso de picado solo si la herramienta es la adecuada para ese bloque.
 * Va aquí y no en Player.getDestroySpeed porque otros mods (p. ej. Collective) sobrescriben ese valor.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	private void beaconsreforged$haste(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		MobEffectInstance haste = player.getEffect(ModEffects.HASTE);
		if (haste == null || player.getMainHandItem().getDestroySpeed(state) <= 1.0f) return;
		double[] mult = ModConfig.get().hasteMiningMultipliers;
		if (mult.length == 0) return;
		double m = mult[Math.min(haste.getAmplifier(), mult.length - 1)];
		cir.setReturnValue((float) (cir.getReturnValue() * m));
	}
}
