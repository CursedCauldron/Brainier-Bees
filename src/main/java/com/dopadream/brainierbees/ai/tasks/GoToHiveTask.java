package com.dopadream.brainierbees.ai.tasks;

import com.dopadream.brainierbees.ai.BeeAi;
import com.dopadream.brainierbees.config.BrainierBeesConfig;
import com.dopadream.brainierbees.registry.ModMemoryTypes;
import com.dopadream.brainierbees.util.HiveAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GoToHiveTask extends Behavior<Bee> {

    public GoToHiveTask() {
        super(Map.of(ModMemoryTypes.HIVE_POS, MemoryStatus.VALUE_PRESENT, ModMemoryTypes.COOLDOWN_LOCATE_HIVE, MemoryStatus.VALUE_ABSENT));
    }


    @Override
    protected boolean canStillUse(ServerLevel level, Bee bee, long l) {
        return bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()
                && (bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).isPresent()
                && bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).get())
                && !BeeAi.isHiveNearFire(level, bee)
                && checkLeadDist(bee);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Bee bee) {
        return bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()
                && (bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).isPresent()
                && bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).get())
                && !BeeAi.isHiveNearFire(level, bee)
                && checkLeadDist(bee);
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
        var brain = bee.getBrain();

        Optional<GlobalPos> hivePosOpt = brain.getMemory(ModMemoryTypes.HIVE_POS);
        Optional<Integer> travellingTicksOpt = brain.getMemory(ModMemoryTypes.TRAVELLING_TICKS);
        Optional<Integer> stuckTicksOpt = brain.getMemory(ModMemoryTypes.STUCK_TICKS);
        Optional<Path> lastPathOpt = brain.getMemory(ModMemoryTypes.LAST_PATH);

        // Handle the homeless
        if (hivePosOpt.isEmpty()) {
            brain.setMemory(ModMemoryTypes.TRAVELLING_TICKS, 1);
            dropHive(bee);
            return;
        }

        GlobalPos hiveGlobal = hivePosOpt.get();
        BlockPos hivePos = hiveGlobal.pos();

        // Travelling ticks handling
        int travellingTicks = travellingTicksOpt.orElse(0) + 1;
        brain.setMemory(ModMemoryTypes.TRAVELLING_TICKS, travellingTicks);

        int maxTravelTicks = 80 * BrainierBeesConfig.MAX_WANDER_RADIUS;
        if (travellingTicks > maxTravelTicks) {
            dropHive(bee);
            return;
        }

        // Pathfinding
        if (!bee.getNavigation().isInProgress()) {
            if (!pathfindDirectlyTowards(hivePos, bee)) {
                dropHive(bee);
                return;
            }
        }

        // Stuck ticks
        if (lastPathOpt.isPresent() && Objects.requireNonNull(bee.getNavigation().getPath()).sameAs(lastPathOpt.get())) {

            int stuckTicks = stuckTicksOpt.orElse(0) + 1;
            brain.setMemory(ModMemoryTypes.STUCK_TICKS, stuckTicks);

            if (stuckTicks > 600) {
                dropHive(bee);
            }

        } else {
            brain.setMemory(ModMemoryTypes.LAST_PATH, bee.getNavigation().getPath());
        }
    }

    private void dropHive(Bee bee) {
        ((HiveAccessor) bee).dropAndBlacklistHive(bee);
    }

    private boolean checkLeadDist(Bee bee) {
        var canReach = true;
        if (bee.isLeashed() && bee.getLeashData() != null && bee.getLeashData().leashHolder != null) {
            BlockPos leashOrigin = bee.getLeashData().leashHolder.blockPosition();
            if (!leashOrigin.closerThan(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos(), 5.5)) {
                canReach = false;
            }
        }
        return canReach;
    }


    private boolean pathfindDirectlyTowards(BlockPos blockPos, Bee bee) {
        bee.getNavigation().setMaxVisitedNodesMultiplier(10.0F);
        bee.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
        return bee.getNavigation().getPath() != null && bee.getNavigation().getPath().canReach();
    }
}
