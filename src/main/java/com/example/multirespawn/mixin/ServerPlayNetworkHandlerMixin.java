package com.example.multirespawn.mixin;

import com.example.multirespawn.respawn.RespawnSelectionService;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onClientStatus", at = @At("HEAD"), cancellable = true)
    private void multirespawn$guardRespawnRequest(ClientStatusC2SPacket packet, CallbackInfo ci) {
        if (packet.getMode() != ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) {
            return;
        }

        if (RespawnSelectionService.shouldBlockVanillaRespawn(player)) {
            ci.cancel();
        }
    }
}
