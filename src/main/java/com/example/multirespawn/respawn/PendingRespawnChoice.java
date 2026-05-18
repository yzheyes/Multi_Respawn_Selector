package com.example.multirespawn.respawn;

import java.util.Optional;
import java.util.UUID;

public class PendingRespawnChoice {
    private final UUID pointId;
    private final long createdAt;

    private PendingRespawnChoice(UUID pointId) {
        this.pointId = pointId;
        this.createdAt = System.currentTimeMillis();
    }

    public static PendingRespawnChoice point(UUID pointId) {
        return new PendingRespawnChoice(pointId);
    }

    public static PendingRespawnChoice vanilla() {
        return new PendingRespawnChoice(null);
    }

    public Optional<UUID> getPointId() {
        return Optional.ofNullable(pointId);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > 30_000L;
    }
}
