package com.cereware.beaconsreforged.pyramid;

import com.cereware.beaconsreforged.data.PyramidMaterial;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Detecta la pirámide bajo un beacon: nivel (capas completas), material y bloques que la forman. */
public final class PyramidScanner {
	public record Result(int tier, PyramidMaterial material, List<BlockPos> blocks) {}

	private PyramidScanner() {}

	public static Result scan(Level level, BlockPos beacon) {
		List<BlockPos> blocks = new ArrayList<>();
		PyramidMaterial material = null;
		boolean mixed = false;
		int tier = 0;
		for (int layer = 1; layer <= 4; layer++) {
			int y = beacon.getY() - layer;
			List<BlockPos> layerBlocks = new ArrayList<>();
			boolean complete = true;
			for (int x = -layer; x <= layer && complete; x++) {
				for (int z = -layer; z <= layer; z++) {
					BlockPos p = new BlockPos(beacon.getX() + x, y, beacon.getZ() + z);
					BlockState state = level.getBlockState(p);
					if (!state.is(BlockTags.BEACON_BASE_BLOCKS)) {
						complete = false;
						break;
					}
					layerBlocks.add(p);
				}
			}
			if (!complete) break;
			for (BlockPos p : layerBlocks) {
				PyramidMaterial m = materialOf(level.getBlockState(p).getBlock());
				if (m == null || (material != null && material != m)) mixed = true;
				if (material == null) material = m;
			}
			blocks.addAll(layerBlocks);
			tier = layer;
		}
		PyramidMaterial result = tier == 0 ? PyramidMaterial.MIXED : (mixed || material == null ? PyramidMaterial.MIXED : material);
		return new Result(tier, result, blocks);
	}

	/** null si es un bloque de base añadido por otro mod (cuenta como mezcla). */
	private static PyramidMaterial materialOf(Block block) {
		if (block == Blocks.IRON_BLOCK) return PyramidMaterial.IRON;
		if (block == Blocks.GOLD_BLOCK) return PyramidMaterial.GOLD;
		if (block == Blocks.EMERALD_BLOCK) return PyramidMaterial.EMERALD;
		if (block == Blocks.DIAMOND_BLOCK) return PyramidMaterial.DIAMOND;
		if (block == Blocks.NETHERITE_BLOCK) return PyramidMaterial.NETHERITE;
		return null;
	}
}
