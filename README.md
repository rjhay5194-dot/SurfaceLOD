# Surface LOD

Phone-focused distant terrain LOD for Minecraft Fabric 1.21.11.

## Design goal

Render terrain outside vanilla render distance without loading complete distant
chunks into RAM. Distant terrain should eventually be represented by a compact
surface-only dataset and low-complexity GPU mesh.

### Core rules

- Never keep complete distant chunks just for LOD rendering.
- No entities, block entities, inventories, caves or redstone in the LOD.
- Generate/update LOD data off the render thread.
- Use bounded caches so RAM use cannot grow forever.
- Use small work budgets controlled by CPU Usage.
- Replace LOD with vanilla chunks smoothly when the player approaches.
- Provide five quality levels and five CPU-usage levels.
- Optional vanilla fog disable.
- LOD can be toggled with F8.

## Current version: 0.1.0-alpha

This is the architecture/bootstrap phase. It intentionally does NOT claim to
render distant terrain yet. The next implementation stage is the surface
sampler.

## Planned pipeline

1. Surface sampler
   - Obtain only the top surface information needed for a distant cell.
   - Avoid creating normal client chunks for every LOD cell.
2. Compression
   - Store height/material information in compact primitive arrays.
3. Background queue
   - CPU budget limits how many cells are processed per tick.
4. LOD mesh builder
   - Merge many surface cells into very small meshes.
5. GPU renderer
   - Upload persistent buffers and avoid per-frame allocations.
6. Transition system
   - Keep LOD and vanilla rendering together briefly, then replace LOD.
7. Fog
   - Remove vanilla distance fog when the setting is enabled, while retaining
     normal atmospheric effects where appropriate.

## Quality levels

1. Potato
2. Low
3. Balanced
4. High
5. Ultra

## CPU levels

1. Minimal
2. Low
3. Balanced
4. Fast
5. Maximum

## Build

Use Java 21 and a current Gradle installation or the Gradle wrapper.

`./gradlew build`

The finished jar will be in `build/libs/`.

## Important

The difficult part is the distant surface sampler. Do not solve it by simply
loading full client chunks at a huge render distance; that defeats the purpose
of this project.
