package io.github.dopadream.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import io.github.dopadream.ai.tasks.*;
import io.github.dopadream.registry.BrainierBeesRegistries;
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
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class BeeAi {
    private static final UniformInt TIME_BETWEEN_POLLINATING = UniformInt.of(10, 15);
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(3, 16);

    public BeeAi() {
    }

    public static void initMemories(Bee bee, RandomSource randomSource) {
        bee.getBrain().setMemory(BrainierBeesRegistries.POLLINATING_COOLDOWN.get(), TIME_BETWEEN_POLLINATING.sample(randomSource));
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
                        StopAttackingIfTargetInvalid.create(),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1F),
                        MeleeAttack.create(20),
                        EraseMemoryIf.create(Predicate.not(Bee::isAngry), MemoryModuleType.ATTACK_TARGET),
                        EraseMemoryIf.create(Bee::hasStung, MemoryModuleType.ATTACK_TARGET)
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }



    private static void initPollinateActivity(Brain<Bee> brain) {
        brain.addActivityWithConditions(
                Activity.CELEBRATE,
                ImmutableList.of(
                        Pair.of(0, BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25F)),
                        Pair.of(0, new FindFlowerTask()),
                        Pair.of(1, new PollinateFlowerTask()),
                        Pair.of(2, EraseMemoryIf.create(Bee::hasNectar, BrainierBeesRegistries.FLOWER_POS.get())),
                        Pair.of(3, EraseMemoryIf.create(Bee::wantsToEnterHive, BrainierBeesRegistries.FLOWER_POS.get()))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(BrainierBeesRegistries.WANTS_HIVE.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(BrainierBeesRegistries.POLLINATING_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    private static void initCoreActivity(Brain<Bee> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new MoveToTargetSink(),
                new CountDownCooldownTicks(BrainierBeesRegistries.POLLINATING_COOLDOWN.get()),
                new CountDownCooldownTicks(BrainierBeesRegistries.LOCATE_HIVE_COOLDOWN.get()),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));

    }


    private static void initIdleActivity(Brain<Bee> brain) {
        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, new FloatTask()),
                        Pair.of(1, new GoToHiveTask()),
                        Pair.of(2, new EnterHiveTask()),
                        Pair.of(3, new FollowTemptation(livingEntity -> 0.6F)),
                        Pair.of(4, BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25F)),
                        Pair.of(5, new AnimalMakeLove(EntityType.BEE)),
                        Pair.of(6, new LocateHiveTask()),
                        Pair.of(7, new GrowCropTask()),
                        Pair.of(
                                8,
                                new RunOne(
                                        ImmutableList.of(
                                                Pair.of(new BeePathfinding(), 1))
                                )
                        )
                ),
                ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT))
        );
    }



    public static void updateActivity(Bee bee) {
        bee.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT,  Activity.CELEBRATE, Activity.IDLE));
    }


    public static Predicate<ItemStack> getTemptations() {
        return itemStack -> itemStack.is(ItemTags.BEE_FOOD);
    }
}
