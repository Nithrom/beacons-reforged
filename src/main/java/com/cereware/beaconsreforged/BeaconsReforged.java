package com.cereware.beaconsreforged;

import com.cereware.beaconsreforged.config.ModConfig;
import com.cereware.beaconsreforged.network.ModNetworking;
import com.cereware.beaconsreforged.effect.ModEffects;
import com.cereware.beaconsreforged.registry.ModComponents;
import com.cereware.beaconsreforged.registry.ModMenus;
import com.cereware.beaconsreforged.portable.PortableBeacon;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeaconsReforged implements ModInitializer {
	public static final String MOD_ID = "beaconsreforged";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.load();
		ModEffects.init();
		ModComponents.init();
		ModMenus.init();
		ModNetworking.init();
		PortableBeacon.init();
		LOGGER.info("Beacons Reforged cargado");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
