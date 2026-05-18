package com.example.multirespawn.respawn;

import com.example.multirespawn.data.PlayerRespawnData;
import com.example.multirespawn.data.RespawnDataStorage;
import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.network.ModPackets;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RespawnSelectionService {
    public static final UUID WORLD_SPAWN_ID = new UUID(0L, 0L);

    private RespawnSelectionService() {
    }

    public static void handleRequest(ServerPlayerEntity player) {
        List<RespawnPoint> validPoints = refreshValidPoints(player);
        if (validPoints.size() == 1) {
            RespawnPoint point = validPoints.get(0);
            VanillaRespawnBridge.alignVanillaSpawnPoint(player, point);
            PendingRespawnChoiceManager.setPoint(player.getUuid(), point.getId());
            ModPackets.sendTriggerRespawn(player);
            return;
        }

        ModPackets.sendOpenChoiceScreen(player, validPoints);
    }

    public static void handleChoice(ServerPlayerEntity player, UUID pointId) {
        if (WORLD_SPAWN_ID.equals(pointId)) {
            PendingRespawnChoiceManager.setVanilla(player.getUuid());
            ModPackets.sendTriggerRespawn(player);
            return;
        }

        RespawnDataStorage storage = RespawnDataStorage.get(player.getServer());
        PlayerRespawnData data = storage.getPlayerData(player.getUuid());
        RespawnPoint point = data.findById(pointId).orElse(null);
        if (point == null) {
            ModPackets.sendError(player, "That respawn point no longer exists.");
            ModPackets.sendOpenChoiceScreen(player, refreshValidPoints(player));
            return;
        }

        ValidationResult result = RespawnValidator.validate(player, point);
        if (!result.isValid()) {
            data.remove(point.getId());
            storage.markDirty();
            ModPackets.sendError(player, result.getReason());
            ModPackets.sendOpenChoiceScreen(player, refreshValidPoints(player));
            return;
        }

        VanillaRespawnBridge.alignVanillaSpawnPoint(player, point);
        PendingRespawnChoiceManager.setPoint(player.getUuid(), point.getId());
        ModPackets.sendTriggerRespawn(player);
    }

    public static boolean shouldBlockVanillaRespawn(ServerPlayerEntity player) {
        if (PendingRespawnChoiceManager.get(player.getUuid()).isPresent()) {
            return false;
        }

        List<RespawnPoint> validPoints = refreshValidPoints(player);
        if (validPoints.size() > 1) {
            ModPackets.sendOpenChoiceScreen(player, validPoints);
            return true;
        }

        if (validPoints.size() == 1) {
            RespawnPoint point = validPoints.get(0);
            VanillaRespawnBridge.alignVanillaSpawnPoint(player, point);
            PendingRespawnChoiceManager.setPoint(player.getUuid(), point.getId());
        }

        return false;
    }

    public static List<RespawnPoint> refreshValidPoints(ServerPlayerEntity player) {
        RespawnDataStorage storage = RespawnDataStorage.get(player.getServer());
        PlayerRespawnData data = storage.getPlayerData(player.getUuid());
        List<RespawnPoint> validPoints = new ArrayList<>();
        boolean changed = false;

        for (RespawnPoint point : data.getPoints()) {
            ValidationResult result = RespawnValidator.validate(player, point);
            if (result.isValid()) {
                point.setValid(true);
                validPoints.add(point);
            } else {
                point.setValid(false);
                changed = true;
            }
        }

        if (data.removeInvalid() > 0 || changed) {
            storage.markDirty();
        }

        return validPoints;
    }
}
