package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.BeaconsReforged;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: nueva config editada desde el juego (solo operadores). */
public record UpdateConfigPayload(String json) implements CustomPacketPayload {
	public static final Type<UpdateConfigPayload> TYPE = new Type<>(BeaconsReforged.id("update_config"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateConfigPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, UpdateConfigPayload::json, UpdateConfigPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
