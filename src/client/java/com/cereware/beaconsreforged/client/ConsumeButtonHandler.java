package com.cereware.beaconsreforged.client;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.client.mixin.AbstractContainerScreenAccessor;
import com.cereware.beaconsreforged.network.ConsumePyramidPayload;
import com.cereware.beaconsreforged.network.ConsumeStatusPayload;
import com.cereware.beaconsreforged.pyramid.PyramidConsumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;

/** Añade el botón "Consumir pirámide" al menú del beacon y gestiona la confirmación. */
public final class ConsumeButtonHandler {
	private static ConsumeStatusPayload status;

	private ConsumeButtonHandler() {}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(ConsumeStatusPayload.TYPE, (payload, context) -> status = payload);
		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (screen instanceof BeaconScreen beaconScreen) addButton(client, beaconScreen);
		});
	}

	private static void addButton(Minecraft client, BeaconScreen screen) {
		ConsumeStatusPayload s = status;
		if (s == null || s.containerId() != screen.getMenu().containerId || s.state() == PyramidConsumer.HIDDEN) return;
		AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
		int size = s.tier() * 2 + 1;
		Component material = Component.translatable(s.material().translationKey());

		Button button = Button.builder(Component.translatable("gui.beaconsreforged.consume", size, size, material), b -> {
			if (!ModConfig.get().showConsumeConfirmation) {
				ClientPlayNetworking.send(new ConsumePyramidPayload(s.containerId()));
				return;
			}
			Component title = Component.translatable("gui.beaconsreforged.confirm.title");
			Component msg = Component.translatable("gui.beaconsreforged.confirm.message", size, size, material, s.blockCount());
			client.gui.setScreen(new ConfirmScreen(yes -> {
				if (yes) ClientPlayNetworking.send(new ConsumePyramidPayload(s.containerId()));
				client.gui.setScreen(screen);
			}, title, msg));
		}).bounds(acc.beaconsreforged$getLeftPos(), acc.beaconsreforged$getTopPos() - 24, acc.beaconsreforged$getImageWidth(), 20).build();

		if (s.state() != PyramidConsumer.AVAILABLE) {
			button.active = false;
			String reason = s.state() == PyramidConsumer.MIXED ? "mixed" : "not_better";
			button.setTooltip(Tooltip.create(Component.translatable("gui.beaconsreforged.consume." + reason)));
		}
		Screens.getWidgets(screen).add(button);
	}
}
