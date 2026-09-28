package com.surfacelod.sample;

/**
 * Stores one terrain column in a single int.
 *
 * bits 0-11  : surface Y + 64
 * bits 12-27 : RGB565 colour
 * bits 28-31 : water depth
 */
public final class ColumnCodec {

    public static final int EMPTY = 0;
    public static final int Y_OFFSET = 64;

    private ColumnCodec() {
    }

    public static int pack(
            int surfaceY,
            int rgb888,
            int waterDepth
    ) {
        int y = Math.max(
                0,
                Math.min(
                        4095,
                        surfaceY + Y_OFFSET
                )
        );

        int r = (rgb888 >> 16) & 0xFF;
        int g = (rgb888 >> 8) & 0xFF;
        int b = rgb888 & 0xFF;

        int color =
                ((r >> 3) << 11)
                        | ((g >> 2) << 5)
                        | (b >> 3);

        if (color == 0) {
            color = 1;
        }

        int depth =
                Math.max(
                        0,
                        Math.min(15, waterDepth)
                );

        return y
                | (color << 12)
                | (depth << 28);
    }

    public static boolean isEmpty(int packed) {
        return packed == EMPTY;
    }

    public static int surfaceY(int packed) {
        return (packed & 0xFFF) - Y_OFFSET;
    }

    public static int waterDepth(int packed) {
        return packed >>> 28;
    }

    public static int rgb888(int packed) {
        int color =
                (packed >>> 12) & 0xFFFF;

        int r =
                ((color >> 11) & 31)
                        * 255 / 31;

        int g =
                ((color >> 5) & 63)
                        * 255 / 63;

        int b =
                (color & 31)
                        * 255 / 31;

        return (r << 16)
                | (g << 8)
                | b;
    }
}
