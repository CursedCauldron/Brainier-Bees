package cursedcauldron.brainierbees.ai.tasks;

import cursedcauldron.brainierbees.ai.ModMemoryTypes;
import cursedcauldron.brainierbees.mixin.BeeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.*;

public class LocateHiveTask extends Behavior<Bee> {

    private ServerLevel level;

    public LocateHiveTask() {
        super(Map.of(ModMemoryTypes.COOLDOWN_LOCATE_HIVE, MemoryStatus.VALUE_ABSENT));
    }

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
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee bee) {
        return bee.getBrain().getMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE).isEmpty() && bee.getBrain().getMemory(HIVE_POS).isEmpty();
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee livingEntity, long l) {
        return false;
    }


    @Override
    protected void start(ServerLevel level, Bee bee, long l) {
        bee.getBrain().setMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE, 200);
        this.level = level;
        List<BlockPos> list = this.findNearbyHivesWithSpace(level, bee);
        if (!list.isEmpty()) {
            for(BlockPos blockPos : list) {
                if (bee.getBrain().getMemory(HIVE_BLACKLIST).isEmpty() || !bee.getBrain().getMemory(HIVE_BLACKLIST).get().contains(blockPos)) {
                    bee.getBrain().setMemory(HIVE_POS, blockPos);
                    ((BeeAccessor)bee).setHivePos(blockPos);
                    return;
                }
            }

            if (bee.getBrain().getMemory(HIVE_BLACKLIST).isPresent()) {
                bee.getBrain().getMemory(HIVE_BLACKLIST).get().clear();
            }
            bee.getBrain().setMemory(HIVE_POS, list.get(0));
            ((BeeAccessor)bee).setHivePos(list.get(0));
        }
    }

    private boolean doesHiveHaveSpace(BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BeehiveBlockEntity) {
            return !((BeehiveBlockEntity)blockEntity).isFull();
        } else {
            return false;
        }
    }

    private List<BlockPos> findNearbyHivesWithSpace(ServerLevel level, Bee bee) {
        BlockPos blockPos = bee.blockPosition();
        PoiManager poiManager = level.getPoiManager();
        Stream<PoiRecord> stream = poiManager.getInRange(holder -> holder.is(PoiTypeTags.BEE_HOME), blockPos, 20, PoiManager.Occupancy.ANY);
        return stream.map(PoiRecord::getPos)
                .filter(this::doesHiveHaveSpace)
                .sorted(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos)))
                .collect(Collectors.toList());
    }
}
