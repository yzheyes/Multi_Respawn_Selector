package com.example.multirespawn.event;

import com.example.multirespawn.data.PlayerRespawnData;
import com.example.multirespawn.data.RespawnDataStorage;
import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.data.RespawnPointType;
import com.example.multirespawn.network.ModPackets;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public final class RespawnPointNamingEvents {
    private RespawnPointNamingEvents() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || !player.isSneaking() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            boolean supportedBlock = state.isIn(BlockTags.BEDS) || state.isOf(Blocks.RESPAWN_ANCHOR);
            if (!supportedBlock) {
                return ActionResult.PASS;
            }

            PlayerRespawnData data = RespawnDataStorage.get(serverPlayer.getServer()).getPlayerData(serverPlayer.getUuid());
            RespawnPoint point = data.findByLocation(world.getRegistryKey().getValue(), pos).orElse(null);
            if (point == null || (point.getType() != RespawnPointType.BED && point.getType() != RespawnPointType.RESPAWN_ANCHOR)) {
                return ActionResult.PASS;
            }

            ModPackets.sendOpenRenameScreen(serverPlayer, point);
            return ActionResult.SUCCESS;
        });
    }
}
