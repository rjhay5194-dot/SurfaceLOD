package com.surfacelod.sample;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SurfaceSampler {

    public static final int COLUMNS = 256;

    private static final int FALLBACK_RGB = 0x808080;

    private static final BlockPos.MutableBlockPos POS =
            new BlockPos.MutableBlockPos();

    private SurfaceSampler() {
    }

    /**
     * Samples the top visible block of every column
     * in an already-loaded chunk.
     *
     * Index = z * 16 + x.
     */
    public static void sampleChunk(
            LevelChunk chunk,
            int[] out
    ) {
        final int minY = chunk.getMinY();

        final int baseX =
                chunk.getPos().getMinBlockX();

        final int baseZ =
                chunk.getPos().getMinBlockZ();

        final BlockPos.MutableBlockPos pos = POS;

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {

                int index =
                        (z << 4) | x;

                int top =
                        chunk.getHeight(
                                Heightmap.Types.MOTION_BLOCKING,
                                x,
                                z
                        ) - 1;

                if (top < minY) {
                    out[index] =
                            ColumnCodec.EMPTY;
                    continue;
                }

               
