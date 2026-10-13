package com.cereware.beaconsreforged.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int beaconsreforged$getLeftPos();

	@Accessor("topPos")
	int beaconsreforged$getTopPos();

	@Accessor("imageWidth")
	int beaconsreforged$getImageWidth();
}
