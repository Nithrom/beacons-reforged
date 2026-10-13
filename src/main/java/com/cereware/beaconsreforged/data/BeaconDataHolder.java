package com.cereware.beaconsreforged.data;

import org.jspecify.annotations.Nullable;

/** Implementado por BeaconBlockEntity (vía mixin) para guardar los datos del beacon colocado. */
public interface BeaconDataHolder {
	@Nullable BeaconData beaconsreforged$getData();

	void beaconsreforged$setData(@Nullable BeaconData data);
}
