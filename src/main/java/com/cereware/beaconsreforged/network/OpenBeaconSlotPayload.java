package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.BeaconsReforged;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: abrir el menú del slot de beacon (tecla C). */
public record OpenBeaconSlotPayload() implements CustomPacketPayload {
	public static final OpenBeaconSlotPayload INSTANCE = new OpenBeaconSlotPayload();
	public static final Type<OpenBeaconSlotPayload> TYPE = new Type<>(BeaconsReforged.id("open_beacon_slot"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenBeaconSlotPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
