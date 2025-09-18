package com.dopadream.brainierbees.util;

import com.dopadream.brainierbees.registry.ModMemoryTypes;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.animal.Bee;

import java.util.LinkedList;
import java.util.List;

public interface HiveAccessor {


    BlockPos brainier_bees$getMemorizedHome();

    void brainier_bees$setMemorizedHome(BlockPos pos);

    private void dropHive(Bee bee) {
        bee.getBrain().setMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE, 200);
    }

    default void dropAndBlacklistHive(Bee bee) {
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()) {
            ((HiveAccessor)bee).removeMemorizedHive(bee);
        }
        ((HiveAccessor)bee).dropHive(bee);
    }

    default void blacklistTarget(Bee bee, BlockPos blockPos) {
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_BLACKLIST).isEmpty()) {
            LinkedList<GlobalPos> list = Lists.newLinkedList();
            list.add(GlobalPos.of(bee.level().dimension(), blockPos));
            bee.getBrain().setMemory(ModMemoryTypes.HIVE_BLACKLIST, list);
        } else {
            bee.getBrain().getMemory(ModMemoryTypes.HIVE_BLACKLIST).get().add(GlobalPos.of(bee.level().dimension(), blockPos));
            while(bee.getBrain().getMemory(ModMemoryTypes.HIVE_BLACKLIST).get().size() > 3) {
                bee.getBrain().getMemory(ModMemoryTypes.HIVE_BLACKLIST).get().removeFirst();
            }
        }
    }
    default void removeMemorizedHive(Bee bee) {
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_BLACKLIST).isPresent()) {
            bee.getBrain().eraseMemory(ModMemoryTypes.HIVE_POS);
            ((HiveAccessor)bee).brainier_bees$setMemorizedHome(null);
        } else {
            ((HiveAccessor)bee).blacklistTarget(bee, bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
        }
    }
}
