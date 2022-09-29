package cursedcauldron.brainierbees.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import cursedcauldron.brainierbees.ai.tasks.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Predicate;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.*;

public class BeeBrain {
    private static final UniformInt TIME_BETWEEN_POLLINATING = UniformInt.of(10, 15);


    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(3, 16);

    public BeeBrain() {
    }

    public static void initMemories(Bee bee, RandomSource randomSource) {
        bee.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, TIME_BETWEEN_POLLINATING.sample(randomSource));

    }

    public static Brain<?> makeBrain(Brain<Bee> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initStingActivity(brain);
        initPollinateActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }



    private static void initStingActivity(Brain<Bee> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.FIGHT,
                0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(),
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1F),
                        new BeeAttackTask(20),
                        new EraseMemoryIf<>(Predicate.not(Bee::isAngry), MemoryModuleType.ATTACK_TARGET)
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }



    private static void initPollinateActivity(Brain<Bee> brain) {
        brain.addActivityWithConditions(
                Activity.CELEBRATE,
                ImmutableList.of(
                        Pair.of(0, new BabyFollowAdult<>(ADULT_FOLLOW_RANGE, 1.25F)),
                        Pair.of(0, new FindFlowerTask()),
                        Pair.of(1, new PollinateFlowerTask()),
                        Pair.of(2, new EraseMemoryIf<>(Bee::hasNectar, ModMemoryTypes.FLOWER_POS)
                        )
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(ModMemoryTypes.WANTS_HIVE, MemoryStatus.VALUE_ABSENT),
                        Pair.of(ModMemoryTypes.POLLINATING_COOLDOWN, MemoryStatus.VALUE_ABSENT)
                )
        );
    }




    private static void initCoreActivity(Brain<Bee> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new MoveToTargetSink(),
                new CountDownCooldownTicks(ModMemoryTypes.POLLINATING_COOLDOWN),
                new CountDownCooldownTicks(COOLDOWN_LOCATE_HIVE),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));

    }


    private static void initIdleActivity(Brain<Bee> brain) {
        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(9, new FloatTask()),

                        Pair.of(9, new GrowCropTask()),
                        Pair.of(2, new AnimalMakeLove(EntityType.BEE, 1.0F)),
                        Pair.of(3, new FollowTemptation(livingEntity -> 1.25F)),
                        Pair.of(0, new BabyFollowAdult<>(ADULT_FOLLOW_RANGE, 1.25F)),
                        Pair.of(5, new LocateHiveTask()),
                        Pair.of(0, new GoToHiveTask()),
                        Pair.of(1, new EnterHiveTask()),
                        Pair.of(9, new BeeWanderTask(0.6F)
                        )
                ),
                ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT))
        );
    }



    public static void updateActivity(Bee bee) {
        bee.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT,  Activity.CELEBRATE, Activity.IDLE));
    }


    public static Ingredient getTemptations() {
        return Ingredient.of(ItemTags.FLOWERS);
    }
}
