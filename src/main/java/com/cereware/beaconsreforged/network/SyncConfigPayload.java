package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.BeaconsReforged;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Servidor → cliente (al entrar y al cambiarla): config del servidor en JSON y si el jugador puede editarla. */
public record SyncConfigPayload(String json, boolean canEdit) implements CustomPacketPayload {
	public static final Type<SyncConfigPayload> TYPE = new Type<>(BeaconsReforged.id("sync_config"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncConfigPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, SyncConfigPayload::json, ByteBufCodecs.BOOL, SyncConfigPayload::canEdit, SyncConfigPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
