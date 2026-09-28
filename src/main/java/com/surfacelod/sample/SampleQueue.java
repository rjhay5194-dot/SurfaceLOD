package com.surfacelod.sample;

import com.surfacelod.SurfaceLodClient;
import com.surfacelod.config.LodConfig;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class SampleQueue {

    private static final LongArrayFIFOQueue QUEUE =
            new LongArrayFIFOQueue();

    private static final LongOpenHashSet PENDING =
            new LongOpenHashSet();

    private static final int[] SCRATCH =
            new int[SurfaceSampler.COLUMNS];

    private static final int MAX_PENDING = 4096;

    private static long sampledTotal = 0;

    private SampleQueue() {
    }

    public static void onChunkLoad(LevelChunk chunk) {
        if (!LodConfig.get().enabled) {
            return;
        }

        long key = chunk.getPos().toLong();

        // Already cached.
        if (SurfaceLodStore.get(key) != null) {
            return;
        }

        if (PENDING.size() >= MAX_PENDING) {
            return;
        }

        if (PENDING.add(key)) {
            QUEUE.enqueue(key);
        }
    }

    public static void onChunkUnload(LevelChunk chunk) {
        if (!LodConfig.get().enabled) {
            return;
        }

        long key = chunk.getPos().toLong();

        if (PENDING.remove(key)) {
            process(chunk);
        }
    }

    public static void tick(Minecraft mc) {
        ClientLevel level = mc.level;

        if (level == null || QUEUE.isEmpty()) {
            return;
        }

        long budgetNs =
                LodConfig.get().cpuMode.mainThreadBudgetMicros * 1000L;

        long start = System.nanoTime();

        while (!QUEUE.isEmpty()
                && System.nanoTime() - start < budgetNs) {

            long key = QUEUE.dequeueLong();

            if (!PENDING.remove(key)) {
                continue;
            }

            ChunkPos cp = new ChunkPos(key);

            LevelChunk chunk =
                    level.getChunkSource().getChunk(
                            cp.x,
                            cp.z,
                            ChunkStatus.FULL,
                            false
                    );

            if (chunk != null) {
                process(chunk);
            }
        }
    }

    private static void process(LevelChunk chunk) {
        SurfaceSampler.sampleChunk(chunk, SCRATCH);

        // Store a copy because SCRATCH is reused for every chunk.
        SurfaceLodStore.put(chunk.getPos(), SCRATCH);

        sampledTotal++;

        if (sampledTotal == 1 || sampledTotal % 500 == 0) {
            int mid = SCRATCH[(8 << 4) | 8];

            SurfaceLodClient.LOG.info(
                    "Sampled {} chunks; centre column: y={} rgb={} water={} cache={}",
                    sampledTotal,
                    ColumnCodec.surfaceY(mid),
                    Integer.toHexString(ColumnCodec.rgb888(mid)),
                    ColumnCodec.waterDepth(mid),
                    SurfaceLodStore.size()
            );
        }
    }

    public static void clear() {
        QUEUE.clear();
        PENDING.clear();
        SurfaceLodStore.clear();
        sampledTotal = 0;
    }
}
