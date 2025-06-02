package io.github.dopadream.ai.tasks;

import io.github.dopadream.util.HiveAccessor;
import io.github.dopadream.registry.BrainierBeesRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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

public class LocateHiveTask extends Behavior<Bee> {

    private ServerLevel level;

    public LocateHiveTask() {
        super(Map.of(BrainierBeesRegistries.LOCATE_HIVE_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee bee) {
        return bee.getBrain().getMemory(BrainierBeesRegistries.LOCATE_HIVE_COOLDOWN.get()).isEmpty() && bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isEmpty();
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee livingEntity, long l) {
        return false;
    }


    @Override
    protected void start(ServerLevel level, Bee bee, long l) {
        bee.getBrain().setMemory(BrainierBeesRegistries.LOCATE_HIVE_COOLDOWN.get(), 200);
        this.level = level;
        List<BlockPos> list = this.findNearbyHivesWithSpace(level, bee);
        if (!list.isEmpty()) {
            for(BlockPos blockPos : list) {
                if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get()).isEmpty() || !bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get()).get().contains(GlobalPos.of(bee.level().dimension(), blockPos))) {
                    bee.getBrain().setMemory(BrainierBeesRegistries.HIVE_POS.get(), GlobalPos.of(level.dimension(), blockPos));
//                    ((BeeAccessor)bee).setHivePos(blockPos);
                    ((HiveAccessor)bee).brainier_bees$setMemorizedHome(blockPos);
                    return;
                }
            }
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
