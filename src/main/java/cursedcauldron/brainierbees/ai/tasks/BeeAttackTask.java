package cursedcauldron.brainierbees.ai.tasks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Bee;


public class BeeAttackTask extends MeleeAttack {

    private final float cooldownBetweenAttacks;

    public BeeAttackTask(float i) {
        super();
        this.cooldownBetweenAttacks = i;
    }

    private LivingEntity getAttackTarget(Mob mob) {
        return (LivingEntity)mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

}
