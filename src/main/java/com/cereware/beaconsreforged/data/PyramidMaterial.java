package com.cereware.beaconsreforged.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/** Material de la pirámide consumida. MIXED = pirámide mezclada (solo válida en modo vanilla). */
public enum PyramidMaterial implements StringRepresentable {
	MIXED("mixed", 0, 0),
	IRON("iron", 2, 1),
	GOLD("gold", 3, 2),
	EMERALD("emerald", 4, 3),
	DIAMOND("diamond", 5, 4),
	NETHERITE("netherite", 8, 5);

	public static final Codec<PyramidMaterial> CODEC = StringRepresentable.fromEnum(PyramidMaterial::values);
	public static final StreamCodec<ByteBuf, PyramidMaterial> STREAM_CODEC =
		ByteBufCodecs.idMapper(i -> values()[i], PyramidMaterial::ordinal);

	private final String name;
	/** Puntos de poder que da la pirámide (modo full). */
	public final int points;
	/** Nivel máximo de poder que da este material usado como pago (modo full). */
	public final int paymentLevel;

	PyramidMaterial(String name, int points, int paymentLevel) {
		this.name = name;
		this.points = points;
		this.paymentLevel = paymentLevel;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public String translationKey() {
		return "material.beaconsreforged." + name;
	}
}
