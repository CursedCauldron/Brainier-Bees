package cursedcauldron.brainierbees.ai.tasks;

import com.google.common.collect.Lists;
import cursedcauldron.brainierbees.ai.ModMemoryTypes;
import cursedcauldron.brainierbees.util.HiveAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Map;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.*;

public class GoToHiveTask extends Behavior<Bee> {

    public GoToHiveTask() {
        super(Map.of(ModMemoryTypes.HIVE_POS, MemoryStatus.VALUE_PRESENT, COOLDOWN_LOCATE_HIVE, MemoryStatus.VALUE_ABSENT));
    }

//    public boolean canBeeUse() {
//        return Bee.this.hivePos != null
//                && !Bee.this.hasRestriction()
//                && Bee.this.wantsToEnterHive()
//                && !this.hasReachedTarget(Bee.this.hivePos)
//                && Bee.this.level.getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
//    }


    private boolean isHiveNearFire(ServerLevel level, Bee bee) {
        if (bee.getBrain().getMemory(HIVE_POS).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(bee.getBrain().getMemory(HIVE_POS).get().pos());
            return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)blockEntity).isFireNearby();
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Bee bee, long l) {
        return bee.getBrain().getMemory(HIVE_POS).isPresent() && (bee.getBrain().getMemory(WANTS_HIVE).isPresent() && bee.getBrain().getMemory(WANTS_HIVE).get()) && !isHiveNearFire(level, bee);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Bee bee) {
        return bee.getBrain().getMemory(HIVE_POS).isPresent() && (bee.getBrain().getMemory(WANTS_HIVE).isPresent() && bee.getBrain().getMemory(WANTS_HIVE).get()) && !isHiveNearFire(level, bee);
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
        if (bee.getBrain().getMemory(TRAVELLING_TICKS).isPresent() && bee.getBrain().getMemory(HIVE_POS).isPresent()) {
            bee.getBrain().setMemory(TRAVELLING_TICKS, bee.getBrain().getMemory(TRAVELLING_TICKS).get() + 1);
            if (bee.getBrain().getMemory(TRAVELLING_TICKS).isPresent() && bee.getBrain().getMemory(TRAVELLING_TICKS).get() > 600) {
                ((HiveAccessor)bee).dropAndBlacklistHive(bee);
            } else if (!bee.getNavigation().isInProgress()) {
                    this.pathfindDirectlyTowards(bee.getBrain().getMemory(HIVE_POS).get().pos(), bee);
                    }
                } else {
                    boolean bl = this.pathfindDirectlyTowards(bee.getBrain().getMemory(HIVE_POS).get().pos(), bee);
                    if (!bl) {
                        ((HiveAccessor)bee).dropAndBlacklistHive(bee);
                    } else if (bee.getBrain().getMemory(LAST_PATH).isPresent() && bee.getNavigation().getPath().sameAs(bee.getBrain().getMemory(LAST_PATH).get())) {
                        if (!bee.getBrain().hasMemoryValue(STUCK_TICKS)) {
                            bee.getBrain().setMemory(STUCK_TICKS, 1);
                        } else {
                            bee.getBrain().setMemory(STUCK_TICKS, bee.getBrain().getMemory(STUCK_TICKS).get() + 1);
                        }
                        if (bee.getBrain().getMemory(STUCK_TICKS).isPresent() && bee.getBrain().getMemory(STUCK_TICKS).get() > 600) {
                            ((HiveAccessor)bee).dropAndBlacklistHive(bee);
                        }
                    } else {
                        bee.getBrain().setMemory(LAST_PATH, bee.getNavigation().getPath());
                    }

                }
    }

    private boolean pathfindDirectlyTowards(BlockPos blockPos, Bee bee) {
        bee.getNavigation().setMaxVisitedNodesMultiplier(10.0F);
        bee.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
        return bee.getNavigation().getPath() != null && bee.getNavigation().getPath().canReach();
    }


}
