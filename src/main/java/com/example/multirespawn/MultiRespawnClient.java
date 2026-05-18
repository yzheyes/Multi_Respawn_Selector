package com.example.multirespawn;

import com.example.multirespawn.network.ClientPackets;
import net.fabricmc.api.ClientModInitializer;

public class MultiRespawnClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPackets.registerClientReceivers();
    }
}
