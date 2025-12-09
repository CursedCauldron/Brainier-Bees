package com.dopadream.brainierbees.ai.tasks;

import com.dopadream.brainierbees.config.BrainierBeesConfig;
import com.dopadream.brainierbees.registry.ModMemoryTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Objects;

public class BeePathfinding extends Behavior<Bee> {
    private BeePathfinding.CachedPathHolder beeCachedPathHolder;

    public BeePathfinding() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    // Make bees not get stuck on ceiling anymore and lag people as a result.
    // Original code by TelepathicGrunt, edited and repurposed by dopadream with permission!
    // Check out Bumblezone!

    public static boolean blockCloserThan(Bee bee, BlockPos blockPos, int i) {
        return blockPos.closerThan(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos(), i);
    }

    public static void smartBeesTM(Bee beeEntity, CachedPathHolder cachedPathHolder) {

        if (cachedPathHolder == null || cachedPathHolder.pathTimer > 50 || cachedPathHolder.cachedPath == null ||
                (beeEntity.getDeltaMovement().length() <= 0.05d && cachedPathHolder.pathTimer > 5) ||
                beeEntity.blockPosition().distManhattan(cachedPathHolder.cachedPath.getTarget()) <= 4) {
            Level world = beeEntity.level();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(beeEntity.blockPosition());
            LevelChunk levelChunk = world.getChunkAt(mutable);
            int height = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, mutable.getX(), mutable.getZ()) + 1;

            for (int attempt = 0; attempt < 15 && beeEntity.blockPosition().distManhattan(mutable) <= 5; attempt++) {
                // pick a random place to fly to

                if (!beeEntity.isLeashed()) {
                    if ((world.dimensionType().hasCeiling()) || (beeEntity.getBlockY() <= (height + 3))) {
                        mutable.set(beeEntity.blockPosition()).move(
                                beeEntity.getRandom().nextInt(21) - 10,
                                beeEntity.getRandom().nextInt(21) - 10,
                                beeEntity.getRandom().nextInt(21) - 10
                        );
                    } else {
                        mutable.set(beeEntity.blockPosition()).move(
                                beeEntity.getRandom().nextInt(21) - 10,
                                beeEntity.getRandom().nextInt(21) - 10,
                                beeEntity.getRandom().nextInt(21) - 10
                        );
                    }
                } else {
                    mutable.set(beeEntity.blockPosition()).move(
                            beeEntity.getRandom().nextInt(5) - 2,
                            beeEntity.getRandom().nextInt(5) - 2,
                            beeEntity.getRandom().nextInt(5) - 2
                    );
                }

                boolean valid = true;

                for (int i = 0; i < 3; i++) {
                    if (!world.getBlockState(mutable.offset(0, -i, 0)).isAir()) {
                        valid = false;
                        break;
                    }
                }

                if (valid && !world.getBlockState(mutable.offset(0, -4, 0)).isAir()) {
                    mutable.set(mutable.getX(), mutable.getY() - beeEntity.getRandom().nextInt(0, 2), mutable.getZ());
                    if (beeEntity.getLeashData() == null) {
                        if (beeEntity.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isEmpty()) {
                            break; // Valid spot to go towards. Homeless bees only!
                        } else {
                            if (!blockCloserThan(beeEntity, mutable, BrainierBeesConfig.MAX_WANDER_RADIUS)) {
                                Vec3 hivePos = Vec3.atCenterOf(beeEntity.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
                                mutable.set(
                                        lerp(beeEntity.position(), hivePos, 0.25)
                                );
                                break;
                            }
                            break; // Valid spot to go towards within a set radius of their home (if they have one!)
                        }
                    } else {
                        mutable.set(
                                lerp(mutable.getCenter(), Objects.requireNonNull(beeEntity.getLeashData().leashHolder).position(), 0.25)
                        );
                        break; // Wander freely in a small area when on a leash!
                    }
                } else {
                    mutable.set(beeEntity.blockPosition());
                }
            }

            Path newPath = beeEntity.getNavigation().createPath(mutable, 1);
            beeEntity.getNavigation().moveTo(newPath, 1);

            if (cachedPathHolder == null) {
                cachedPathHolder = new CachedPathHolder();
            }
            cachedPathHolder.cachedPath = newPath;
            cachedPathHolder.pathTimer = 0;
        } else {
            beeEntity.getNavigation().moveTo(cachedPathHolder.cachedPath, 1);
            cachedPathHolder.pathTimer += 1;
        }

    }

    public static Vec3i lerp(Vec3 current, Vec3 target, double t) {
        double x = current.x + (target.x - current.x) * t;
        double y = current.y + (target.y - current.y) * t;
        double z = current.z + (target.z - current.z) * t;
        return new Vec3i((int) x, (int) y, (int) z);
    }

    @Override
    protected void start(ServerLevel serverLevel, Bee livingEntity, long l) {
        smartBeesTM(livingEntity, beeCachedPathHolder);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Bee bee) {
        return (bee.getNavigation().isDone() && bee.getRandom().nextInt(10) == 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee bee, long l) {
        return (bee.getNavigation().isInProgress());
    }

    @Override
    protected void tick(ServerLevel serverLevel, Bee livingEntity, long l) {
        super.tick(serverLevel, livingEntity, l);
        if ((livingEntity).hasNectar()) {
            livingEntity.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, 400);
        }
    }

    public static class CachedPathHolder {
        public Path cachedPath;
        public int pathTimer = 0;

        public CachedPathHolder() {
        }
    }
}
