package com.cereware.beaconsreforged.mixin;

import com.cereware.beaconsreforged.data.BeaconData;
import com.cereware.beaconsreforged.data.BeaconDataHolder;
import com.cereware.beaconsreforged.registry.ModComponents;
import com.cereware.beaconsreforged.pyramid.PyramidConsumer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import com.cereware.beaconsreforged.data.PyramidMaterial;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Conserva BeaconData al colocar/romper el beacon y al guardar/cargar el mundo. */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin implements BeaconDataHolder {
	@Unique
	private static final String KEY = "beaconsreforged:beacon_data";

	@Unique
	private @Nullable BeaconData beaconsreforged$data;

	@Override
	public @Nullable BeaconData beaconsreforged$getData() {
		return beaconsreforged$data;
	}

	@Override
	public void beaconsreforged$setData(@Nullable BeaconData data) {
		beaconsreforged$data = data;
	}

	@Inject(method = "applyImplicitComponents", at = @At("TAIL"))
	private void beaconsreforged$apply(DataComponentGetter components, CallbackInfo ci) {
		beaconsreforged$data = components.get(ModComponents.BEACON_DATA);
	}

	@Inject(method = "collectImplicitComponents", at = @At("TAIL"))
	private void beaconsreforged$collect(DataComponentMap.Builder builder, CallbackInfo ci) {
		if (beaconsreforged$data != null) {
			builder.set(ModComponents.BEACON_DATA, beaconsreforged$data);
			builder.set(DataComponents.MAX_STACK_SIZE, 1);
			Level level = ((BlockEntity) (Object) this).getLevel();
			if (beaconsreforged$data.material() == PyramidMaterial.NETHERITE && level != null) {
				builder.set(DataComponents.DAMAGE_RESISTANT, new DamageResistant(
					level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_FIRE)));
			}
		}
	}

	@Inject(method = "createMenu", at = @At("RETURN"))
	private void beaconsreforged$sendStatus(int containerId, Inventory inventory, Player player, CallbackInfoReturnable<AbstractContainerMenu> cir) {
		BlockEntity self = (BlockEntity) (Object) this;
		if (player instanceof ServerPlayer sp && self.getLevel() != null) {
			PyramidConsumer.sendStatus(sp, containerId, self.getLevel(), self.getBlockPos(), this);
		}
	}

	@Inject(method = "removeComponentsFromTag", at = @At("TAIL"))
	private void beaconsreforged$removeFromTag(ValueOutput output, CallbackInfo ci) {
		output.discard(KEY);
	}

	@Inject(method = "saveAdditional", at = @At("TAIL"))
	private void beaconsreforged$save(ValueOutput output, CallbackInfo ci) {
		output.storeNullable(KEY, BeaconData.CODEC, beaconsreforged$data);
	}

	@Inject(method = "loadAdditional", at = @At("TAIL"))
	private void beaconsreforged$load(ValueInput input, CallbackInfo ci) {
		beaconsreforged$data = input.read(KEY, BeaconData.CODEC).orElse(null);
	}
}
