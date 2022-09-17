package cursedcauldron.brainierbees.ai.tasks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.FlyingRandomStroll;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BeeWanderTask extends FlyingRandomStroll {
    public BeeWanderTask(float f) {
        super(f);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob bee) {
        return bee.getNavigation().isDone() && bee.getRandom().nextInt(10) == 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob bee, long l) {
        return bee.getNavigation().isInProgress();
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob bee, long l) {
        Vec3 vec3 = this.findPos((Bee) bee);
        if (vec3 != null) {
            bee.getNavigation().moveTo(bee.getNavigation().createPath(new BlockPos(vec3), 1), 1.0D);
        }

    }

    boolean closerThan(Bee bee, BlockPos blockPos, int i) {
        return blockPos.closerThan(bee.blockPosition(), (double)i);
    }

    @Nullable
    private Vec3 findPos(Bee bee) {
        Vec3 vec32;
        if (bee.hasHive() && !this.closerThan(bee, bee.getHivePos(), 22)) {
            Vec3 vec3 = Vec3.atCenterOf(bee.getHivePos());
            vec32 = vec3.subtract(bee.position()).normalize();
        } else {
            vec32 = bee.getViewVector(0.0F);
        }

        Vec3 vec33 = HoverRandomPos.getPos(bee, 8, 7, vec32.x, vec32.z, 1.5707964F, 3, 1);
        return vec33 != null ? vec33 : AirAndWaterRandomPos.getPos(bee, 8, 4, -2, vec32.x, vec32.z, 1.5707963705062866D);
    }
}
