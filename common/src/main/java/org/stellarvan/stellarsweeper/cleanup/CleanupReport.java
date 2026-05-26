package org.stellarvan.stellarsweeper.cleanup;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class CleanupReport {
    private final CleanupCause cause;
    private boolean skipped;
    private String skipReason = "";
    private int totalCount;
    private int entityCount;
    private final Map<String, CleanupStats> statsByItemId = new LinkedHashMap<>();
    private final Set<String> skippedWorlds = new LinkedHashSet<>();

    public CleanupReport(CleanupCause cause) {
        this.cause = cause;
    }

    public CleanupCause cause() {
        return cause;
    }

    public boolean skipped() {
        return skipped;
    }

    public String skipReason() {
        return skipReason;
    }

    public int totalCount() {
        return totalCount;
    }

    public int entityCount() {
        return entityCount;
    }

    public Map<String, CleanupStats> statsByItemId() {
        return Collections.unmodifiableMap(statsByItemId);
    }

    public Set<String> skippedWorlds() {
        return Collections.unmodifiableSet(skippedWorlds);
    }

    public void setSkipped(String reason) {
        this.skipped = true;
        this.skipReason = reason;
    }

    public void addSkippedWorld(String worldId) {
        skippedWorlds.add(worldId);
    }

    public void addItem(String itemId, String displayName, int stackCount) {
        totalCount += stackCount;
        entityCount += 1;
        CleanupStats stats = statsByItemId.computeIfAbsent(itemId, key -> new CleanupStats(itemId, displayName));
        stats.add(stackCount);
    }
}
