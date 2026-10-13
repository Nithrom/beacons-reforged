package com.cereware.beaconsreforged.client;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.full.PowerCatalog;
import com.cereware.beaconsreforged.network.UpdateConfigPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Configuración dentro del juego (sin dependencias). Abre con Mod Menu o /beaconsreforged config.
 * En un servidor solo los operadores pueden cambiar lo que no es de cliente.
 */
public class ConfigScreen extends Screen {
	private final @Nullable Screen parent;
	private final ModConfig edit = ModConfig.copy();
	private final List<EditBox> haste = new ArrayList<>();
	private final List<CycleButton<Integer>> maxLevels = new ArrayList<>();
	private EditBox consumedBlock;

	public ConfigScreen(@Nullable Screen parent) {
		super(Component.translatable("config.beaconsreforged.title"));
		this.parent = parent;
	}

	private static boolean serverEditable() {
		return !ModConfig.isConnectedToServer() || ModConfig.canEditServer;
	}

	private static Component onOff(boolean b) {
		return Component.translatable(b ? "options.on" : "options.off");
	}

	@Override
	protected void init() {
		haste.clear();
		maxLevels.clear();
		boolean editable = serverEditable();
		int left = width / 2 - 155, right = width / 2 + 5, y0 = 36;

		// Columna izquierda: general
		var mode = addRenderableWidget(CycleButton.<ModConfig.Mode>builder(m -> Component.translatable("config.beaconsreforged.mode." + m), edit.mode)
			.withValues(ModConfig.Mode.values())
			.create(left, y0, 150, 20, Component.translatable("config.beaconsreforged.mode"), (b, v) -> edit.mode = v));
		mode.active = editable;

		consumedBlock = new EditBox(font, left, y0 + 36, 150, 18, Component.translatable("config.beaconsreforged.consumed_block"));
		consumedBlock.setMaxLength(100);
		consumedBlock.setValue(edit.consumedBlock);
		consumedBlock.active = editable;
		addRenderableWidget(consumedBlock);

		addRenderableWidget(CycleButton.builder(ConfigScreen::onOff, edit.showConsumeConfirmation).withValues(true, false)
			.create(left, y0 + 60, 150, 20, Component.translatable("config.beaconsreforged.confirm"), (b, v) -> edit.showConsumeConfirmation = v));

		var keep = addRenderableWidget(CycleButton.builder(ConfigScreen::onOff, edit.keepBeaconOnDeath).withValues(true, false)
			.create(left, y0 + 84, 150, 20, Component.translatable("config.beaconsreforged.keep_on_death"), (b, v) -> edit.keepBeaconOnDeath = v));
		keep.active = editable;

		List<Integer> caps = new ArrayList<>();
		for (int i = 0; i <= 20; i++) caps.add(i);
		var luck = addRenderableWidget(CycleButton.<Integer>builder(v -> Component.literal(String.valueOf(v)), edit.luckTotalCap).withValues(caps)
			.create(left, y0 + 108, 150, 20, Component.translatable("config.beaconsreforged.luck_cap"), (b, v) -> edit.luckTotalCap = v));
		luck.active = editable;

		for (int i = 0; i < 5; i++) {
			EditBox box = new EditBox(font, left + i * 30, y0 + 144, 28, 18, Component.literal("x" + (i + 1)));
			box.setMaxLength(6);
			box.setValue(trim(edit.hasteMiningMultipliers[i]));
			box.active = editable;
			haste.add(addRenderableWidget(box));
		}

		// Columna derecha: nivel máximo por poder
		for (int i = 0; i < PowerCatalog.ENTRIES.size(); i++) {
			PowerCatalog.Entry e = PowerCatalog.ENTRIES.get(i);
			String id = e.effect().unwrapKey().map(k -> k.identifier().toString()).orElse("");
			var b = addRenderableWidget(CycleButton.<Integer>builder(v -> Component.translatable("enchantment.level." + v), edit.maxLevelFor(id))
				.withValues(1, 2, 3, 4, 5)
				.create(right, y0 + 12 + i * 22, 150, 20, e.effect().value().getDisplayName(), (btn, v) -> edit.maxLevelPerEffect.put(id, v)));
			b.active = editable;
			maxLevels.add(b);
		}

		int by = y0 + 12 + 8 * 22 + 6;
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> saveAndClose()).bounds(width / 2 - 155, by, 150, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).bounds(width / 2 + 5, by, 150, 20).build());
	}

	private static String trim(double d) {
		return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
	}

	private void saveAndClose() {
		edit.consumedBlock = consumedBlock.getValue().trim();
		for (int i = 0; i < 5; i++) {
			try {
				edit.hasteMiningMultipliers[i] = Double.parseDouble(haste.get(i).getValue().trim().replace(',', '.'));
			} catch (NumberFormatException ignored) {
				// se mantiene el valor anterior
			}
		}
		edit.sanitize();
		ModConfig.setShowConsumeConfirmation(edit.showConsumeConfirmation);
		if (!ModConfig.isConnectedToServer()) {
			ModConfig.replaceAndSave(edit);
		} else if (ModConfig.canEditServer) {
			ClientPlayNetworking.send(new UpdateConfigPayload(ModConfig.toJson(edit)));
		}
		onClose();
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().gui.setScreen(parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int left = width / 2 - 155, right = width / 2 + 5, y0 = 36;
		center(g, title, 12, 0xFFFFFFFF);
		if (!serverEditable()) center(g, Component.translatable("config.beaconsreforged.no_permission"), 24, 0xFFFF5555);
		g.text(font, Component.translatable("config.beaconsreforged.consumed_block"), left, y0 + 25, 0xFFA0A0A0, true);
		g.text(font, Component.translatable("config.beaconsreforged.haste"), left, y0 + 133, 0xFFA0A0A0, true);
		g.text(font, Component.translatable("config.beaconsreforged.max_levels"), right, y0, 0xFFA0A0A0, true);
	}

	private void center(GuiGraphicsExtractor g, Component text, int y, int color) {
		g.text(font, text, width / 2 - font.width(text) / 2, y, color, true);
	}
}
