package com.cereware.beaconsreforged.client;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.network.SyncConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.cereware.beaconsreforged.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public class BeaconsReforgedClient implements ClientModInitializer {
	private static boolean openConfigNextTick;

	@Override
	public void onInitializeClient() {
		ConsumeButtonHandler.init();
		ClientPlayNetworking.registerGlobalReceiver(SyncConfigPayload.TYPE, (payload, context) -> {
			ModConfig.applySynced(payload.json());
			ModConfig.canEditServer = payload.canEdit();
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
			ClientCommands.literal("beaconsreforged").then(ClientCommands.literal("config").executes(c -> {
				openConfigNextTick = true;
				return 1;
			}))));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ModConfig.clearSynced());
		MenuScreens.register(ModMenus.FULL_BEACON, FullBeaconScreen::new);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openConfigNextTick) {
				openConfigNextTick = false;
				client.gui.setScreen(new ConfigScreen(null));
			}
		});
	}
}
