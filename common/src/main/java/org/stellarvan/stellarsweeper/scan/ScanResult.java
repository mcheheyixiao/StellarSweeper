package org.stellarvan.stellarsweeper.scan;

import java.util.List;

public final class ScanResult {
    private final List<DroppedItemSnapshot> candidates;
    private final boolean hasOnlinePlayers;

    public ScanResult(List<DroppedItemSnapshot> candidates, boolean hasOnlinePlayers) {
        this.candidates = candidates;
        this.hasOnlinePlayers = hasOnlinePlayers;
    }

    public List<DroppedItemSnapshot> candidates() {
        return candidates;
    }

    public boolean hasOnlinePlayers() {
        return hasOnlinePlayers;
    }
}
