package com.dopadream.brainierbees.ai.tasks;

import com.dopadream.brainierbees.ai.BeeAi;
import com.dopadream.brainierbees.registry.ModMemoryTypes;
import com.dopadream.brainierbees.mixin.BeeAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PollinateFlowerTask extends Behavior<Bee> {

    private int lastSoundPlayedTick;
    @Nullable
    private Vec3 hoverPos;


    public PollinateFlowerTask() {
        super(Map.of(ModMemoryTypes.FLOWER_POS, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee bee) {
        return bee.getBrain().getMemory(ModMemoryTypes.FLOWER_POS).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.POLLINATING_COOLDOWN).isEmpty() && !(bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).get());
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee bee, long l) {
        return bee.getBrain().getMemory(ModMemoryTypes.FLOWER_POS).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.POLLINATING_COOLDOWN).isEmpty() && !(bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.WANTS_HIVE).get());
    }

    private boolean hasPollinatedLongEnough(Bee bee) {
        return bee.getBrain().getMemory(ModMemoryTypes.SUCCESSFUL_POLLINATING_TICKS).isPresent() && bee.getBrain().getMemory(ModMemoryTypes.SUCCESSFUL_POLLINATING_TICKS).get() > 400;
    }


    @Override
    protected void start(ServerLevel serverLevel, Bee bee, long l) {
        super.start(serverLevel, bee, l);
        this.lastSoundPlayedTick = 0;
        bee.resetTicksWithoutNectarSinceExitingHive();
    }


    @Override
    protected void stop(ServerLevel serverLevel, Bee bee, long l) {
        super.stop(serverLevel, bee, l);
        if (this.hasPollinatedLongEnough(bee)) {
            ((BeeAccessor)bee).invokeSetHasNectar(true);
            bee.getBrain().eraseMemory(ModMemoryTypes.FLOWER_POS);
            bee.getBrain().setMemory(ModMemoryTypes.SUCCESSFUL_POLLINATING_TICKS, 0);
            bee.getBrain().setMemory(ModMemoryTypes.POLLINATING_COOLDOWN, UniformInt.of(400, 400).sample(serverLevel.getRandom()));
        }

        bee.getNavigation().stop();
        ((BeeAccessor)bee).setRemainingCooldownBeforeLocatingNewFlower(200);
    }


    private void setWantedPos(Bee bee) {
        bee.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), 0.35F);
    }


    private float getOffset(Bee bee) {
        return (bee.getRandom().nextFloat() * 2.0F - 1.0F) * 0.33333334F;
    }


    @Override
    protected void tick(ServerLevel serverLevel, Bee bee, long l) {
        super.tick(serverLevel, bee, l);

        var brain = bee.getBrain();

        // Increment pollination ticks
        BeeAi.incrementMemory(brain, ModMemoryTypes.POLLINATING_TICKS);

        int pollinatingTicks = brain.getMemory(ModMemoryTypes.POLLINATING_TICKS).orElse(0);

        if (pollinatingTicks > 600) {
            clearFlowerMemory(brain);
            return;
        }

        var flowerPosOpt = brain.getMemory(ModMemoryTypes.FLOWER_POS);
        if (flowerPosOpt.isPresent()) {
            var flowerPos = flowerPosOpt.get().pos();
            Vec3 vec3 = Vec3.atBottomCenterOf(flowerPos).add(0.0, 0.6F, 0.0);

            if (vec3.distanceTo(bee.position()) > 1.0) {
                this.hoverPos = vec3;
                this.setWantedPos(bee);
            } else {
                if (this.hoverPos == null) this.hoverPos = vec3;

                boolean closeToHover = bee.position().distanceTo(this.hoverPos) <= 0.1;
                boolean updatePos = true;

                if (closeToHover) {
                    boolean randomOffset = bee.getRandom().nextInt(25) == 0;
                    if (randomOffset) {
                        this.hoverPos = new Vec3(
                                vec3.x() + getOffset(bee),
                                vec3.y(),
                                vec3.z() + getOffset(bee)
                        );
                        bee.getNavigation().stop();
                    } else {
                        updatePos = false;
                    }
                    bee.getLookControl().setLookAt(vec3.x(), vec3.y(), vec3.z());
                }

                if (updatePos) this.setWantedPos(bee);

                // Increment *successful* pollination ticks
                BeeAi.incrementMemory(brain, ModMemoryTypes.SUCCESSFUL_POLLINATING_TICKS);
                int successTicks = brain.getMemory(ModMemoryTypes.SUCCESSFUL_POLLINATING_TICKS).orElse(0);

                if (bee.getRandom().nextFloat() < 0.05F && successTicks > this.lastSoundPlayedTick + 59) {
                    this.lastSoundPlayedTick = successTicks;
                    bee.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
                }
            }
        }

        // Check if flower exists still
        flowerPosOpt.ifPresent(flowerPos -> {
            if (!serverLevel.getBlockState(flowerPos.pos()).is(BlockTags.FLOWERS)) {
                clearFlowerMemory(brain);
            }
        });
    }

    private static void clearFlowerMemory(Brain<?> brain) {
        brain.eraseMemory(ModMemoryTypes.FLOWER_POS);
        brain.setMemory(ModMemoryTypes.POLLINATING_TICKS, 0);
    }

}
