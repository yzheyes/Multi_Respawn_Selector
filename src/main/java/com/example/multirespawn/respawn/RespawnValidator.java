package com.example.multirespawn.respawn;

import com.example.multirespawn.data.RespawnPoint;
import com.example.multirespawn.data.RespawnPointType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public final class RespawnValidator {
    private RespawnValidator() {
    }

    public static ValidationResult validate(ServerPlayerEntity player, RespawnPoint point) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, point.getDimensionId());
        ServerWorld world = player.getServer().getWorld(worldKey);
        if (world == null) {
            return ValidationResult.invalid("Dimension does not exist: " + point.getDimensionId());
        }

        BlockState state = world.getBlockState(point.getPos());
        if (point.getType() == RespawnPointType.BED && !state.isIn(net.minecraft.registry.tag.BlockTags.BEDS)) {
            return ValidationResult.invalid("The bed no longer exists.");
        }

        if (point.getType() == RespawnPointType.RESPAWN_ANCHOR) {
            if (!state.isOf(Blocks.RESPAWN_ANCHOR)) {
                return ValidationResult.invalid("The respawn anchor no longer exists.");
            }
            if (state.get(RespawnAnchorBlock.CHARGES) <= 0) {
                return ValidationResult.invalid("The respawn anchor has no charge.");
            }
        }

        Optional<Vec3d> safePos = findSafeRespawnPos(world, point.getPos());
        if (safePos.isEmpty()) {
            return ValidationResult.invalid("No safe landing position was found.");
        }

        return ValidationResult.valid(world, safePos.get());
    }

    public static Optional<Vec3d> findSafeRespawnPos(ServerWorld world, BlockPos anchorPos) {
        int horizontalRadius = 3;
        int minDy = -1;
        int maxDy = 2;

        for (int dy = minDy; dy <= maxDy; dy++) {
            for (int radius = 0; radius <= horizontalRadius; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }

                        BlockPos feet = anchorPos.add(dx, dy, dz);
                        if (isSafe(world, feet)) {
                            return Optional.of(Vec3d.ofBottomCenter(feet));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isSafe(ServerWorld world, BlockPos feet) {
        if (feet.getY() <= world.getBottomY()) {
            return false;
        }

        BlockPos floor = feet.down();
        BlockState floorState = world.getBlockState(floor);
        FluidState feetFluid = world.getFluidState(feet);
        FluidState floorFluid = world.getFluidState(floor);

        if (feetFluid.isIn(FluidTags.LAVA) || floorFluid.isIn(FluidTags.LAVA)) {
            return false;
        }

        if (!floorState.isSideSolidFullSquare(world, floor, Direction.UP)) {
            return false;
        }

        Box playerBox = new Box(
                feet.getX() + 0.2D, feet.getY(), feet.getZ() + 0.2D,
                feet.getX() + 0.8D, feet.getY() + 1.8D, feet.getZ() + 0.8D
        );
        return world.isSpaceEmpty(playerBox);
    }
}
