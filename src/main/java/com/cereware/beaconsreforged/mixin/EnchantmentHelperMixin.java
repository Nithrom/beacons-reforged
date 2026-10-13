package com.cereware.beaconsreforged.mixin;

import com.cereware.beaconsreforged.effect.LuckBonus;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Suerte → Botín extra (drops de mobs, cabezas de wither…). */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
	@Inject(method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I",
		at = @At("RETURN"), cancellable = true)
	private static void beaconsreforged$luckLooting(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		if (enchantment.is(Enchantments.LOOTING)) cir.setReturnValue(LuckBonus.apply(cir.getReturnValue(), entity));
	}
}
