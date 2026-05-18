package com.example.multirespawn.respawn;

public final class SpawnPointCaptureGuard {
    private static final ThreadLocal<Boolean> SUPPRESSED = ThreadLocal.withInitial(() -> false);

    private SpawnPointCaptureGuard() {
    }

    public static boolean isSuppressed() {
        return SUPPRESSED.get();
    }

    public static void runSuppressed(Runnable runnable) {
        boolean previous = SUPPRESSED.get();
        SUPPRESSED.set(true);
        try {
            runnable.run();
        } finally {
            SUPPRESSED.set(previous);
        }
    }
}
