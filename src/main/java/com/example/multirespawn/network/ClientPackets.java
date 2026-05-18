package com.example.multirespawn.network;

import com.example.multirespawn.client.RespawnChoiceScreen;
import com.example.multirespawn.client.RenameRespawnPointScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClientPackets {
    private ClientPackets() {
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OPEN_CHOICE_SCREEN, (client, handler, buf, responseSender) -> {
            List<RespawnPointView> points = readPointViews(buf);
            client.execute(() -> client.setScreen(new RespawnChoiceScreen(points, client.currentScreen)));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SYNC_POINTS, (client, handler, buf, responseSender) -> {
            List<RespawnPointView> points = readPointViews(buf);
            client.execute(() -> {
                Screen screen = client.currentScreen;
                if (screen instanceof RespawnChoiceScreen choiceScreen) {
                    choiceScreen.setPoints(points);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ERROR_MESSAGE, (client, handler, buf, responseSender) -> {
            String message = buf.readString(32767);
            client.execute(() -> {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal(message), false);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TRIGGER_RESPAWN, (client, handler, buf, responseSender) ->
                client.execute(ClientPackets::sendVanillaRespawnRequest));

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OPEN_RENAME_SCREEN, (client, handler, buf, responseSender) -> {
            UUID pointId = buf.readUuid();
            String currentName = buf.readString(32767);
            String dimension = buf.readIdentifier().toString();
            String pos = buf.readBlockPos().toShortString();
            client.execute(() -> client.setScreen(new RenameRespawnPointScreen(pointId, currentName, dimension, pos)));
        });
    }

    public static void requestRespawnPoints() {
        ClientPlayNetworking.send(ModPackets.REQUEST_POINTS, PacketByteBufs.empty());
    }

    public static void chooseRespawnPoint(UUID pointId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(pointId);
        ClientPlayNetworking.send(ModPackets.CHOOSE_POINT, buf);
    }

    public static void deleteRespawnPoint(UUID pointId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(pointId);
        ClientPlayNetworking.send(ModPackets.DELETE_POINT, buf);
    }

    public static void renameRespawnPoint(UUID pointId, String newName) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(pointId);
        buf.writeString(newName.trim(), 64);
        ClientPlayNetworking.send(ModPackets.RENAME_POINT, buf);
    }

    private static void sendVanillaRespawnRequest() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
        }
    }

    private static List<RespawnPointView> readPointViews(PacketByteBuf buf) {
        int size = buf.readVarInt();
        List<RespawnPointView> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(RespawnPointView.read(buf));
        }
        return points;
    }
}
