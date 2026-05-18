package com.example.multirespawn.mixin;

import com.example.multirespawn.data.PlayerRespawnData;
import com.example.multirespawn.data.RespawnDataStorage;
import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.data.RespawnPointType;
import com.example.multirespawn.respawn.SpawnPointCaptureGuard;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "setSpawnPoint", at = @At("TAIL"))
    private void multirespawn$captureSpawnPoint(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle,
                                                boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (SpawnPointCaptureGuard.isSuppressed()) {
            return;
        }

        if (pos == null) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ServerWorld world = player.getServer().getWorld(dimension);
        if (world == null) {
            return;
        }

        BlockState state = world.getBlockState(pos);
        RespawnPointType type = RespawnPointType.CUSTOM;
        if (state.isIn(BlockTags.BEDS)) {
            type = RespawnPointType.BED;
        } else if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
            type = RespawnPointType.RESPAWN_ANCHOR;
        }

        String name = switch (type) {
            case BED -> "Bed " + pos.toShortString();
            case RESPAWN_ANCHOR -> "Respawn Anchor " + pos.toShortString();
            case COMMAND, CUSTOM -> "Spawn Point " + pos.toShortString();
        };

        RespawnDataStorage storage = RespawnDataStorage.get(player.getServer());
        PlayerRespawnData data = storage.getPlayerData(player.getUuid());
        data.addOrUpdateKeepingExistingName(RespawnPoint.create(name, dimension.getValue(), pos, angle, player.getPitch(), type));
        storage.markDirty();
    }
}
