package cursedcauldron.brainierbees.util;

import com.google.common.collect.Lists;
import cursedcauldron.brainierbees.mixin.BeeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.animal.Bee;

import java.util.List;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.*;

public interface HiveAccessor {

    BlockPos getMemorizedHome();

    void setMemorizedHome(BlockPos pos);

    private void dropHive(Bee bee) {
        bee.getBrain().setMemory(COOLDOWN_LOCATE_HIVE, 200);
    }

    default void dropAndBlacklistHive(Bee bee) {
        if (bee.getBrain().getMemory(HIVE_POS).isPresent()) {
            ((HiveAccessor)bee).removeMemorizedHive(bee);
//            System.out.println("hello??!");
        }
        ((HiveAccessor)bee).dropHive(bee);
//        System.out.println("goobye");
    }

    default void blacklistTarget(Bee bee, BlockPos blockPos) {
        if (bee.getBrain().getMemory(HIVE_BLACKLIST).isEmpty()) {
            List<GlobalPos> list = Lists.newArrayList();
            list.add(GlobalPos.of(bee.level().dimension(), blockPos));
            bee.getBrain().setMemory(HIVE_BLACKLIST, list);
//            System.out.println("nvm");
        } else {
            bee.getBrain().getMemory(HIVE_BLACKLIST).get().add(GlobalPos.of(bee.level().dimension(), blockPos));
            while(bee.getBrain().getMemory(HIVE_BLACKLIST).get().size() > 3) {
                bee.getBrain().getMemory(HIVE_BLACKLIST).get().remove(0);
            }
//            System.out.println("almost works");
        }
    }
    default void removeMemorizedHive(Bee bee) {
        if (bee.getBrain().getMemory(HIVE_BLACKLIST).isPresent()) {
            bee.getBrain().eraseMemory(HIVE_POS);
            ((HiveAccessor)bee).setMemorizedHome(null);
//            System.out.println("FUCK!");
        } else {
            ((HiveAccessor)bee).blacklistTarget(bee, bee.getBrain().getMemory(HIVE_POS).get().pos());
        }
    }
}
