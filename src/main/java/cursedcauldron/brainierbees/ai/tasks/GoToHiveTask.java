package cursedcauldron.brainierbees.ai.tasks;

import com.google.common.collect.Lists;
import cursedcauldron.brainierbees.ai.ModMemoryTypes;
import cursedcauldron.brainierbees.mixin.BeeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Map;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.*;

public class GoToHiveTask extends Behavior<Bee> {

    public GoToHiveTask() {
        super(Map.of(ModMemoryTypes.HIVE_POS, MemoryStatus.VALUE_PRESENT));
    }

//    public boolean canBeeUse() {
//        return Bee.this.hivePos != null
//                && !Bee.this.hasRestriction()
//                && Bee.this.wantsToEnterHive()
//                && !this.hasReachedTarget(Bee.this.hivePos)
//                && Bee.this.level.getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
//    }

    public boolean wantsToEnterHive(ServerLevel level, Bee bee) {
        if (((BeeAccessor)bee).getStayOutOfHiveCountdown() <= 0 && !bee.hasStung() && bee.getTarget() == null) {
            boolean bl = level.isRaining() || level.isNight() || bee.hasNectar();
            return bl && !this.isHiveNearFire(level, bee);
        } else {
            return false;
        }
    }

    private boolean isHiveNearFire(ServerLevel level, Bee bee) {
        if (bee.getBrain().getMemory(HIVE_POS).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(bee.getBrain().getMemory(HIVE_POS).get());
            return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)blockEntity).isFireNearby();
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Bee bee, long l) {
        return bee.getBrain().getMemory(HIVE_POS).isPresent() && bee.getBrain().getMemory(WANTS_HIVE).isPresent() && !isHiveNearFire(level, bee);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Bee bee) {
        return bee.getBrain().getMemory(HIVE_POS).isPresent() && bee.getBrain().getMemory(WANTS_HIVE).isPresent() && !isHiveNearFire(level, bee);
    }

    @Override
    protected void start(ServerLevel serverLevel, Bee bee, long l) {
        super.start(serverLevel, bee, l);
    }



    @Override
    protected void stop(ServerLevel serverLevel, Bee bee, long l) {
        super.stop(serverLevel, bee, l);
    }

    @Override
    protected void tick(ServerLevel level, Bee bee, long l) {
        if (bee.getBrain().getMemory(HIVE_POS).isPresent()) {
            bee.getBrain().setMemory(TRAVELLING_TICKS, +1);
            if (bee.getBrain().getMemory(TRAVELLING_TICKS).isPresent() && bee.getBrain().getMemory(TRAVELLING_TICKS).get() > 600) {
                this.dropAndBlacklistHive(bee);
            } else if (!bee.getNavigation().isInProgress()) {
                    this.pathfindDirectlyTowards(bee.getBrain().getMemory(HIVE_POS).get(), bee);
                    }
                } else {
                    boolean bl = this.pathfindDirectlyTowards(bee.getBrain().getMemory(HIVE_POS).get(), bee);
                    if (!bl) {
                        this.dropAndBlacklistHive(bee);
                    } else if (bee.getBrain().getMemory(LAST_PATH).isPresent() && bee.getNavigation().getPath().sameAs(bee.getBrain().getMemory(LAST_PATH).get())) {
                        bee.getBrain().setMemory(STUCK_TICKS, +1);
                        if (bee.getBrain().getMemory(STUCK_TICKS).isPresent() && bee.getBrain().getMemory(STUCK_TICKS).get() > 600) {
                            this.dropHive(bee);
                            bee.getBrain().eraseMemory(STUCK_TICKS);
                        }
                    } else {
                        bee.getBrain().setMemory(LAST_PATH, bee.getNavigation().getPath());
                    }

                }
    }

    private boolean pathfindDirectlyTowards(BlockPos blockPos, Bee bee) {
        bee.getNavigation().setMaxVisitedNodesMultiplier(10.0F);
        bee.getNavigation().moveTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 1.0);
        return bee.getNavigation().getPath() != null && bee.getNavigation().getPath().canReach();
    }


    private void dropAndBlacklistHive(Bee bee) {
        if (bee.getBrain().getMemory(HIVE_POS).isPresent()) {
            this.blacklistTarget(bee, bee.getBrain().getMemory(HIVE_POS).get());
        }

        this.dropHive(bee);
    }

    private void dropHive(Bee bee) {
        bee.getBrain().eraseMemory(HIVE_POS);
        ((BeeAccessor)bee).setHivePos(null);
        bee.getBrain().setMemory(COOLDOWN_LOCATE_HIVE, 200);
    }


    private void blacklistTarget(Bee bee, BlockPos blockPos) {
        if (bee.getBrain().getMemory(HIVE_BLACKLIST).isEmpty()) {
            List<BlockPos> list = Lists.<BlockPos>newArrayList();
            bee.getBrain().setMemory(HIVE_BLACKLIST, list);
        } else {
            bee.getBrain().getMemory(HIVE_BLACKLIST).get().add(blockPos);
            while(bee.getBrain().getMemory(HIVE_BLACKLIST).get().size() > 3) {
                bee.getBrain().getMemory(HIVE_BLACKLIST).get().remove(0);
            }
        }
    }
}
