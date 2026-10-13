package com.cereware.beaconsreforged.registry;

import com.cereware.beaconsreforged.BeaconsReforged;
import com.cereware.beaconsreforged.data.BeaconData;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModComponents {
	public static final DataComponentType<BeaconData> BEACON_DATA = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		BeaconsReforged.id("beacon_data"),
		new DataComponentType.Builder<BeaconData>()
			.persistent(BeaconData.CODEC)
			.networkSynchronized(BeaconData.STREAM_CODEC)
			.cacheEncoding()
			.build());

	private ModComponents() {}

	public static void init() {
		ItemComponentTooltipProviderRegistry.addLast(BEACON_DATA);
	}
}
