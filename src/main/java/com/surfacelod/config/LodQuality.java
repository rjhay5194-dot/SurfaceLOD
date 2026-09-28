package com.surfacelod.config;

public enum LodQuality {
    POTATO(16),
    LOW(8),
    BALANCED(4),
    HIGH(2),
    ULTRA(1);

    public final int baseStep;

    LodQuality(int baseStep) {
        this.baseStep = baseStep;
    }
}
