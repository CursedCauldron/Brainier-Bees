package com.dopadream.brainierbees.ai.tasks;

import com.dopadream.brainierbees.BrainierBees;
import com.dopadream.brainierbees.ai.ModMemoryTypes;
import com.dopadream.brainierbees.util.HiveAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class GoToHiveTask extends Behavior<Bee> {
    private static final Logger LOGGER = LogManager.getLogger(BrainierBees.MOD_ID);

    public GoToHiveTask() {
        super(Map.of(ModMemoryTypes.HIVE_POS, MemoryStatus.VALUE_PRESENT, ModMemoryTypes.COOLDOWN_LOCATE_HIVE, MemoryStatus.VALUE_ABSENT));
    }

//    public boolean canBeeUse() {
//        return Bee.this.hivePos != null
//                && !Bee.this.hasRestriction()
//                && Bee.this.wantsToEnterHive()
//                && !this.hasReachedTarget(Bee.this.hivePos)
//                && Bee.this.level.getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
//    }


    private boolean isHiveNearFire(ServerLevel level, Bee bee) {
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
            return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity) blockEntity).isFireNearby();
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Bee bee, long l) {
        return bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent() && (bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).get()) && !isHiveNearFire(level, bee);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Bee bee) {
        return bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent() && (bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).get()) && !isHiveNearFire(level, bee);
    }

    @Override
    protected void start(ServerLevel serverLevel, Bee bee, long l) {
        super.start(serverLevel, bee, l);
        bee.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        bee.resetLove();
    }


    @Override
    protected void stop(ServerLevel serverLevel, Bee bee, long l) {
        super.stop(serverLevel, bee, l);
    }

    @Override
    protected void tick(ServerLevel level, Bee bee, long l) {
        if (bee.getBrain().getMemory(ModMemoryTypes.TRAVELLING_TICKS).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()) {
            bee.getBrain().setMemory(ModMemoryTypes.TRAVELLING_TICKS, bee.getBrain().getMemory(ModMemoryTypes.TRAVELLING_TICKS).get() + 1);
            if (bee.getBrain().getMemory(ModMemoryTypes.TRAVELLING_TICKS).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.TRAVELLING_TICKS).get() > (78 * (new BrainierBees().MAX_WANDER_RADIUS))) {
                ((HiveAccessor) bee).dropAndBlacklistHive(bee);
            } else if (!bee.getNavigation().isInProgress()) {
                this.pathfindDirectlyTowards(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos(), bee);
            }
        } else {
            bee.getBrain().setMemory(ModMemoryTypes.TRAVELLING_TICKS, 1);
            boolean bl = this.pathfindDirectlyTowards(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos(), bee);
            if (!bl) {
                ((HiveAccessor) bee).dropAndBlacklistHive(bee);
            } else if (bee.getBrain().getMemory(ModMemoryTypes.LAST_PATH).isPresent() && bee.getNavigation().getPath().sameAs(bee.getBrain().getMemory(ModMemoryTypes.LAST_PATH).get())) {
                if (!bee.getBrain().hasMemoryValue(ModMemoryTypes.STUCK_TICKS)) {
                    bee.getBrain().setMemory(ModMemoryTypes.STUCK_TICKS, 1);
                } else {
                    bee.getBrain().setMemory(ModMemoryTypes.STUCK_TICKS, bee.getBrain().getMemory(ModMemoryTypes.STUCK_TICKS).get() + 1);
                }
                if (bee.getBrain().getMemory(ModMemoryTypes.STUCK_TICKS).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.STUCK_TICKS).get() > 600) {
                    ((HiveAccessor) bee).dropAndBlacklistHive(bee);
                }
            } else {
                bee.getBrain().setMemory(ModMemoryTypes.LAST_PATH, bee.getNavigation().getPath());
            }
        }
//        if (bee.getBrain().getMemory(TRAVELLING_TICKS).isPresent()) {
//            LOGGER.warn(bee.getBrain().getMemory(TRAVELLING_TICKS).get());
//        }
    }

    private boolean pathfindDirectlyTowards(BlockPos blockPos, Bee bee) {
        bee.getNavigation().setMaxVisitedNodesMultiplier(10.0F);
        bee.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
        return bee.getNavigation().getPath() != null && bee.getNavigation().getPath().canReach();
    }


}
