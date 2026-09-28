package com.surfacelod;

import com.surfacelod.config.LodConfig;
import com.surfacelod.render.SurfaceLodRenderer;
import com.surfacelod.sample.SampleQueue;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurfaceLodClient implements ClientModInitializer {
    public static final String MOD_ID = "surfacelod";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LodConfig.load();
        LodConfig c = LodConfig.get();

        LOG.info("Surface LOD loaded: enabled={} quality={} cpu={} distance={} chunks",
                c.enabled, c.quality, c.cpuMode, c.lodDistanceChunks);

        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> SampleQueue.onChunkLoad(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> SampleQueue.onChunkUnload(chunk));
        ClientTickEvents.END_CLIENT_TICK.register(SampleQueue::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> SampleQueue.clear());

        SurfaceLodRenderer.register();
        LOG.info("Surface LOD renderer registered");
    }
}
