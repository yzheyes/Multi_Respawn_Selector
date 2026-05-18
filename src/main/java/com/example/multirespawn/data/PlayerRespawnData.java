package com.example.multirespawn.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerRespawnData {
    private final UUID playerUuid;
    private final List<RespawnPoint> points = new ArrayList<>();

    public PlayerRespawnData(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public List<RespawnPoint> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public RespawnPoint addOrUpdate(RespawnPoint point) {
        Optional<RespawnPoint> sameLocation = findByLocation(point.getDimensionId(), point.getPos());
        if (sameLocation.isPresent()) {
            sameLocation.get().updateFrom(point);
            return sameLocation.get();
        }

        points.add(point);
        return point;
    }

    public RespawnPoint addOrUpdateKeepingExistingName(RespawnPoint point) {
        Optional<RespawnPoint> sameLocation = findByLocation(point.getDimensionId(), point.getPos());
        if (sameLocation.isPresent()) {
            sameLocation.get().updateFromKeepingName(point);
            return sameLocation.get();
        }

        points.add(point);
        return point;
    }

    public Optional<RespawnPoint> findById(UUID id) {
        return points.stream().filter(point -> point.getId().equals(id)).findFirst();
    }

    public Optional<RespawnPoint> findByIdOrName(String idOrName) {
        try {
            return findById(UUID.fromString(idOrName));
        } catch (IllegalArgumentException ignored) {
            return points.stream().filter(point -> point.getName().equalsIgnoreCase(idOrName)).findFirst();
        }
    }

    public Optional<RespawnPoint> findByLocation(Identifier dimensionId, BlockPos pos) {
        return points.stream()
                .filter(point -> point.getDimensionId().equals(dimensionId) && point.getPos().equals(pos))
                .findFirst();
    }

    public boolean remove(UUID id) {
        return points.removeIf(point -> point.getId().equals(id));
    }

    public boolean removeByIdOrName(String idOrName) {
        try {
            UUID id = UUID.fromString(idOrName);
            return remove(id);
        } catch (IllegalArgumentException ignored) {
            return points.removeIf(point -> point.getName().equalsIgnoreCase(idOrName));
        }
    }

    public int removeInvalid() {
        int removed = 0;
        Iterator<RespawnPoint> iterator = points.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isValid()) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public void clear() {
        points.clear();
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("playerUuid", playerUuid);
        NbtList list = new NbtList();
        for (RespawnPoint point : points) {
            list.add(point.toNbt());
        }
        nbt.put("points", list);
        return nbt;
    }

    public static PlayerRespawnData fromNbt(NbtCompound nbt) {
        PlayerRespawnData data = new PlayerRespawnData(nbt.getUuid("playerUuid"));
        NbtList list = nbt.getList("points", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : list) {
            data.points.add(RespawnPoint.fromNbt((NbtCompound) element));
        }
        return data;
    }
}
