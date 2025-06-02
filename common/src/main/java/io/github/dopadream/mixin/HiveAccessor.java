package io.github.dopadream.mixin;

import com.google.common.collect.Lists;
import io.github.dopadream.registry.BrainierBeesRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.animal.Bee;

import java.util.List;

public interface HiveAccessor {

    BlockPos brainier_bees$getMemorizedHome();

    void brainier_bees$setMemorizedHome(BlockPos pos);

    private void dropHive(Bee bee) {
        bee.getBrain().setMemory(BrainierBeesRegistries.LOCATE_HIVE_COOLDOWN.get(), 200);
    }

    default void dropAndBlacklistHive(Bee bee) {
        if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).isPresent()) {
            ((HiveAccessor)bee).removeMemorizedHive(bee);
//            System.out.println("hello??!");
        }
        ((HiveAccessor)bee).dropHive(bee);
//        System.out.println("goobye");
    }

    default void blacklistTarget(Bee bee, BlockPos blockPos) {
        if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get()).isEmpty()) {
            List<GlobalPos> list = Lists.newArrayList();
            list.add(GlobalPos.of(bee.level().dimension(), blockPos));
            bee.getBrain().setMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get(), list);
//            System.out.println("nvm");
        } else {
            bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get()).get().add(GlobalPos.of(bee.level().dimension(), blockPos));
            while(bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get()).get().size() > 3) {
                bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get()).get().removeFirst();
            }
//            System.out.println("almost works");
        }
    }
    default void removeMemorizedHive(Bee bee) {
        if (bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_BLACKLIST.get()).isPresent()) {
            bee.getBrain().eraseMemory(BrainierBeesRegistries.HIVE_POS.get());
            ((HiveAccessor)bee).brainier_bees$setMemorizedHome(null);
//            System.out.println("FUCK!");
        } else {
            ((HiveAccessor)bee).blacklistTarget(bee, bee.getBrain().getMemory(BrainierBeesRegistries.HIVE_POS.get()).get().pos());
        }
    }
}