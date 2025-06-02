package io.github.dopadream.ai.tasks;

import io.github.dopadream.mixin.BeeAccessor;
import io.github.dopadream.registry.BrainierBeesRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;

public class EnterHiveTask extends Behavior<Bee> {


    public EnterHiveTask() {
        super(Map.of(BrainierBeesRegistries.HIVE_POS.get(), MemoryStatus.VALUE_PRESENT));
    }

    public boolean wantsToEnterHive(ServerLevel level, Bee bee) {
        if (((BeeAccessor)bee).getStayOutOfHiveCountdown() <= 0 && !bee.hasStung() && bee.getTarget() == null) {
            boolean bl = level.isRaining() || level.isMoonVisible() || bee.hasNectar();
            return bl && !this.isHiveNearFire(level, bee);
        } else {
            return false;
        }
    }

    private boolean isHiveNearFire(ServerLevel level, Bee bee) {
        if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos());
            return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)blockEntity).isFireNearby();
        }
    }


    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee bee) {
        if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isPresent() && this.wantsToEnterHive(serverLevel, bee) && bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos().closerToCenterThan(bee.position(), 2.0)) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos());
            if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
                if (!beehiveBlockEntity.isFull()) {
                    return true;
                }

                bee.getBrain().eraseMemory(BrainierBeesRegistries.HIVE_POS.get());
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel serverLevel, Bee bee, long l) {
        if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isPresent()) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos());
            if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
                beehiveBlockEntity.addOccupant(bee);
            }
        }
        super.start(serverLevel, bee, l);
    }
}
