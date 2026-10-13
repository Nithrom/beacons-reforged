package com.cereware.beaconsreforged.mixin;

import com.cereware.beaconsreforged.effect.LuckBonus;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Suerte → Fortuna extra al romper bloques. */
@Mixin(ApplyBonusCount.class)
public abstract class ApplyBonusCountMixin {
	@Shadow @Final private Holder<Enchantment> enchantment;

	@ModifyVariable(method = "run", at = @At("STORE"), ordinal = 0)
	private int beaconsreforged$luckFortune(int level, ItemStack stack, LootContext context) {
		if (!enchantment.is(Enchantments.FORTUNE)) return level;
		return LuckBonus.apply(level, context.getOptional(LootContextParams.THIS_ENTITY));
	}
}
