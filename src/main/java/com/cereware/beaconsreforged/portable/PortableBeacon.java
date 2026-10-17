package com.cereware.beaconsreforged.portable;

import com.cereware.beaconsreforged.BeaconsReforged;
import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.registry.ModComponents;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import com.cereware.slots.api.CerewareSlotsApi;
import com.cereware.slots.api.SlotDefinition;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import com.cereware.beaconsreforged.full.FullBeaconMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Hueco del beacon (librería CereWare Slots, tecla C) y apertura de menús. */
public final class PortableBeacon {
	/** Identificador del hueco en CereWare Slots. */
	public static final Identifier SLOT = BeaconsReforged.id("beacon");

	/** Hueco antiguo (1.0.0). Solo se usa para migrar al hueco de CereWare Slots al entrar. */
	@Deprecated
	public static final AttachmentType<ItemStack> LEGACY_EQUIPPED = AttachmentRegistry.create(BeaconsReforged.id("equipped_beacon"),
		b -> b.persistent(ItemStack.OPTIONAL_CODEC).copyOnDeath());

	private PortableBeacon() {}

	public static boolean isPortableBeacon(ItemStack stack) {
		return stack.is(Items.BEACON) && stack.has(ModComponents.BEACON_DATA);
	}

	public static ItemStack getEquipped(Player player) {
		return CerewareSlotsApi.getStack(player, SLOT);
	}

	public static void setEquipped(Player player, ItemStack stack) {
		CerewareSlotsApi.setStack(player, SLOT, stack);
	}

	/** Menú de selección de poderes del beacon que se tiene en la mano (reutiliza el menú vanilla). */
	public static void openPowersMenu(ServerPlayer player, InteractionHand hand, ItemStack stack) {
		player.openMenu(new SimpleMenuProvider((id, inv, p) ->
			new BeaconMenu(id, inv, new PortableBeaconData(p, hand, stack), ContainerLevelAccess.NULL), stack.getHoverName()));
	}

	/** Agacharse + clic derecho al aire: el beacon de la mano pasa al slot y el equipado pasa a la mano. */
	public static void swapWithHand(ServerPlayer player, InteractionHand hand, ItemStack held) {
		if (held.getCount() != 1) return;
		ItemStack previous = getEquipped(player);
		setEquipped(player, held.copy());
		player.setItemInHand(hand, previous);
		player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC.value(),
			net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.2f);
		player.sendOverlayMessage(Component.translatable("message.beaconsreforged.equipped", held.getHoverName()));
	}

	public static void init() {
		CerewareSlotsApi.register(new SlotDefinition(SLOT, Component.translatable("slot.beaconsreforged.beacon"),
			Items.BEACON, PortableBeacon::isPortableBeacon, 1, 10, () -> ModConfig.get().keepBeaconOnDeath));
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!isPortableBeacon(stack)) return InteractionResult.PASS;
			if (player.isShiftKeyDown()) {
				if (player instanceof ServerPlayer sp) swapWithHand(sp, hand, stack);
				return InteractionResult.SUCCESS;
			}
			if (player instanceof ServerPlayer sp) {
				var data = stack.get(ModComponents.BEACON_DATA);
				var clamped = data.clampToConfig();
				if (clamped != data) stack.set(ModComponents.BEACON_DATA, clamped);
				if (ModConfig.isFull()) sp.openMenu(new SimpleMenuProvider((id, inv, pl) -> new FullBeaconMenu(id, inv), stack.getHoverName()));
				else openPowersMenu(sp, hand, stack);
			}
			return InteractionResult.SUCCESS;
		});
		// Migración desde 1.0.0: mover el beacon del hueco antiguo al de CereWare Slots
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer p = handler.getPlayer();
			ItemStack legacy = p.getAttached(LEGACY_EQUIPPED);
			if (legacy == null) return;
			p.removeAttached(LEGACY_EQUIPPED);
			if (getEquipped(p).isEmpty()) setEquipped(p, legacy);
			else if (!p.getInventory().add(legacy)) p.spawnAtLocation(p.level(), legacy);
		});
		BeaconEffectTicker.init();
	}
}
