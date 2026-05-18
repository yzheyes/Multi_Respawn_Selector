package com.example.multirespawn.network;

import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.data.RespawnPointType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record RespawnPointView(UUID id, String name, Identifier dimensionId, BlockPos pos,
                               RespawnPointType type, boolean valid) {
    public static RespawnPointView fromPoint(RespawnPoint point) {
        return new RespawnPointView(point.getId(), point.getName(), point.getDimensionId(), point.getPos(),
                point.getType(), point.isValid());
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(id);
        buf.writeString(name);
        buf.writeIdentifier(dimensionId);
        buf.writeBlockPos(pos);
        buf.writeEnumConstant(type);
        buf.writeBoolean(valid);
    }

    public static RespawnPointView read(PacketByteBuf buf) {
        return new RespawnPointView(
                buf.readUuid(),
                buf.readString(32767),
                buf.readIdentifier(),
                buf.readBlockPos(),
                buf.readEnumConstant(RespawnPointType.class),
                buf.readBoolean()
        );
    }
}
