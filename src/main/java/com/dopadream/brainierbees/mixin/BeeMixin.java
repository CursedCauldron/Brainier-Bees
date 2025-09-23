package com.dopadream.brainierbees.mixin;


import com.dopadream.brainierbees.ai.BeeAi;
import com.dopadream.brainierbees.registry.ModMemoryTypes;
import com.dopadream.brainierbees.registry.ModSensorTypes;
import com.dopadream.brainierbees.util.HiveAccessor;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Bee.class)
public abstract class BeeMixin extends Animal implements HiveAccessor {

    @Unique
    private static final ImmutableList<SensorType<? extends Sensor<? super Bee>>> SENSOR_TYPES;
    @Unique
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;

    @Unique
    private BlockPos brainier_bees$memorizedHome;

    @Override
    public BlockPos brainier_bees$getMemorizedHome() {
        return this.brainier_bees$memorizedHome;
    }

    @Override
    public void brainier_bees$setMemorizedHome(BlockPos pos) {
        this.brainier_bees$memorizedHome = pos;
    }

    @Unique
    public int brainier_bees$HoneyCooldown;

    public BeeMixin(EntityType<? extends Bee> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow private @Nullable UUID persistentAngerTarget;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void Bee(EntityType<? extends Bee> entityType, Level level, CallbackInfo ci) {
        this.setPathfindingMalus(PathType.DANGER_FIRE, 8.0F);
        this.setPathfindingMalus(PathType.TRAPDOOR, 8.0F);
        this.setPathfindingMalus(PathType.WATER, -3.0F);
    }

    @Inject(method = "registerGoals", at = @At("RETURN"))
    public void killGoals(CallbackInfo ci) {
        this.removeAllGoals(goal -> true);
    }

    @Inject(method = "customServerAiStep", at = @At("RETURN"))
    public void aiStep(CallbackInfo ci) {
        Bee $this = (Bee) (Object) this;
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("beeBrain");
        this.getBrain().tick((ServerLevel) $this.level(), $this);
        profilerFiller.pop();
        profilerFiller.push("beeActivityUpdate");
        BeeAi.updateActivity($this);
        profilerFiller.pop();

        if (this.persistentAngerTarget != null) {
            getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, getTarget());
        }

        if (getBrain().getMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE).isPresent()) {
            if (getBrain().getMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE).get() > 0 ) {
                getBrain().setMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE, getBrain().getMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE).get() - 1);
            } else {
                getBrain().eraseMemory(ModMemoryTypes.COOLDOWN_LOCATE_HIVE);
            }
        }

        if ((this.brainier_bees$getMemorizedHome() == null) && (getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()) ) {
            this.brainier_bees$setMemorizedHome(getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
        }

        if(this.brainier_bees$getMemorizedHome() != null) {
            getBrain().setMemory(ModMemoryTypes.HIVE_POS, new GlobalPos(level().dimension(), this.brainier_bees$getMemorizedHome()));
        }

        if (getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()) {
            if (!(level().getBlockEntity(getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos()) instanceof BeehiveBlockEntity)) {
                if (getBrain().getMemory(ModMemoryTypes.HIVE_POS).isPresent()) {
                    this.removeMemorizedHive($this);
                }
            }
            if (brainier_bees$newHiveNearFire()) {
                this.removeMemorizedHive($this);
            }
        }


        if (brainier_bees$newWantsHive()) {
            $this.getBrain().setMemory(ModMemoryTypes.WANTS_HIVE, true);
        } else {
            $this.getBrain().eraseMemory(ModMemoryTypes.WANTS_HIVE);
        }
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }

    @Unique
    public boolean brainier_bees$newWantsHive() {
        Bee bee = (Bee) (Object) this;
        if (((BeeAccessor)bee).getStayOutOfHiveCountdown() <= 0 && !bee.hasStung() && bee.getTarget() == null) {
            boolean bl = level().isRaining() || level().isMoonVisible() || bee.hasNectar();
            return bl && !this.brainier_bees$newHiveNearFire();
        } else {
            return false;
        }
    }

    @Unique
    private boolean brainier_bees$newHiveNearFire() {
        Bee bee = (Bee) (Object) this;
        if (bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = level().getBlockEntity(bee.getBrain().getMemory(ModMemoryTypes.HIVE_POS).get().pos());
            return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)blockEntity).isFireNearby();
        }
    }


    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    public void addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo ci) {
        valueOutput.putInt("HoneyCooldown", this.brainier_bees$HoneyCooldown);
        if (this.brainier_bees$getMemorizedHome() != null) {
            valueOutput.storeNullable("MemorizedHome", BlockPos.CODEC, this.brainier_bees$getMemorizedHome());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci) {
        if (valueInput.getInt("HoneyCooldown").isPresent()) {
            this.brainier_bees$HoneyCooldown = valueInput.getInt("HoneyCooldown").get();
        }
    }

    @Inject(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/Bee;", at = @At("HEAD"))
    public void getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob, CallbackInfoReturnable<Bee> cir) {
        if (ageableMob != null) {
            BeeAi.initMemories((Bee) ageableMob, ageableMob.getRandom());
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return levelReader.getBlockState(blockPos).isAir() ? 10.0f : 0.0f;
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        Bee $this = (Bee) (Object) this;
        RandomSource randomSource = serverLevelAccessor.getRandom();
        BeeAi.initMemories($this, randomSource);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, spawnReason, spawnGroupData);
    }

    public Brain.@NotNull Provider<Bee> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        return BeeAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public @NotNull Brain<Bee> getBrain() {
        return (Brain<Bee>) super.getBrain();
    }

    static {
        SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
                SensorType.NEAREST_PLAYERS,
                SensorType.HURT_BY,
                SensorType.NEAREST_ADULT,
                SensorType.NEAREST_LIVING_ENTITIES,
                ModSensorTypes.BEE_TEMPTATIONS
        );
        MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH,
                MemoryModuleType.BREED_TARGET,
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.PATH,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                MemoryModuleType.NEAREST_VISIBLE_ADULT,
                MemoryModuleType.HURT_BY_ENTITY,
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.TEMPTING_PLAYER,
                MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                MemoryModuleType.IS_TEMPTED,
                ModMemoryTypes.FLOWER_POS,
                ModMemoryTypes.HIVE_POS,
                ModMemoryTypes.LAST_PATH,
                ModMemoryTypes.HIVE_BLACKLIST,
                ModMemoryTypes.POLLINATING_COOLDOWN,
                ModMemoryTypes.POLLINATING_TICKS,
                ModMemoryTypes.SUCCESSFUL_POLLINATING_TICKS,
                ModMemoryTypes.COOLDOWN_LOCATE_HIVE,
                ModMemoryTypes.TRAVELLING_TICKS,
                ModMemoryTypes.STUCK_TICKS,
                ModMemoryTypes.WANTS_HIVE,
                MemoryModuleType.IS_PANICKING);
    }
}
