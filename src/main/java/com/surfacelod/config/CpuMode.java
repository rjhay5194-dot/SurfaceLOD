package com.surfacelod.config;

public enum CpuMode {
    LOW(500),
    MEDIUM(1000),
    HIGH(2000);

    public final int mainThreadBudgetMicros;

    CpuMode(int mainThreadBudgetMicros) {
        this.mainThreadBudgetMicros = mainThreadBudgetMicros;
    }
}
