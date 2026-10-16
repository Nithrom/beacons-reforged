package com.cereware.beaconsreforged.registry;

import com.cereware.beaconsreforged.BeaconsReforged;
import com.cereware.beaconsreforged.full.FullBeaconMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
	public static final MenuType<FullBeaconMenu> FULL_BEACON = Registry.register(BuiltInRegistries.MENU,
		BeaconsReforged.id("full_beacon"), new MenuType<>(FullBeaconMenu::new, FeatureFlags.VANILLA_SET));

	private ModMenus() {}

	public static void init() {}
}
