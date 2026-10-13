package com.cereware.beaconsreforged.client;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.network.OpenBeaconSlotPayload;
import com.cereware.beaconsreforged.network.SyncConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.cereware.beaconsreforged.registry.ModMenus;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;

public class BeaconsReforgedClient implements ClientModInitializer {
	public static final KeyMapping OPEN_SLOT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.beaconsreforged.open_slot", InputConstants.Type.KEYBOARD, InputConstants.KEY_C, KeyMapping.Category.INVENTORY));

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
		MenuScreens.register(ModMenus.BEACON_SLOT, BeaconSlotScreen::new);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openConfigNextTick) {
				openConfigNextTick = false;
				client.gui.setScreen(new ConfigScreen(null));
			}
			while (OPEN_SLOT.consumeClick()) {
				if (client.player != null) ClientPlayNetworking.send(OpenBeaconSlotPayload.INSTANCE);
			}
		});
	}
}
