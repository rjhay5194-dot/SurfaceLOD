package com.surfacelod.sample;

import com.surfacelod.config.LodConfig;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.world.level.ChunkPos;

public final class SurfaceLodStore {

    private static final Long2ObjectLinkedOpenHashMap<int[]> DATA =
            new Long2ObjectLinkedOpenHashMap<>();

    private SurfaceLodStore() {
    }

    public static void put(ChunkPos pos, int[] columns) {
        if (columns == null || columns.length != SurfaceSampler.COLUMNS) {
            return;
        }

        int[] copy = columns.clone();
        long key = pos.toLong();

        DATA.remove(key);
        DATA.putAndMoveToFirst(key, copy);

        trim();
    }

    public static int[] get(long key) {
        return DATA.getAndMoveToFirst(key);
    }

    public static int[] get(ChunkPos pos) {
        return get(pos.toLong());
    }

    public static int size() {
        return DATA.size();
    }

    public static void clear() {
        DATA.clear();
    }

    private static void trim() {
        long bytesPerChunk = 256L * Integer.BYTES + 64L;

        long maxChunks = Math.max(
                1L,
                (LodConfig.get().cacheLimitMb * 1024L * 1024L)
                        / bytesPerChunk
        );

        while (DATA.size() > maxChunks) {
            DATA.removeLast();
        }
    }
}
