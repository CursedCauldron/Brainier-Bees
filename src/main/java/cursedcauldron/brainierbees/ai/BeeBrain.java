package cursedcauldron.brainierbees.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import cursedcauldron.brainierbees.ai.tasks.BeeAttackTask;
import cursedcauldron.brainierbees.ai.tasks.BeeWanderTask;
import cursedcauldron.brainierbees.ai.tasks.FloatTask;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.frog.FrogAi;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public class BeeBrain {

    public BeeBrain() {
    }

    public static Brain<?> makeBrain(Brain<Bee> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initStingActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }


    private static void initCoreActivity(Brain<Bee> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(0, 0), new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    public static void initIdleActivity(Brain<Bee> brain) {
        brain.addActivity(Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, new FollowTemptation(livingEntity -> 1.25F)),
                        Pair.of(1, new AnimalMakeLove(EntityType.BEE, 1.0F)),
                        Pair.of(3, new GateBehavior<>(
                                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableSet.of(),
                                GateBehavior.OrderPolicy.ORDERED,
                                GateBehavior.RunningPolicy.TRY_ALL,
                                ImmutableList.of(
                                        Pair.of(new BeeWanderTask(0.4F), 2),
                                        Pair.of(new FloatTask(), 2)
                                        )))
                ));
    }

    public static void initStingActivity(Brain<Bee> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT,10,
                ImmutableList.of(new GateBehavior<>(
                                ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT),
                                ImmutableSet.of(),
                                GateBehavior.OrderPolicy.ORDERED,
                                GateBehavior.RunningPolicy.TRY_ALL,
                                ImmutableList.of(
                                        Pair.of(new StartAttacking<>(Bee::isAngry, bee -> bee.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE)), 2),
                                        Pair.of(new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F), 2),
                                        Pair.of(new BeeAttackTask(0.4F), 2),
                                        Pair.of(new FloatTask(), 2)
                                ))
                ), MemoryModuleType.ATTACK_TARGET);
    }


    public static void updateActivity(Bee bee) {
        bee.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }



    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Bee bee) {
        Brain<Bee> brain = (Brain<Bee>) bee.getBrain();
            Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(bee, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(bee, optional.get())) {
                return optional;
            } else {
                Optional optional2;
                if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                    optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                    if (optional2.isPresent()) {
                        return optional2;
                    }
                }
                return Optional.empty();
            }
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(ItemTags.FLOWERS);
    }


}
