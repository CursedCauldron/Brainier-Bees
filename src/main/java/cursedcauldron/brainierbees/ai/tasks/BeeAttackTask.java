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
        super(60);
        this.cooldownBetweenAttacks = i;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        if (mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            LivingEntity livingEntity = getAttackTarget(mob);
            Bee bee = (Bee) mob;
            return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent() && bee.isAngry() && !bee.hasStung() && BehaviorUtils.canSee(mob, livingEntity) && mob.isWithinMeleeAttackRange(livingEntity);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        if (mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            LivingEntity livingEntity = getAttackTarget(mob);
            Bee bee = (Bee) mob;
            return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent() && bee.isAngry() && !bee.hasStung() && BehaviorUtils.canSee(mob, livingEntity) && mob.isWithinMeleeAttackRange(livingEntity);
        } else {
            return false;
        }
    }


    private LivingEntity getAttackTarget(Mob mob) {
        return (LivingEntity)mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

}
