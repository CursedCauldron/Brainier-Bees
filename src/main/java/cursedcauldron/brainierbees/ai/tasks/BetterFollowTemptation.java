package cursedcauldron.brainierbees.ai.tasks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class BetterFollowTemptation extends FollowTemptation {
    public BetterFollowTemptation(Function<LivingEntity, Float> function) {
        super(function);
    }

    public BetterFollowTemptation(Function<LivingEntity, Float> function, Function<LivingEntity, Double> function2) {
        super(function, function2);
    }

    @Override
    protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        Player player = this.getTemptingPlayer(pathfinderMob).get();
        Brain<?> brain = pathfinderMob.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true));
        double d = this.closeEnoughDistance.apply(pathfinderMob);
        if ((pathfinderMob.distanceToSqr(player) < Mth.square(d)) || !(serverLevel.getBlockState(BlockPos.of(BlockPos.asLong(player.blockPosition().getX(), player.blockPosition().getY()+1, player.blockPosition().getZ()))).isAir())) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPos(player.blockPosition().getX(), player.blockPosition().getY()+2, player.blockPosition().getZ()), this.getSpeedModifier(pathfinderMob), 1));
        }
    }
}
