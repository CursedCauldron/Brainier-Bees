package cursedcauldron.brainierbees.ai.tasks;

import cursedcauldron.brainierbees.BrainierBees;
import cursedcauldron.brainierbees.ai.ModMemoryTypes;
import cursedcauldron.brainierbees.util.SimpleConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Map;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.HIVE_POS;

public class BeePathfinding extends Behavior<Bee> {
    private BeePathfinding.CachedPathHolder beeCachedPathHolder;

    public  BeePathfinding() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    // Make bees not get stuck on ceiling anymore and lag people as a result.
    // Original code by TelepathicGrunt, edited and repurposed by dopadream with permission!
    // Check out Bumblezone!

    @Override
    protected void start(ServerLevel serverLevel, Bee livingEntity, long l) {
        smartBeesTM(livingEntity, beeCachedPathHolder);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Bee bee) {
        return (bee.getNavigation().isDone() && bee.getRandom().nextInt(10) == 0  || !bee.getBrain().getMemory(HIVE_POS).isPresent());
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee bee, long l) {
        return (bee.getNavigation().isInProgress()  ||  !bee.getBrain().getMemory(HIVE_POS).isPresent());
    }

    @Override
    protected void tick(ServerLevel serverLevel, Bee livingEntity, long l) {
        super.tick(serverLevel, livingEntity, l);
        if ((livingEntity).hasNectar()) {
            livingEntity.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, 400);
        }
    }

    public static boolean blockCloserThan(Bee bee, BlockPos blockPos, int i) {
        return blockPos.closerThan(bee.getBrain().getMemory(HIVE_POS).get().pos(), i);
    }

    public static CachedPathHolder smartBeesTM(Bee beeEntity, CachedPathHolder cachedPathHolder) {

        if(cachedPathHolder == null || cachedPathHolder.pathTimer > 50 || cachedPathHolder.cachedPath == null ||
                (beeEntity.getDeltaMovement().length() <= 0.05d && cachedPathHolder.pathTimer > 5) ||
                beeEntity.blockPosition().distManhattan(cachedPathHolder.cachedPath.getTarget()) <= 4)
        {
            Level world = beeEntity.level;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(beeEntity.blockPosition());
            LevelChunk levelChunk = world.getChunkAt(mutable);
            int height = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, mutable.getX(), mutable.getZ()) + 1;

            for(int attempt = 0; attempt < 11 || beeEntity.blockPosition().distManhattan(mutable) <= 5; attempt++) {
                // pick a random place to fly to

                if  ((world.dimensionType().hasCeiling()) || (beeEntity.getBlockY() <= (height + 3))) {
                    mutable.set(beeEntity.blockPosition()).move(
                            beeEntity.getRandom().nextInt(21) - 10,
                            beeEntity.getRandom().nextInt(6) - 2,
                            beeEntity.getRandom().nextInt(21) - 10
                    );
                } else {
                    mutable.set(beeEntity.blockPosition()).move(
                            beeEntity.getRandom().nextInt(21) - 10,
                            beeEntity.getRandom().nextInt(6) - 5,
                            beeEntity.getRandom().nextInt(21) - 10
                    );
                }
                if (beeEntity.getBrain().getMemory(HIVE_POS).isEmpty()) {
                    if (world.getBlockState(new BlockPos(mutable.getX(), mutable.getY()-2, mutable.getZ())).isAir()) {
                        break; // Valid spot to go towards. Homeless bees only!
                    }
                } else {
                    if (blockCloserThan(beeEntity, mutable, new BrainierBees().MAX_WANDER_RADIUS) && world.getBlockState(new BlockPos(mutable.getX(), mutable.getY()-2, mutable.getZ())).isAir()) {
                        break; // Valid spot to go towards within a set radius of their home (if they have one!)
                    }
                }
            }

            Path newPath = beeEntity.getNavigation().createPath(mutable, 1);
            beeEntity.getNavigation().moveTo(newPath, 1);

            if(cachedPathHolder == null) {
                cachedPathHolder = new CachedPathHolder();
            }
            cachedPathHolder.cachedPath = newPath;
            cachedPathHolder.pathTimer = 0;
        }
        else{
            beeEntity.getNavigation().moveTo(cachedPathHolder.cachedPath, 1);
            cachedPathHolder.pathTimer += 1;
        }

        return cachedPathHolder;
    }

    public static class CachedPathHolder {
        public Path cachedPath;
        public int pathTimer = 0;

        public CachedPathHolder() {}
    }
}
