package com.example.multirespawn.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RespawnDataStorage extends PersistentState {
    private static final String STORAGE_ID = "multirespawn_points";

    private final Map<UUID, PlayerRespawnData> players = new HashMap<>();

    public static RespawnDataStorage get(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(RespawnDataStorage::fromNbt, RespawnDataStorage::new, STORAGE_ID);
    }

    public PlayerRespawnData getPlayerData(UUID playerUuid) {
        return players.computeIfAbsent(playerUuid, PlayerRespawnData::new);
    }

    public Collection<PlayerRespawnData> getAllPlayers() {
        return players.values();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (PlayerRespawnData data : players.values()) {
            list.add(data.toNbt());
        }
        nbt.put("players", list);
        return nbt;
    }

    public static RespawnDataStorage fromNbt(NbtCompound nbt) {
        RespawnDataStorage storage = new RespawnDataStorage();
        NbtList list = nbt.getList("players", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : list) {
            PlayerRespawnData data = PlayerRespawnData.fromNbt((NbtCompound) element);
            storage.players.put(data.getPlayerUuid(), data);
        }
        return storage;
    }
}
