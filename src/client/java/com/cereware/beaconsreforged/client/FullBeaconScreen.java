package com.cereware.beaconsreforged.client;

import com.cereware.beaconsreforged.data.BeaconData;
import com.cereware.beaconsreforged.full.FullBeacon;
import com.cereware.beaconsreforged.full.FullBeaconMenu;
import com.cereware.beaconsreforged.full.PowerCatalog;
import com.cereware.beaconsreforged.network.SetPowerPayload;
import com.cereware.beaconsreforged.network.UnlockLevelPayload;
import com.cereware.beaconsreforged.registry.ModComponents;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Menú del modo full con el aspecto del beacon vanilla. Poderes en escalera por tamaño de pirámide
 * (3x3/5x5/7x7 a la izquierda, 9x9 a la derecha). Clic izq. sube nivel, clic der. lo baja.
 */
public class FullBeaconScreen extends AbstractContainerScreen<FullBeaconMenu> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/beacon.png");
	private static final Identifier BUTTON = Identifier.withDefaultNamespace("container/beacon/button");
	private static final Identifier BUTTON_DISABLED = Identifier.withDefaultNamespace("container/beacon/button_disabled");
	private static final Identifier BUTTON_SELECTED = Identifier.withDefaultNamespace("container/beacon/button_selected");
	private static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("container/beacon/button_highlighted");
	private static final Identifier CONFIRM = Identifier.withDefaultNamespace("container/beacon/confirm");
	private static final Identifier CANCEL = Identifier.withDefaultNamespace("container/beacon/cancel");
	private static final int LABEL = 0xFFE0E0E0;
	/** Posición x de cada ítem de pago (mismo hueco que en vanilla), de nivel I a V. */
	private static final int[] PAYMENT_X = {20, 41, 63, 86, 108};

	public FullBeaconScreen(FullBeaconMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 230, 219);
	}

	private static BeaconData data() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player == null ? null : FullBeacon.held(mc.player).get(ModComponents.BEACON_DATA);
	}

	@Override
	protected void init() {
		super.init();
		// Izquierda: niveles 1-3 (2 poderes por fila). Derecha: nivel 4. Mismas coordenadas que vanilla.
		for (int i = 0; i < PowerCatalog.ENTRIES.size(); i++) {
			int tier = PowerCatalog.ENTRIES.get(i).requiredTier();
			int col = i % 2;
			int x = tier < 4 ? leftPos + 53 + col * 24 : leftPos + 144 + col * 24;
			int y = tier < 4 ? topPos + 22 + (tier - 1) * 25 : topPos + 47;
			addRenderableWidget(new PowerButton(x, y, i));
		}
		addRenderableWidget(new IconButton(leftPos + 164, topPos + 107, CONFIRM, true));
		addRenderableWidget(new IconButton(leftPos + 190, topPos + 107, CANCEL, false));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(g, mouseX, mouseY, partialTick);
		g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);
		for (int lvl = 1; lvl <= 5; lvl++) {
			g.item(new ItemStack(PowerCatalog.paymentItem(lvl)), leftPos + PAYMENT_X[lvl - 1], topPos + 109);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		BeaconData d = data();
		if (d == null) return;
		centered(g, Component.translatable("gui.beaconsreforged.points", d.usedPoints(), d.material().points), 62, 10);
		Component max = d.unlockedLevel() == 0 ? Component.literal("-") : Component.translatable("enchantment.level." + d.unlockedLevel());
		centered(g, Component.translatable("gui.beaconsreforged.max_level", max), 169, 10);
	}

	private void centered(GuiGraphicsExtractor g, Component text, int cx, int y) {
		g.text(font, text, cx - font.width(text) / 2, y, LABEL, true);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		if (data() == null) {
			onClose();
			return;
		}
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		// Tooltip de los materiales de pago
		BeaconData d = data();
		for (int lvl = 1; lvl <= 5; lvl++) {
			int x = leftPos + PAYMENT_X[lvl - 1], y = topPos + 109;
			if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
				Component roman = Component.translatable("enchantment.level." + lvl);
				Component item = new ItemStack(PowerCatalog.paymentItem(lvl)).getHoverName();
				MutableComponent tip = Component.translatable("gui.beaconsreforged.payment_info", item, roman);
				if (lvl > d.material().points) {
					tip.append("\n").append(Component.translatable("gui.beaconsreforged.unlock_too_high",
						Component.translatable(d.material().translationKey())).withStyle(ChatFormatting.RED));
				} else if (lvl <= d.unlockedLevel()) {
					tip.append("\n").append(Component.translatable("gui.beaconsreforged.unlock_done", roman).withStyle(ChatFormatting.GRAY));
				}
				g.setTooltipForNextFrame(font, font.split(tip, 200), mouseX, mouseY);
			}
		}
	}

	/** Botón de poder: icono + nivel en romano. */
	private class PowerButton extends AbstractWidget {
		private final int index;

		PowerButton(int x, int y, int index) {
			super(x, y, 22, 22, PowerCatalog.ENTRIES.get(index).effect().value().getDisplayName());
			this.index = index;
		}

		@Override
		protected boolean isValidClickButton(MouseButtonInfo info) {
			return info.button() == InputConstants.MOUSE_BUTTON_LEFT || info.button() == InputConstants.MOUSE_BUTTON_RIGHT;
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			BeaconData d = data();
			if (d == null) return;
			int delta = event.button() == InputConstants.MOUSE_BUTTON_RIGHT ? -1 : 1;
			int target = FullBeacon.levelOf(d, index) + delta;
			if (target >= 0 && FullBeacon.canSet(d, index, target)) {
				ClientPlayNetworking.send(new SetPowerPayload(index, target));
			}
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
			BeaconData d = data();
			if (d == null) return;
			PowerCatalog.Entry e = PowerCatalog.ENTRIES.get(index);
			boolean locked = d.tier() < e.requiredTier();
			int level = FullBeacon.levelOf(d, index);
			this.active = !locked;
			Identifier sprite = locked ? BUTTON_DISABLED : level > 0 ? BUTTON_SELECTED : isHovered() ? BUTTON_HIGHLIGHTED : BUTTON;
			g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), 22, 22);
			g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(e.effect()), getX() + 2, getY() + 2, 18, 18, locked ? 0.35f : 1f);
			if (level > 0) {
				Component roman = Component.translatable("enchantment.level." + level);
				g.text(font, roman, getX() + 21 - font.width(roman), getY() + 13, 0xFFFFFF55, true);
			}
			setTooltip(Tooltip.create(tooltip(d, e, level, locked)));
		}

		private Component tooltip(BeaconData d, PowerCatalog.Entry e, int level, boolean locked) {
			MutableComponent tip = Component.empty().append(e.effect().value().getDisplayName()).withStyle(ChatFormatting.WHITE);
			if (level > 0) tip.append(" ").append(Component.translatable("enchantment.level." + level));
			if (locked) {
				int size = e.requiredTier() * 2 + 1;
				return tip.append("\n").append(Component.translatable("gui.beaconsreforged.needs_pyramid", size, size).withStyle(ChatFormatting.RED));
			}
			tip.append("\n").append(Component.translatable("gui.beaconsreforged.click_help").withStyle(ChatFormatting.GRAY));
			if (!FullBeacon.canSet(d, index, level + 1)) {
				String reason = level >= e.maxLevel() ? "effect_cap" : level >= d.unlockedLevel() ? "needs_unlock" : "no_points";
				tip.append("\n").append(Component.translatable("gui.beaconsreforged." + reason).withStyle(ChatFormatting.GOLD));
			}
			return tip;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}
	}

	/** Botones ✔ (desbloquear con el pago) y ✖ (cerrar), con los sprites vanilla. */
	private class IconButton extends AbstractWidget {
		private final Identifier icon;
		private final boolean confirm;

		IconButton(int x, int y, Identifier icon, boolean confirm) {
			super(x, y, 22, 22, Component.empty());
			this.icon = icon;
			this.confirm = confirm;
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			if (confirm) ClientPlayNetworking.send(new UnlockLevelPayload(0));
			else onClose();
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
			if (confirm) {
				ItemStack pay = menu.paymentSlot.getItem();
				this.active = !pay.isEmpty() && menu.isValidPayment(pay);
				setTooltip(Tooltip.create(active
					? Component.translatable("gui.beaconsreforged.unlock_confirm",
						Component.translatable("enchantment.level." + PowerCatalog.paymentLevel(pay)))
					: Component.translatable("gui.beaconsreforged.unlock_hint")));
			}
			Identifier bg = !active ? BUTTON_DISABLED : isHovered() ? BUTTON_HIGHLIGHTED : BUTTON;
			g.blitSprite(RenderPipelines.GUI_TEXTURED, bg, getX(), getY(), 22, 22);
			g.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getX() + 2, getY() + 2, 18, 18);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}
	}
}
