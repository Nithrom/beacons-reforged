package com.cereware.beaconsreforged.client;

import com.cereware.beaconsreforged.portable.BeaconSlotMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Pantalla del slot de beacon. Provisional: textura de la tolva tapando los 4 huecos sobrantes. */
public class BeaconSlotScreen extends AbstractContainerScreen<BeaconSlotMenu> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/hopper.png");

	public BeaconSlotScreen(BeaconSlotMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 133);
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(g, mouseX, mouseY, partialTick);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);
		for (int i : new int[] {0, 1, 3, 4}) {
			g.fill(x + 43 + i * 18, y + 19, x + 61 + i * 18, y + 37, 0xFFC6C6C6);
		}
	}
}
