package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.BeaconsReforged;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: el jugador ha confirmado consumir la pirámide. */
public record ConsumePyramidPayload(int containerId) implements CustomPacketPayload {
	public static final Type<ConsumePyramidPayload> TYPE = new Type<>(BeaconsReforged.id("consume_pyramid"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConsumePyramidPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, ConsumePyramidPayload::containerId, ConsumePyramidPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
