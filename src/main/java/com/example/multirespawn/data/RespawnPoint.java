package com.example.multirespawn.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class RespawnPoint {
    private final UUID id;
    private String name;
    private Identifier dimensionId;
    private BlockPos pos;
    private float yaw;
    private float pitch;
    private RespawnPointType type;
    private long updatedAt;
    private boolean valid;

    public RespawnPoint(UUID id, String name, Identifier dimensionId, BlockPos pos, float yaw, float pitch,
                        RespawnPointType type, long updatedAt, boolean valid) {
        this.id = id;
        this.name = name;
        this.dimensionId = dimensionId;
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
        this.type = type;
        this.updatedAt = updatedAt;
        this.valid = valid;
    }

    public static RespawnPoint create(String name, Identifier dimensionId, BlockPos pos, float yaw, float pitch,
                                      RespawnPointType type) {
        return new RespawnPoint(UUID.randomUUID(), name, dimensionId, pos, yaw, pitch, type,
                System.currentTimeMillis(), true);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Identifier getDimensionId() {
        return dimensionId;
    }

    public BlockPos getPos() {
        return pos;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public RespawnPointType getType() {
        return type;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isValid() {
        return valid;
    }

    public void rename(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateFrom(RespawnPoint other) {
        this.name = other.name;
        this.dimensionId = other.dimensionId;
        this.pos = other.pos;
        this.yaw = other.yaw;
        this.pitch = other.pitch;
        this.type = other.type;
        this.updatedAt = other.updatedAt;
        this.valid = other.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("id", id);
        nbt.putString("name", name);
        nbt.putString("dimension", dimensionId.toString());
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        nbt.putFloat("yaw", yaw);
        nbt.putFloat("pitch", pitch);
        nbt.putString("type", type.name());
        nbt.putLong("updatedAt", updatedAt);
        nbt.putBoolean("valid", valid);
        return nbt;
    }

    public static RespawnPoint fromNbt(NbtCompound nbt) {
        Identifier dimension = Identifier.tryParse(nbt.getString("dimension"));
        if (dimension == null) {
            dimension = new Identifier("minecraft", "overworld");
        }

        RespawnPointType type;
        try {
            type = RespawnPointType.valueOf(nbt.getString("type"));
        } catch (IllegalArgumentException ignored) {
            type = RespawnPointType.CUSTOM;
        }

        return new RespawnPoint(
                nbt.getUuid("id"),
                nbt.getString("name"),
                dimension,
                new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")),
                nbt.getFloat("yaw"),
                nbt.getFloat("pitch"),
                type,
                nbt.getLong("updatedAt"),
                !nbt.contains("valid") || nbt.getBoolean("valid")
        );
    }
}
