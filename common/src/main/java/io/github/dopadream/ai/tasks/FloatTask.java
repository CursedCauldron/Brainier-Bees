package io.github.dopadream.ai.tasks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;

import java.util.Map;

public class FloatTask extends Behavior<Bee> {


    public FloatTask() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }


    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee bee) {
        return bee.isInWater() && bee.getFluidHeight(FluidTags.WATER) > bee.getFluidJumpThreshold() || bee.isInLava();
    }

    @Override
    protected void start(ServerLevel serverLevel, Bee bee, long l) {
        bee.getJumpControl().jump();
    }
}
