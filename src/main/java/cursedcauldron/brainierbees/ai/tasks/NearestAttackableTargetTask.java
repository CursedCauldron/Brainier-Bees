package cursedcauldron.brainierbees.ai.tasks;

import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;

import java.util.Map;

public class NearestAttackableTargetTask extends Behavior<Bee> {
    public NearestAttackableTargetTask(Map<MemoryModuleType<?>, MemoryStatus> map) {
        super(map);
    }
}
