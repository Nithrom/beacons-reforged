package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.BeaconsReforged;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor (modo full): poner el poder index del catálogo a level (0 = quitar). */
public record SetPowerPayload(int index, int level) implements CustomPacketPayload {
	public static final Type<SetPowerPayload> TYPE = new Type<>(BeaconsReforged.id("set_power"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetPowerPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, SetPowerPayload::index, ByteBufCodecs.VAR_INT, SetPowerPayload::level, SetPowerPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
