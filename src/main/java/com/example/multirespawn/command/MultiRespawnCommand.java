package com.example.multirespawn.command;

import com.example.multirespawn.data.PlayerRespawnData;
import com.example.multirespawn.data.RespawnDataStorage;
import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.data.RespawnPointType;
import com.example.multirespawn.respawn.RespawnSelectionService;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MultiRespawnCommand {
    private MultiRespawnCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("multirespawn")
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.greedyString())
                                        .executes(context -> add(context.getSource(),
                                                StringArgumentType.getString(context, "name")))))
                        .then(literal("list")
                                .executes(context -> list(context.getSource())))
                        .then(literal("remove")
                                .then(argument("id_or_name", StringArgumentType.greedyString())
                                        .executes(context -> remove(context.getSource(),
                                                StringArgumentType.getString(context, "id_or_name")))))
                        .then(literal("rename")
                                .then(argument("id_or_name", StringArgumentType.string())
                                        .then(argument("new_name", StringArgumentType.greedyString())
                                                .executes(context -> rename(context.getSource(),
                                                        StringArgumentType.getString(context, "id_or_name"),
                                                        StringArgumentType.getString(context, "new_name"))))))
                        .then(literal("clear")
                                .executes(context -> clear(context.getSource())))));
    }

    private static int add(ServerCommandSource source, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        RespawnDataStorage storage = RespawnDataStorage.get(player.getServer());
        PlayerRespawnData data = storage.getPlayerData(player.getUuid());
        Identifier dimension = player.getWorld().getRegistryKey().getValue();

        RespawnPoint point = RespawnPoint.create(name, dimension, player.getBlockPos(), player.getYaw(), player.getPitch(),
                RespawnPointType.COMMAND);
        data.addOrUpdate(point);
        storage.markDirty();

        source.sendFeedback(() -> Text.translatable("commands.multirespawn.added", name), false);
        return 1;
    }

    private static int list(ServerCommandSource source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        RespawnSelectionService.refreshValidPoints(player);
        PlayerRespawnData data = RespawnDataStorage.get(player.getServer()).getPlayerData(player.getUuid());

        if (data.getPoints().isEmpty()) {
            source.sendFeedback(() -> Text.translatable("commands.multirespawn.empty"), false);
            return 0;
        }

        source.sendFeedback(() -> Text.translatable("commands.multirespawn.list.header"), false);
        for (RespawnPoint point : data.getPoints()) {
            source.sendFeedback(() -> Text.literal("- " + point.getName()
                    + " [" + point.getId() + "] "
                    + point.getDimensionId() + " "
                    + point.getPos().toShortString() + " "
                    + point.getType()), false);
        }
        return data.getPoints().size();
    }

    private static int remove(ServerCommandSource source, String idOrName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        RespawnDataStorage storage = RespawnDataStorage.get(player.getServer());
        PlayerRespawnData data = storage.getPlayerData(player.getUuid());

        boolean removed = data.removeByIdOrName(idOrName);
        if (removed) {
            storage.markDirty();
            source.sendFeedback(() -> Text.translatable("commands.multirespawn.removed", idOrName), false);
            return 1;
        }

        source.sendError(Text.translatable("commands.multirespawn.no_match", idOrName));
        return 0;
    }

    private static int clear(ServerCommandSource source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        RespawnDataStorage storage = RespawnDataStorage.get(player.getServer());
        PlayerRespawnData data = storage.getPlayerData(player.getUuid());
        int count = data.getPoints().size();
        data.clear();
        storage.markDirty();
        source.sendFeedback(() -> Text.translatable("commands.multirespawn.cleared", count), false);
        return count;
    }

    private static int rename(ServerCommandSource source, String idOrName, String newName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String normalizedName = normalizeName(newName);
        if (normalizedName.isEmpty()) {
            source.sendError(Text.translatable("commands.multirespawn.name_empty"));
            return 0;
        }

        RespawnDataStorage storage = RespawnDataStorage.get(player.getServer());
        PlayerRespawnData data = storage.getPlayerData(player.getUuid());
        RespawnPoint point = data.findByIdOrName(idOrName).orElse(null);
        if (point == null) {
            source.sendError(Text.translatable("commands.multirespawn.no_match", idOrName));
            return 0;
        }

        String oldName = point.getName();
        point.rename(normalizedName);
        storage.markDirty();
        source.sendFeedback(() -> Text.translatable("commands.multirespawn.renamed", oldName, normalizedName), false);
        return 1;
    }

    private static String normalizeName(String name) {
        String trimmed = name.trim();
        if (trimmed.length() > 64) {
            return trimmed.substring(0, 64);
        }
        return trimmed;
    }
}
