package com.example.multirespawn.respawn;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class ValidationResult {
    private final boolean valid;
    private final String reason;
    private final ServerWorld world;
    private final Vec3d safePos;

    private ValidationResult(boolean valid, String reason, ServerWorld world, Vec3d safePos) {
        this.valid = valid;
        this.reason = reason;
        this.world = world;
        this.safePos = safePos;
    }

    public static ValidationResult valid(ServerWorld world, Vec3d safePos) {
        return new ValidationResult(true, "", world, safePos);
    }

    public static ValidationResult invalid(String reason) {
        return new ValidationResult(false, reason, null, null);
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    public Optional<ServerWorld> getWorld() {
        return Optional.ofNullable(world);
    }

    public Optional<Vec3d> getSafePos() {
        return Optional.ofNullable(safePos);
    }
}
