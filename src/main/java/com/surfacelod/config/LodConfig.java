package com.surfacelod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LodConfig {

    private static final Logger LOG =
            LoggerFactory.getLogger("surfacelod");

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("surfacelod.json");

    public static final int MIN_DISTANCE_CHUNKS = 12;
    public static final int MAX_DISTANCE_CHUNKS = 64;

    public static final int MIN_CACHE_MB = 16;
    public static final int MAX_CACHE_MB = 256;

    public boolean enabled = true;

    public LodQuality quality =
            LodQuality.BALANCED;

    public CpuMode cpuMode =
            CpuMode.LOW;

    public int lodDistanceChunks = 32;

    public boolean disableVanillaFog = false;

    public StructureMode structureMode =
            StructureMode.VISITED_ONLY;

    public RenderStyle renderStyle =
            RenderStyle.SHADED;

    public boolean renderWater = true;

    public int cacheLimitMb = 64;

    private static LodConfig instance =
            new LodConfig();

    private LodConfig() {
    }

    public static LodConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (Reader reader =
                         Files.newBufferedReader(FILE)) {

                LodConfig loaded =
                        GSON.fromJson(reader, LodConfig.class);

                if (loaded != null) {
                    instance = loaded;
                }

            } catch (Exception e) {
                LOG.warn(
                        "Could not read Surface
