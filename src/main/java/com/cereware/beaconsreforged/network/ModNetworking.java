package com.cereware.beaconsreforged.network;

import com.cereware.beaconsreforged.full.FullBeacon;
import com.cereware.beaconsreforged.pyramid.PyramidConsumer;
import com.cereware.beaconsreforged.portable.PortableBeacon;
import com.cereware.beaconsreforged.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
	private ModNetworking() {}

	/** Operadores (nivel 2) o el dueño de la partida en un mundo individual. */
	public static boolean canEdit(ServerPlayer player) {
		var server = player.level().getServer();
		return server.isSingleplayerOwner(player.nameAndId()) || player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public static void init() {
		PayloadTypeRegistry.clientboundPlay().register(SyncConfigPayload.TYPE, SyncConfigPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UpdateConfigPayload.TYPE, UpdateConfigPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			sender.sendPacket(new SyncConfigPayload(ModConfig.toJson(), canEdit(handler.getPlayer()))));
		ServerPlayNetworking.registerGlobalReceiver(UpdateConfigPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			if (!canEdit(player)) return;
			try {
				ModConfig.replaceAndSave(new com.google.gson.Gson().fromJson(payload.json(), ModConfig.class));
			} catch (Exception e) {
				return;
			}
			for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
				ServerPlayNetworking.send(p, new SyncConfigPayload(ModConfig.toJson(), canEdit(p)));
			}
			player.sendSystemMessage(Component.translatable("message.beaconsreforged.config_saved"));
		});
		PayloadTypeRegistry.clientboundPlay().register(ConsumeStatusPayload.TYPE, ConsumeStatusPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ConsumePyramidPayload.TYPE, ConsumePyramidPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetPowerPayload.TYPE, SetPowerPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UnlockLevelPayload.TYPE, UnlockLevelPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SetPowerPayload.TYPE,
			(payload, context) -> FullBeacon.setLevel(context.player(), payload.index(), payload.level()));
		ServerPlayNetworking.registerGlobalReceiver(UnlockLevelPayload.TYPE,
			(payload, context) -> FullBeacon.unlock(context.player()));
		PayloadTypeRegistry.serverboundPlay().register(OpenBeaconSlotPayload.TYPE, OpenBeaconSlotPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenBeaconSlotPayload.TYPE,
			(payload, context) -> PortableBeacon.openSlotMenu(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(ConsumePyramidPayload.TYPE,
			(payload, context) -> PyramidConsumer.handleConsume(context.player(), payload.containerId()));
	}
}
