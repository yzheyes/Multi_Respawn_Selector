package com.example.multirespawn;

import com.example.multirespawn.command.MultiRespawnCommand;
import com.example.multirespawn.network.ModPackets;
import com.example.multirespawn.respawn.PendingRespawnChoiceManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;

public class MultiRespawnMod implements ModInitializer {
    public static final String MOD_ID = "multirespawn";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModPackets.registerServerReceivers();
        MultiRespawnCommand.register();

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PendingRespawnChoiceManager.clear(handler.player.getUuid()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PendingRespawnChoiceManager.clearAll());
    }
}
