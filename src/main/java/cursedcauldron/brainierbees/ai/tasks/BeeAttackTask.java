package cursedcauldron.brainierbees.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;


public class BeeAttackTask extends Behavior<Bee> {

    private final float cooldownBetweenAttacks;

    public BeeAttackTask(float i) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));
        this.cooldownBetweenAttacks = i;
    }


    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee mob) {
        return mob.isAngry() && !mob.hasStung();
    }


    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee bee, long l) {
        return bee.isAngry() && !bee.hasStung();
    }


    @Override
    protected void start(ServerLevel serverLevel, Bee mob, long l) {
        LivingEntity livingEntity = this.getAttackTarget(mob);
        BehaviorUtils.lookAtEntity(mob, livingEntity);
        mob.swing(InteractionHand.MAIN_HAND);
        mob.doHurtTarget(livingEntity);
        mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)this.cooldownBetweenAttacks);
    }

    private LivingEntity getAttackTarget(Bee mob) {
        return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

}
