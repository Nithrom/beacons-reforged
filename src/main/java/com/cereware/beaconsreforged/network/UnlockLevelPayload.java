package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.BeaconsReforged;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor (modo full): pagar para desbloquear el nivel máximo indicado. */
public record UnlockLevelPayload(int level) implements CustomPacketPayload {
	public static final Type<UnlockLevelPayload> TYPE = new Type<>(BeaconsReforged.id("unlock_level"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UnlockLevelPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, UnlockLevelPayload::level, UnlockLevelPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
