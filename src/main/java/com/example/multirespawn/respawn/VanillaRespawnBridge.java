package com.example.multirespawn.respawn;

import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.data.RespawnPointType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public final class VanillaRespawnBridge {
    private VanillaRespawnBridge() {
    }

    public static void alignVanillaSpawnPoint(ServerPlayerEntity player, RespawnPoint point) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, point.getDimensionId());
        boolean forced = point.getType() == RespawnPointType.COMMAND || point.getType() == RespawnPointType.CUSTOM;

        SpawnPointCaptureGuard.runSuppressed(() ->
                player.setSpawnPoint(worldKey, point.getPos(), point.getYaw(), forced, false));
    }
}
