package com.dopadream.brainierbees.ai.tasks;

import com.dopadream.brainierbees.ai.ModMemoryTypes;
import com.dopadream.brainierbees.mixin.BeeAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;

public class EnterHiveTask extends Behavior<Bee> {


    public EnterHiveTask() {
        super(Map.of(ModMemoryTypes.HIVE_POS, MemoryStatus.VALUE_PRESENT));
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
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
            return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)blockEntity).isFireNearby();
        }
    }


    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee bee) {
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent() && this.wantsToEnterHive(serverLevel, bee) && bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos().closerToCenterThan(bee.position(), 2.0)) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
            if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
                if (!beehiveBlockEntity.isFull()) {
                    return true;
                }

                bee.getBrain().eraseMemory(ModMemoryTypes.HIVE_POS);
            }
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee livingEntity, long l) {
        return false;
    }



    @Override
    protected void start(ServerLevel serverLevel, Bee bee, long l) {
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
            if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
                beehiveBlockEntity.addOccupant(bee, bee.hasNectar());
            }
        }
        super.start(serverLevel, bee, l);
    }
}
