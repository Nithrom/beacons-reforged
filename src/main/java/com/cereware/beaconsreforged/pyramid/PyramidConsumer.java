package com.cereware.beaconsreforged.pyramid;

import com.cereware.beaconsreforged.BeaconsReforged;
import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.data.BeaconData;
import com.cereware.beaconsreforged.data.BeaconDataHolder;
import com.cereware.beaconsreforged.data.PyramidMaterial;
import com.cereware.beaconsreforged.mixin.BeaconMenuAccessor;
import com.cereware.beaconsreforged.network.ConsumeStatusPayload;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Lógica de servidor para consumir la pirámide bajo un beacon. */
public final class PyramidConsumer {
	public static final int HIDDEN = 0, AVAILABLE = 1, MIXED = 2, NOT_BETTER = 3;

	private PyramidConsumer() {}

	public static int evaluate(PyramidScanner.Result scan, BeaconData current) {
		if (scan.tier() == 0) return HIDDEN;
		boolean full = ModConfig.isFull();
		if (full && scan.material() == PyramidMaterial.MIXED) return MIXED;
		if (current == null) return AVAILABLE;
		if (!full) return scan.tier() > current.tier() ? AVAILABLE : NOT_BETTER;
		boolean tierOk = scan.tier() >= current.tier();
		boolean matOk = scan.material().points >= current.material().points;
		boolean better = scan.tier() > current.tier() || scan.material().points > current.material().points;
		return tierOk && matOk && better ? AVAILABLE : NOT_BETTER;
	}

	/** Se llama al abrir el menú del beacon: avisa al cliente de si puede mostrar el botón. */
	public static void sendStatus(ServerPlayer player, int containerId, Level level, BlockPos pos, BeaconDataHolder holder) {
		PyramidScanner.Result scan = PyramidScanner.scan(level, pos);
		int state = evaluate(scan, holder.beaconsreforged$getData());
		ServerPlayNetworking.send(player, new ConsumeStatusPayload(containerId, state, scan.tier(), scan.material(), scan.blocks().size()));
	}

	/** Petición del cliente tras confirmar. Todo se revalida aquí. */
	public static void handleConsume(ServerPlayer player, int containerId) {
		if (!(player.containerMenu instanceof BeaconMenu menu) || menu.containerId != containerId || !menu.stillValid(player)) return;
		((BeaconMenuAccessor) menu).beaconsreforged$getAccess().execute((level, pos) -> {
			BlockEntity be = level.getBlockEntity(pos);
			if (!(be instanceof BeaconDataHolder holder)) return;
			PyramidScanner.Result scan = PyramidScanner.scan(level, pos);
			BeaconData current = holder.beaconsreforged$getData();
			if (evaluate(scan, current) != AVAILABLE) {
				player.sendSystemMessage(Component.translatable("message.beaconsreforged.consume_failed"));
				return;
			}
			BlockState replacement = consumedState();
			for (BlockPos p : scan.blocks()) level.setBlock(p, replacement, Block.UPDATE_ALL);
			holder.beaconsreforged$setData(new BeaconData(scan.tier(), scan.material(),
				current == null ? 0 : current.unlockedLevel(),
				current == null ? List.of() : current.powers()));
			be.setChanged();
			level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 0.6f);
			SoundType st = (replacement.isAir() ? Blocks.DIRT.defaultBlockState() : replacement).getSoundType();
			for (int layer = 1; layer <= scan.tier(); layer++) {
				level.playSound(null, pos.below(layer), st.getPlaceSound(), SoundSource.BLOCKS, 1.5f, 0.7f + layer * 0.1f);
			}
			level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
			int size = scan.tier() * 2 + 1;
			player.sendSystemMessage(Component.translatable("message.beaconsreforged.consumed", size, size,
				Component.translatable(scan.material().translationKey())));
			player.closeContainer();
		});
	}

	private static BlockState consumedState() {
		try {
			return BuiltInRegistries.BLOCK.getValue(Identifier.parse(ModConfig.get().consumedBlock)).defaultBlockState();
		} catch (Exception e) {
			BeaconsReforged.LOGGER.warn("consumedBlock inválido en la config, usando tierra");
			return Blocks.DIRT.defaultBlockState();
		}
	}
}
