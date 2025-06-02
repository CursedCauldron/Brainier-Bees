package io.github.dopadream.ai.tasks;


import io.github.dopadream.mixin.BeeAccessor;
import io.github.dopadream.registry.BrainierBeesRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PollinateFlowerTask extends Behavior<Bee> {


    private int lastSoundPlayedTick;
    private boolean pollinating;
    @Nullable
    private Vec3 hoverPos;


    public PollinateFlowerTask() {
        super(Map.of(BrainierBeesRegistries.FLOWER_POS.get(), MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee bee) {
        return bee.getBrain().getMemory(BrainierBeesRegistries.FLOWER_POS.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.POLLINATING_COOLDOWN.get()).isEmpty() && !(bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).get());
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee bee, long l) {
        return bee.getBrain().getMemory(BrainierBeesRegistries.FLOWER_POS.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.POLLINATING_COOLDOWN.get()).isEmpty() && !(bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.WANTS_HIVE.get()).get());
    }

    private boolean hasPollinatedLongEnough(Bee bee) {
        return bee.getBrain().getMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get()).isPresent() && bee.getBrain().getMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get()).get() > 400;
    }


    void stopPollinating() {
        this.pollinating = false;
    }

    void startPollinating() {
        this.pollinating = true;
    }


    @Override
    protected void start(ServerLevel serverLevel, Bee bee, long l) {
        super.start(serverLevel, bee, l);
        this.lastSoundPlayedTick = 0;
        this.startPollinating();
        bee.resetTicksWithoutNectarSinceExitingHive();
    }


    @Override
    protected void stop(ServerLevel serverLevel, Bee bee, long l) {
        super.stop(serverLevel, bee, l);
        if (this.hasPollinatedLongEnough(bee)) {
            ((BeeAccessor)bee).invokeSetHasNectar(true);
            bee.getBrain().eraseMemory(BrainierBeesRegistries.FLOWER_POS.get());
            bee.getBrain().setMemory(BrainierBeesRegistries.POLLINATING_COOLDOWN.get(), UniformInt.of(400, 400).sample(serverLevel.getRandom()));
        }

        this.stopPollinating();
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
        if (bee.getBrain().getMemory(BrainierBeesRegistries.POLLINATING_TICKS.get()).isPresent()) {
            bee.getBrain().setMemory(BrainierBeesRegistries.POLLINATING_TICKS.get(), bee.getBrain().getMemory(BrainierBeesRegistries.POLLINATING_TICKS.get()).get() + 1);
        } else {
            bee.getBrain().setMemory(BrainierBeesRegistries.POLLINATING_TICKS.get(), 1);
        }
        if (bee.getBrain().getMemory(BrainierBeesRegistries.POLLINATING_TICKS.get()).get() > 600) {
            bee.getBrain().eraseMemory(BrainierBeesRegistries.FLOWER_POS.get());
            bee.getBrain().setMemory(BrainierBeesRegistries.POLLINATING_TICKS.get(), 0);
        } else if (bee.getBrain().getMemory(BrainierBeesRegistries.FLOWER_POS.get()).isPresent()) {
            Vec3 vec3 = Vec3.atBottomCenterOf(bee.getBrain().getMemory(BrainierBeesRegistries.FLOWER_POS.get()).get().pos()).add(0.0, 0.6F, 0.0);
            if (vec3.distanceTo(bee.position()) > 1.0) {
                this.hoverPos = vec3;
                this.setWantedPos(bee);
            } else {
                if (this.hoverPos == null) {
                    this.hoverPos = vec3;
                }

                boolean bl = bee.position().distanceTo(this.hoverPos) <= 0.1;
                boolean bl2 = true;
                if (!bl && bee.getBrain().getMemory(BrainierBeesRegistries.POLLINATING_TICKS.get()).get() > 600) {
                    bee.getBrain().eraseMemory(BrainierBeesRegistries.FLOWER_POS.get());
                    bee.getBrain().setMemory(BrainierBeesRegistries.POLLINATING_TICKS.get(), 0);
                } else {
                    if (bl) {
                        boolean bl3 = bee.getRandom().nextInt(25) == 0;
                        if (bl3) {
                            this.hoverPos = new Vec3(vec3.x() + (double)this.getOffset(bee), vec3.y(), vec3.z() + (double)this.getOffset(bee));
                            bee.getNavigation().stop();
                        } else {
                            bl2 = false;
                        }

                        bee.getLookControl().setLookAt(vec3.x(), vec3.y(), vec3.z());
                    }

                    if (bl2) {
                        this.setWantedPos(bee);
                    }

                    if (bee.getBrain().getMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get()).isPresent()) {
                        bee.getBrain().setMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get(), bee.getBrain().getMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get()).get() + 1);
                    } else {
                        bee.getBrain().setMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get(), 1);
                    }
                    if (bee.getRandom().nextFloat() < 0.05F && bee.getBrain().getMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get()).get() > this.lastSoundPlayedTick + 59) {
                        this.lastSoundPlayedTick = bee.getBrain().getMemory(BrainierBeesRegistries.SUCCESSFUL_POLLINATING_TICKS.get()).get();
                        bee.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
                    }

                }
            }
        }
        if(bee.getBrain().getMemory(BrainierBeesRegistries.FLOWER_POS.get()).isPresent()) {
            if (!serverLevel.getBlockState(bee.getBrain().getMemory(BrainierBeesRegistries.FLOWER_POS.get()).get().pos()).is(BlockTags.FLOWERS)) {
                bee.getBrain().eraseMemory(BrainierBeesRegistries.FLOWER_POS.get());
                bee.getBrain().setMemory(BrainierBeesRegistries.POLLINATING_TICKS.get(), 0);
            }
        }
    }
}
