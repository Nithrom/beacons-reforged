package com.cereware.beaconsreforged.mixin;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prisa del beacon: multiplica la velocidad de picado solo si la herramienta es la adecuada para ese bloque. */
@Mixin(Player.class)
public abstract class PlayerMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void beaconsreforged$haste(BlockState state, CallbackInfoReturnable<Float> cir) {
		Player self = (Player) (Object) this;
		MobEffectInstance haste = self.getEffect(ModEffects.HASTE);
		if (haste == null || self.getMainHandItem().getDestroySpeed(state) <= 1.0f) return;
		double[] mult = ModConfig.get().hasteMiningMultipliers;
		if (mult.length == 0) return;
		double m = mult[Math.min(haste.getAmplifier(), mult.length - 1)];
		cir.setReturnValue((float) (cir.getReturnValue() * m));
	}
}
