package com.cereware.beaconsreforged.data;

import com.cereware.beaconsreforged.config.ModConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

/**
 * Datos de un beacon que ha consumido una pirámide. Se guarda como data component del ítem,
 * independiente del nombre (renombrar no lo toca).
 *
 * @param tier          nivel de pirámide 1-4 (3x3 .. 9x9)
 * @param material      material de la pirámide
 * @param unlockedLevel nivel máximo de poder desbloqueado con pago (0 = ninguno)
 * @param powers        poderes seleccionados
 */
public record BeaconData(int tier, PyramidMaterial material, int unlockedLevel, List<Power> powers) implements TooltipProvider {

	public record Power(Holder<MobEffect> effect, int level) {
		public static final Codec<Power> CODEC = RecordCodecBuilder.create(i -> i.group(
			MobEffect.CODEC.fieldOf("effect").forGetter(Power::effect),
			ExtraCodecs.intRange(1, 5).fieldOf("level").forGetter(Power::level)
		).apply(i, Power::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, Power> STREAM_CODEC = StreamCodec.composite(
			MobEffect.STREAM_CODEC, Power::effect,
			ByteBufCodecs.VAR_INT, Power::level,
			Power::new);
	}

	public static final Codec<BeaconData> CODEC = RecordCodecBuilder.create(i -> i.group(
		ExtraCodecs.intRange(1, 4).fieldOf("tier").forGetter(BeaconData::tier),
		PyramidMaterial.CODEC.fieldOf("material").forGetter(BeaconData::material),
		ExtraCodecs.intRange(0, 5).optionalFieldOf("unlocked_level", 0).forGetter(BeaconData::unlockedLevel),
		Power.CODEC.listOf().optionalFieldOf("powers", List.of()).forGetter(BeaconData::powers)
	).apply(i, BeaconData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BeaconData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, BeaconData::tier,
		PyramidMaterial.STREAM_CODEC, BeaconData::material,
		ByteBufCodecs.VAR_INT, BeaconData::unlockedLevel,
		Power.STREAM_CODEC.apply(ByteBufCodecs.list()), BeaconData::powers,
		BeaconData::new);

	public BeaconData {
		powers = List.copyOf(powers);
	}

	public int pyramidSize() {
		return tier * 2 + 1;
	}

	/**
	 * Aplica los niveles máximos actuales de la config. Si se bajó un tope, los poderes guardados
	 * se recortan (y se liberan esos puntos). Devuelve this si no hay cambios.
	 */
	public BeaconData clampToConfig() {
		boolean changed = false;
		java.util.List<Power> out = new java.util.ArrayList<>();
		for (Power p : powers) {
			String id = p.effect().unwrapKey().map(k -> k.identifier().toString()).orElse("");
			int cap = ModConfig.get().maxLevelFor(id);
			if (p.level() > cap) {
				out.add(new Power(p.effect(), cap));
				changed = true;
			} else {
				out.add(p);
			}
		}
		return changed ? new BeaconData(tier, material, unlockedLevel, out) : this;
	}

	public int usedPoints() {
		return powers.stream().mapToInt(Power::level).sum();
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> out, TooltipFlag flag, DataComponentGetter components) {
		int size = pyramidSize();
		out.accept(Component.translatable("tooltip.beaconsreforged.pyramid", size, size,
			Component.translatable(material.translationKey())).withStyle(ChatFormatting.GOLD));
		if (ModConfig.isFull() && material.points > 0) {
			out.accept(Component.translatable("tooltip.beaconsreforged.points", usedPoints(), material.points)
				.withStyle(ChatFormatting.GRAY));
		}
		if (unlockedLevel > 0) {
			out.accept(Component.translatable("tooltip.beaconsreforged.max_level",
				Component.translatable("enchantment.level." + unlockedLevel)).withStyle(ChatFormatting.GRAY));
		}
		if (powers.isEmpty()) {
			out.accept(Component.translatable("tooltip.beaconsreforged.no_powers").withStyle(ChatFormatting.DARK_GRAY));
		} else {
			out.accept(Component.translatable("tooltip.beaconsreforged.powers").withStyle(ChatFormatting.GRAY));
			for (Power p : powers) {
				out.accept(Component.literal(" • ").append(p.effect().value().getDisplayName())
					.append(" ").append(Component.translatable("enchantment.level." + p.level()))
					.withStyle(ChatFormatting.AQUA));
			}
		}
	}
}
