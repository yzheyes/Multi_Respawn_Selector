package com.example.multirespawn.respawn;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PendingRespawnChoiceManager {
    private static final Map<UUID, PendingRespawnChoice> PENDING = new ConcurrentHashMap<>();

    private PendingRespawnChoiceManager() {
    }

    public static void setPoint(UUID playerUuid, UUID pointId) {
        PENDING.put(playerUuid, PendingRespawnChoice.point(pointId));
    }

    public static void setVanilla(UUID playerUuid) {
        PENDING.put(playerUuid, PendingRespawnChoice.vanilla());
    }

    public static Optional<PendingRespawnChoice> get(UUID playerUuid) {
        PendingRespawnChoice choice = PENDING.get(playerUuid);
        if (choice != null && choice.isExpired()) {
            PENDING.remove(playerUuid);
            return Optional.empty();
        }
        return Optional.ofNullable(choice);
    }

    public static Optional<PendingRespawnChoice> consume(UUID playerUuid) {
        PendingRespawnChoice choice = PENDING.remove(playerUuid);
        if (choice == null || choice.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(choice);
    }

    public static void clear(UUID playerUuid) {
        PENDING.remove(playerUuid);
    }

    public static void clearAll() {
        PENDING.clear();
    }
}
