package com.surfacelod.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.surfacelod.config.LodConfig;
import com.surfacelod.config.RenderStyle;
import com.surfacelod.sample.ColumnCodec;
import com.surfacelod.sample.SurfaceLodStore;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class SurfaceLodRenderer {

    private SurfaceLodRenderer() {
    }

    public static void register() {
        WorldRenderEvents.BEFORE_ENTITIES.register(SurfaceLodRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        LodConfig cfg = LodConfig.get();

        if (!cfg.enabled || mc.level == null || SurfaceLodStore.size() == 0) {
            return;
        }

        Camera camera = context.camera();
        Vec3 cam = camera.getPosition();

        int cameraChunkX = ChunkPos.blockToSectionCoord((int) Math.floor(cam.x));
        int cameraChunkZ = ChunkPos.blockToSectionCoord((int) Math.floor(cam.z));

        int vanillaDistance = Math.max(2, mc.options.getEffectiveRenderDistance());
        int maxDistance = Math.max(vanillaDistance + 1, cfg.lodDistanceChunks);

        MultiBufferSource consumers = context.consumers();
        if (consumers == null) {
            return;
        }

        VertexConsumer vertices = consumers.getBuffer(RenderTypes.debugQuads());

        int quadBudget = switch (cfg.quality) {
            case POTATO -> 12000;
            case LOW -> 18000;
            case BALANCED -> 26000;
            case HIGH -> 36000;
            case ULTRA -> 48000;
        };

        int quads = 0;

        for (int cz = cameraChunkZ - maxDistance;
             cz <= cameraChunkZ + maxDistance && quads < quadBudget;
             cz++) {

            for (int cx = cameraChunkX - maxDistance;
                 cx <= cameraChunkX + maxDistance && quads < quadBudget;
                 cx++) {

                int dx = cx - cameraChunkX;
                int dz = cz - cameraChunkZ;

                int distance = Math.max(Math.abs(dx), Math.abs(dz));

                if (distance <= vanillaDistance || distance > maxDistance) {
                    continue;
                }

                if (dx * dx + dz * dz > maxDistance * maxDistance) {
                    continue;
                }

                int[] data = SurfaceLodStore.get(ChunkPos.asLong(cx, cz));

                if (data == null) {
                    continue;
                }

                int step = cfg.quality.baseStep;

                if (distance > vanillaDistance + 12) {
                    step *= 2;
                }

                if (distance > vanillaDistance + 28) {
                    step *= 2;
                }

                step = Math.max(1, Math.min(16, step));

                for (int z = 0; z < 16 && quads < quadBudget; z += step) {
                    for (int x = 0; x < 16 && quads < quadBudget; x += step) {

                        int packed = data[(z << 4) | x];

                        if (ColumnCodec.isEmpty(packed)) {
                            continue;
                        }

                        int x2 = Math.min(16, x + step);
                        int z2 = Math.min(16, z + step);

                        int y = ColumnCodec.surfaceY(packed);
                        int rgb = ColumnCodec.rgb888(packed);

                        float shade = 1.0f;

                        if (cfg.renderStyle == RenderStyle.SHADED) {
                            int px = data[(z << 4) | Math.min(15, x2 - 1)];
                            int pz = data[(Math.min(15, z2 - 1) << 4) | x];

                            int dyX = ColumnCodec.isEmpty(px)
                                    ? 0
                                    : ColumnCodec.surfaceY(px) - y;

                            int dyZ = ColumnCodec.isEmpty(pz)
                                    ? 0
                                    : ColumnCodec.surfaceY(pz) - y;

                            shade = (float) (
                                    1.0 / Math.sqrt(
                                            1.0
                                                    + Math.min(8, Math.abs(dyX)) * 0.06
                                                    + Math.min(8, Math.abs(dyZ)) * 0.06
                                    )
                            );
                        }

                        int r = Math.max(0, Math.min(255,
                                (int) (((rgb >> 16) & 255) * shade)));

                        int g = Math.max(0, Math.min(255,
                                (int) (((rgb >> 8) & 255) * shade)));

                        int b = Math.max(0, Math.min(255,
                                (int) ((rgb & 255) * shade)));

                        double worldX = (cx << 4) + x;
                        double worldX2 = (cx << 4) + x2;
                        double worldZ = (cz << 4) + z;
                        double worldZ2 = (cz << 4) + z2;

                        float vx1 = (float) (worldX - cam.x);
                        float vx2 = (float) (worldX2 - cam.x);
                        float vz1 = (float) (worldZ - cam.z);
                        float vz2 = (float) (worldZ2 - cam.z);
                        float vy = (float) (y - cam.y);

                        vertices.addVertex(vx1, vy, vz1)
                                .setColor(r, g, b, 255);

                        vertices.addVertex(vx1, vy, vz2)
                                .setColor(r, g, b, 255);

                        vertices.addVertex(vx2, vy, vz2)
                                .setColor(r, g, b, 255);

                        vertices.addVertex(vx2, vy, vz1)
                                .setColor(r, g, b, 255);

                        quads++;
                    }
                }
            }
        }
    }
    }
