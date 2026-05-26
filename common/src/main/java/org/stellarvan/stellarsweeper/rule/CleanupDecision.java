package org.stellarvan.stellarsweeper.rule;

public final class CleanupDecision {
    private static final CleanupDecision CLEAN = new CleanupDecision(true, "matched");
    private static final CleanupDecision SKIP = new CleanupDecision(false, "not_matched");
    private final boolean shouldClean;
    private final String reason;

    private CleanupDecision(boolean shouldClean, String reason) {
        this.shouldClean = shouldClean;
        this.reason = reason;
    }

    public static CleanupDecision clean() {
        return CLEAN;
    }

    public static CleanupDecision skip() {
        return SKIP;
    }

    public boolean shouldClean() {
        return shouldClean;
    }

    public String reason() {
        return reason;
    }
}
