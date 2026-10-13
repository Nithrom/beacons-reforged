package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.BeaconsReforged;
import com.cereware.beaconsreforged.data.PyramidMaterial;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Servidor → cliente: estado del botón "Consumir pirámide" para el menú abierto. */
public record ConsumeStatusPayload(int containerId, int state, int tier, PyramidMaterial material, int blockCount)
	implements CustomPacketPayload {
	public static final Type<ConsumeStatusPayload> TYPE = new Type<>(BeaconsReforged.id("consume_status"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeStatusPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, ConsumeStatusPayload::containerId,
		ByteBufCodecs.VAR_INT, ConsumeStatusPayload::state,
		ByteBufCodecs.VAR_INT, ConsumeStatusPayload::tier,
		PyramidMaterial.STREAM_CODEC, ConsumeStatusPayload::material,
		ByteBufCodecs.VAR_INT, ConsumeStatusPayload::blockCount,
		ConsumeStatusPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
