package com.cereware.beaconsreforged.mixin;

import com.cereware.beaconsreforged.portable.PortableBeacon;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shift-clic sobre un beacon portable en el inventario (E): lo equipa e intercambia con el equipado. */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void beaconsreforged$equipBeacon(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		if (index < 9 || index > 45) return; // solo inventario, barra rápida y mano secundaria
		Slot slot = ((InventoryMenu) (Object) this).slots.get(index);
		ItemStack stack = slot.getItem();
		if (!PortableBeacon.isPortableBeacon(stack) || stack.getCount() != 1) return;
		cir.setReturnValue(ItemStack.EMPTY);
		if (!(player instanceof ServerPlayer sp)) return; // el cliente espera a que el servidor sincronice
		ItemStack previous = PortableBeacon.getEquipped(sp);
		PortableBeacon.setEquipped(sp, stack.copy());
		slot.set(previous);
		sp.level().playSound(null, sp.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
		sp.sendOverlayMessage(Component.translatable("message.beaconsreforged.equipped", stack.getHoverName()));
	}
}
