package org.stellarvan.stellarsweeper.cleanup;

public final class CleanupRequest {
    private final long requestId;
    private final long createdTick;
    private final long expireTick;

    public CleanupRequest(long requestId, long createdTick, long expireTick) {
        this.requestId = requestId;
        this.createdTick = createdTick;
        this.expireTick = expireTick;
    }

    public long requestId() {
        return requestId;
    }

    public long createdTick() {
        return createdTick;
    }

    public long expireTick() {
        return expireTick;
    }

    public boolean expired(long nowTick) {
        return nowTick > expireTick;
    }
}
