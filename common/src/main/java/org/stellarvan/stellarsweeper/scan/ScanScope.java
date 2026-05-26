package org.stellarvan.stellarsweeper.scan;

import org.stellarvan.stellarsweeper.config.SweeperConfig;

public final class ScanScope {
    private final int cleanRadius;
    private final int yMin;
    private final int yMax;

    public ScanScope(int cleanRadius, int yMin, int yMax) {
        this.cleanRadius = cleanRadius;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public static ScanScope fromConfig(SweeperConfig config) {
        return new ScanScope(config.cleanRadius, config.yMin, config.yMax);
    }

    public int cleanRadius() {
        return cleanRadius;
    }

    public int yMin() {
        return yMin;
    }

    public int yMax() {
        return yMax;
    }
}
