package org.stellarvan.stellarsweeper.cleanup;

public final class CleanupStats {
    private final String itemId;
    private final String displayName;
    private int count;
    private int entityCount;

    public CleanupStats(String itemId, String displayName) {
        this.itemId = itemId;
        this.displayName = displayName;
    }

    public void add(int stackCount) {
        count += stackCount;
        entityCount += 1;
    }

    public String itemId() {
        return itemId;
    }

    public String displayName() {
        return displayName;
    }

    public int count() {
        return count;
    }

    public int entityCount() {
        return entityCount;
    }
}
