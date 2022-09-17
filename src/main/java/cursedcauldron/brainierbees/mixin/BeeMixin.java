package cursedcauldron.brainierbees.mixin;


import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import cursedcauldron.brainierbees.ai.BeeBrain;
import cursedcauldron.brainierbees.ai.ModSensorTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Mixin(Bee.class)
public abstract class BeeMixin extends Animal {

    private static final ImmutableList<SensorType<? extends Sensor<? super Bee>>> SENSOR_TYPES;
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;

    public BeeMixin(EntityType<? extends Bee> entityType, Level level) {
        super(entityType, level);
    }


    private void beeMixinType(EntityType<? extends LivingEntity> entityType, Level level) {
    }

    @Shadow public abstract GoalSelector getGoalSelector();

    @Shadow private @Nullable UUID persistentAngerTarget;

    @Shadow public @Nullable abstract UUID getPersistentAngerTarget();

    @Inject(method = "registerGoals", at = @At("RETURN"))
    public void killGoals(CallbackInfo ci) {
        this.getGoalSelector().removeAllGoals();
    }

    @Inject(method = "customServerAiStep", at = @At("RETURN"))
    public void aiStep(CallbackInfo ci) {
        Bee $this = (Bee) (Object) this;
        $this.level.getProfiler().push("beeBrain");
        this.getBrain().tick((ServerLevel) $this.level, $this);
        $this.level.getProfiler().pop();
        $this.level.getProfiler().push("beeActivityUpdate");
        BeeBrain.updateActivity($this);
        $this.level.getProfiler().pop();
        if (this.persistentAngerTarget != null) {
            getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, getTarget());
        }
    }



    @Override
    public Brain.Provider<Bee> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    public Brain<Bee> makeBrain(Dynamic<?> dynamic) {
        return (Brain<Bee>) BeeBrain.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (Objects.equals(damageSource, DamageSource.mobAttack(getLastHurtByMob()))) {
            getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, getLastHurtByMob());
        } else if (Objects.equals(damageSource, DamageSource.playerAttack(lastHurtByPlayer))) {
            getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, lastHurtByPlayer);

        }
        return super.hurt(damageSource, f);
    }

    @Override
    public Brain<Bee> getBrain() {
        return (Brain<Bee>) super.getBrain();
    }

    static {

        SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
                SensorType.NEAREST_PLAYERS,
                SensorType.HURT_BY,
                ModSensorTypes.BEE_TEMPTATIONS);
        MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.HURT_BY,
                MemoryModuleType.BREED_TARGET,
                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
                MemoryModuleType.LIKED_PLAYER,
                MemoryModuleType.IS_TEMPTED,
                MemoryModuleType.TEMPTING_PLAYER,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                MemoryModuleType.IS_PANICKING);
    }
}
