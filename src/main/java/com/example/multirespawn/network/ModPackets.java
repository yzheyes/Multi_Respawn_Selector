package com.example.multirespawn.network;

import com.example.multirespawn.MultiRespawnMod;
import com.example.multirespawn.data.RespawnDataStorage;
import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.respawn.RespawnSelectionService;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

public final class ModPackets {
    public static final Identifier REQUEST_POINTS = MultiRespawnMod.id("request_points");
    public static final Identifier CHOOSE_POINT = MultiRespawnMod.id("choose_point");
    public static final Identifier DELETE_POINT = MultiRespawnMod.id("delete_point");
    public static final Identifier OPEN_CHOICE_SCREEN = MultiRespawnMod.id("open_choice_screen");
    public static final Identifier SYNC_POINTS = MultiRespawnMod.id("sync_points");
    public static final Identifier ERROR_MESSAGE = MultiRespawnMod.id("error_message");
    public static final Identifier TRIGGER_RESPAWN = MultiRespawnMod.id("trigger_respawn");

    private ModPackets() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_POINTS, (server, player, handler, buf, responseSender) ->
                server.execute(() -> RespawnSelectionService.handleRequest(player)));

        ServerPlayNetworking.registerGlobalReceiver(CHOOSE_POINT, (server, player, handler, buf, responseSender) -> {
            UUID pointId = buf.readUuid();
            server.execute(() -> RespawnSelectionService.handleChoice(player, pointId));
        });

        ServerPlayNetworking.registerGlobalReceiver(DELETE_POINT, (server, player, handler, buf, responseSender) -> {
            UUID pointId = buf.readUuid();
            server.execute(() -> {
                RespawnDataStorage storage = RespawnDataStorage.get(server);
                if (storage.getPlayerData(player.getUuid()).remove(pointId)) {
                    storage.markDirty();
                    sendSyncPoints(player, RespawnSelectionService.refreshValidPoints(player));
                }
            });
        });
    }

    public static void sendOpenChoiceScreen(ServerPlayerEntity player, List<RespawnPoint> points) {
        ServerPlayNetworking.send(player, OPEN_CHOICE_SCREEN, writePointViews(points));
    }

    public static void sendSyncPoints(ServerPlayerEntity player, List<RespawnPoint> points) {
        ServerPlayNetworking.send(player, SYNC_POINTS, writePointViews(points));
    }

    public static void sendError(ServerPlayerEntity player, String message) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(message);
        ServerPlayNetworking.send(player, ERROR_MESSAGE, buf);
    }

    public static void sendTriggerRespawn(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, TRIGGER_RESPAWN, PacketByteBufs.empty());
    }

    private static PacketByteBuf writePointViews(List<RespawnPoint> points) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(points.size());
        for (RespawnPoint point : points) {
            RespawnPointView.fromPoint(point).write(buf);
        }
        return buf;
    }

}
