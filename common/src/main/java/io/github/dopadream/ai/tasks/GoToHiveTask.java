package io.github.dopadream.ai.tasks;


import io.github.dopadream.config.BrainierBeesConfig;
import io.github.dopadream.mixin.HiveAccessor;
import io.github.dopadream.registry.BrainierBeesRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;

public class GoToHiveTask extends Behavior<Bee> {

    public GoToHiveTask() {
        super(Map.of(BrainierBeesRegistries.HIVE_POS.get(), MemoryStatus.VALUE_PRESENT, BrainierBeesRegistries.LOCATE_HIVE_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT));
    }


    private boolean isHiveNearFire(ServerLevel level, Bee bee) {
        if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos());
            return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity) blockEntity).isFireNearby();
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Bee bee, long l) {
        return bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isPresent() && (bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).get()) && !isHiveNearFire(level, bee);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Bee bee) {
        return bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isPresent() && (bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).get()) && !isHiveNearFire(level, bee);
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
        if (bee.getBrain().getMemory(BrainierBeesRegistries.TRAVELLING_TICKS.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isPresent()) {
            bee.getBrain().setMemory(BrainierBeesRegistries.TRAVELLING_TICKS.get(), bee.getBrain().getMemory(BrainierBeesRegistries.TRAVELLING_TICKS.get()).get() + 1);
            if (bee.getBrain().getMemory(BrainierBeesRegistries.TRAVELLING_TICKS.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.TRAVELLING_TICKS.get()).get() > (78 * (BrainierBeesConfig.MAX_WANDER_RADIUS))) {
                ((HiveAccessor) bee).dropAndBlacklistHive(bee);
            } else if (!bee.getNavigation().isInProgress()) {
                this.pathfindDirectlyTowards(bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos(), bee);
            }
        } else {
            bee.getBrain().setMemory(BrainierBeesRegistries.TRAVELLING_TICKS.get(), 1);
            boolean bl = this.pathfindDirectlyTowards(bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos(), bee);
            if (!bl) {
                ((HiveAccessor) bee).dropAndBlacklistHive(bee);
            } else if (bee.getBrain().getMemory(BrainierBeesRegistries.LAST_PATH.get()).isPresent() && bee.getNavigation().getPath().sameAs(bee.getBrain().getMemory(BrainierBeesRegistries.LAST_PATH.get()).get())) {
                if (!bee.getBrain().hasMemoryValue(BrainierBeesRegistries.STUCK_TICKS.get())) {
                    bee.getBrain().setMemory(BrainierBeesRegistries.STUCK_TICKS.get(), 1);
                } else {
                    bee.getBrain().setMemory(BrainierBeesRegistries.STUCK_TICKS.get(), bee.getBrain().getMemory(BrainierBeesRegistries.STUCK_TICKS.get()).get() + 1);
                }
                if (bee.getBrain().getMemory(BrainierBeesRegistries.STUCK_TICKS.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.STUCK_TICKS.get()).get() > 600) {
                    ((HiveAccessor) bee).dropAndBlacklistHive(bee);
                }
            } else {
                bee.getBrain().setMemory(BrainierBeesRegistries.LAST_PATH.get(), bee.getNavigation().getPath());
            }
        }
    }

    private boolean pathfindDirectlyTowards(BlockPos blockPos, Bee bee) {
        bee.getNavigation().setMaxVisitedNodesMultiplier(10.0F);
        bee.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
        return bee.getNavigation().getPath() != null && bee.getNavigation().getPath().canReach();
    }


}
