package cursedcauldron.brainierbees.ai.tasks;

import com.google.common.collect.Lists;
import com.mojang.math.Vector3f;
import cursedcauldron.brainierbees.ai.ModMemoryTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.FlyingRandomStroll;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.HIVE_POS;
import static cursedcauldron.brainierbees.ai.ModMemoryTypes.WANTS_HIVE;

public class BeeWanderTask extends FlyingRandomStroll {
    public BeeWanderTask(float f) {
        super(f);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob bee) {
        return bee.getNavigation().isDone() && bee.getRandom().nextInt(10) == 0 && (bee.getBrain().getMemory(ModMemoryTypes.POLLINATING_COOLDOWN).isPresent() || bee.getBrain().getMemory(WANTS_HIVE).isPresent());
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob bee, long l) {
        return bee.getNavigation().isInProgress() && (bee.getBrain().getMemory(ModMemoryTypes.POLLINATING_COOLDOWN).isPresent() || bee.getBrain().getMemory(WANTS_HIVE).isPresent());
    }

    @Override
    protected void tick(ServerLevel serverLevel, PathfinderMob livingEntity, long l) {
        super.tick(serverLevel, livingEntity, l);
        if (((Bee) livingEntity).hasNectar()) {
            livingEntity.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, 400);
        }
    }

    boolean closerThan(Bee bee, BlockPos blockPos, int i) {
        return blockPos.closerThan(bee.blockPosition(), (double)i);
    }

    public BlockPos findHoneyPos(PathfinderMob entity, Level level) {
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
    protected Vec3 getTargetPos(PathfinderMob bee) {
        Vec3 vec32;
        if (bee.getBrain().getMemory(HIVE_POS).isPresent() && !this.closerThan((Bee) bee, bee.getBrain().getMemory(HIVE_POS).get(), 22)) {
            Vec3 vec3 = Vec3.atCenterOf(bee.getBrain().getMemory(HIVE_POS).get());
            vec32 = vec3.subtract(bee.position()).normalize();
        } else {
            vec32 = bee.getViewVector(0.0F);
        }

        if (findHoneyPos(bee, bee.getLevel()) != null) {
            vec32 = new Vec3(new Vector3f(findHoneyPos(bee, bee.getLevel()).relative(bee.getDirection()).getX(), findHoneyPos(bee, bee.getLevel()).relative(bee.getDirection()).getY(), findHoneyPos(bee, bee.getLevel()).relative(bee.getDirection()).getZ()));
        }

        Vec3 vec33 = HoverRandomPos.getPos(bee, 8, 7, vec32.x, vec32.z, (float) (Math.PI / 2), 3, 1);
        return vec33 != null ? vec33 : AirAndWaterRandomPos.getPos(bee, 8, 4, -2, vec32.x, vec32.z, (float) (Math.PI / 2));
    }
}
