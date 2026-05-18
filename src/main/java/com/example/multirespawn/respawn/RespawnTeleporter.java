package com.example.multirespawn.respawn;

import com.example.multirespawn.data.PlayerRespawnData;
import com.example.multirespawn.data.RespawnDataStorage;
import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.network.ModPackets;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.UUID;

public final class RespawnTeleporter {
    private RespawnTeleporter() {
    }

    public static void applyPendingRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        Optional<PendingRespawnChoice> pending = PendingRespawnChoiceManager.consume(oldPlayer.getUuid());
        if (pending.isEmpty() || pending.get().getPointId().isEmpty()) {
            return;
        }

        UUID pointId = pending.get().getPointId().get();
        RespawnDataStorage storage = RespawnDataStorage.get(newPlayer.getServer());
        PlayerRespawnData data = storage.getPlayerData(newPlayer.getUuid());
        RespawnPoint point = data.findById(pointId).orElse(null);
        if (point == null) {
            ModPackets.sendError(newPlayer, "Selected respawn point no longer exists. Used vanilla respawn instead.");
            return;
        }

        ValidationResult result = RespawnValidator.validate(newPlayer, point);
        if (!result.isValid()) {
            data.remove(point.getId());
            storage.markDirty();
            ModPackets.sendError(newPlayer, result.getReason() + " Used vanilla respawn instead.");
            return;
        }

        ServerWorld world = result.getWorld().orElseThrow();
        Vec3d safePos = result.getSafePos().orElseThrow();
        newPlayer.teleport(world, safePos.x, safePos.y, safePos.z, point.getYaw(), point.getPitch());

        ModPackets.sendSyncPoints(newPlayer, RespawnSelectionService.refreshValidPoints(newPlayer));
    }
}
