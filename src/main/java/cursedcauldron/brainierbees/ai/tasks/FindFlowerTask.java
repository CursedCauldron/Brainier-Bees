package cursedcauldron.brainierbees.ai.tasks;

import com.google.common.collect.Lists;
import cursedcauldron.brainierbees.ai.ModMemoryTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.pathfinder.Path;

import java.util.List;
import java.util.Map;

public class FindFlowerTask extends Behavior<Bee> {

    private BlockPos flowerPosPublic;


    public FindFlowerTask() {
        super(Map.of(ModMemoryTypes.POLLINATING_COOLDOWN, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel world, Bee entity) {
        return !entity.hasNectar() || (entity.getBrain().getMemory(ModMemoryTypes.FLOWER_POS).isEmpty() && entity.getBrain().getMemory(ModMemoryTypes.POLLINATING_COOLDOWN).isEmpty());
    }

    @Override
    protected boolean canStillUse(ServerLevel world, Bee entity, long l) {
        return !entity.hasNectar() || (entity.getBrain().getMemory(ModMemoryTypes.FLOWER_POS).isEmpty() && entity.getBrain().getMemory(ModMemoryTypes.POLLINATING_COOLDOWN).isEmpty());
    }

    public BlockPos getFlowerPos(Bee entity, ServerLevel level) {
        int radius = 5;
        List<BlockPos> possibles = Lists.newArrayList();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius; y <= radius; y++) {
                    BlockPos pos = new BlockPos(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
                    if (entity.level.getBlockState(pos).is(BlockTags.FLOWERS)) {
                        possibles.add(pos);
                    }
                }
            }
        }
        if (possibles.isEmpty()) {
            entity.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, UniformInt.of(120, 240).sample(level.getRandom()));
            return null;
        } else {
            return possibles.get(entity.getRandom().nextInt(possibles.size()));
        }
    }

    @Override
    protected void start(ServerLevel level, Bee entity, long l) {
        BlockPos flowerPos = this.getFlowerPos(entity, level);
        if (flowerPos != null && entity.getBrain().getMemory(ModMemoryTypes.FLOWER_POS).isEmpty()) {
            this.flowerPosPublic = flowerPos;
        }
    }

    @Override
    protected void tick(ServerLevel level, Bee entity, long l) {
        if (this.flowerPosPublic != null) {
            BlockPos flowerPos = this.flowerPosPublic;
            BehaviorUtils.setWalkAndLookTargetMemories(entity, flowerPos, 0.4F, 1);
            Path flower = entity.getNavigation().createPath(flowerPos, 1);
            if (flower != null && flower.canReach()) {
                entity.getNavigation().moveTo(flower, 0.6);
                if (entity.blockPosition().closerThan(flowerPos, 2) && entity.level.getBlockState(flowerPos).is(BlockTags.FLOWERS)) {
                    entity.getBrain().setMemory(ModMemoryTypes.FLOWER_POS, flowerPos);
                    this.flowerPosPublic = flowerPos;
                }
            } else {
                entity.getBrain().eraseMemory(ModMemoryTypes.FLOWER_POS);
                entity.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, UniformInt.of(120, 240).sample(level.getRandom()));
            }
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, Bee livingEntity, long l) {
        super.stop(serverLevel, livingEntity, l);
        if (livingEntity.getBrain().getMemory(ModMemoryTypes.POLLINATING_COOLDOWN).isEmpty() && livingEntity.hasNectar()) {
            livingEntity.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, 100);
        }
    }
}
