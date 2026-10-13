package com.cereware.beaconsreforged.effect;

import com.cereware.beaconsreforged.BeaconsReforged;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Poderes propios: Suerte (Fortuna + Botín + suerte vanilla) y Alcance (+1 bloque por nivel). */
public final class ModEffects {
	public static final Holder<MobEffect> LUCK = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
		BeaconsReforged.id("luck"),
		new BeaconEffect(MobEffectCategory.BENEFICIAL, 0x59C106)
			.addAttributeModifier(Attributes.LUCK, BeaconsReforged.id("effect.luck"), 1.0, AttributeModifier.Operation.ADD_VALUE));

	/**
	 * Prisa del modo full: no acelera la animación del brazo como la vanilla. El multiplicador de picado
	 * se aplica en PlayerMixin y solo con la herramienta adecuada para el bloque.
	 */
	public static final Holder<MobEffect> HASTE = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
		BeaconsReforged.id("haste"),
		new BeaconEffect(MobEffectCategory.BENEFICIAL, 0xD9C043));

	public static final Holder<MobEffect> REACH = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
		BeaconsReforged.id("reach"),
		new BeaconEffect(MobEffectCategory.BENEFICIAL, 0x8F5BD6)
			.addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, BeaconsReforged.id("effect.reach_block"), 1.0, AttributeModifier.Operation.ADD_VALUE)
			.addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE, BeaconsReforged.id("effect.reach_entity"), 1.0, AttributeModifier.Operation.ADD_VALUE));

	private ModEffects() {}

	public static void init() {}

	static final class BeaconEffect extends MobEffect {
		BeaconEffect(MobEffectCategory category, int color) {
			super(category, color);
		}
	}
}
