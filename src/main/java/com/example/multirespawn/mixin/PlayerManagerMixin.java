package com.example.multirespawn.mixin;

import com.example.multirespawn.respawn.RespawnTeleporter;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void multirespawn$applySelectedRespawn(ServerPlayerEntity oldPlayer, boolean alive,
                                                  CallbackInfoReturnable<ServerPlayerEntity> cir) {
        RespawnTeleporter.applyPendingRespawn(oldPlayer, cir.getReturnValue());
    }
}
