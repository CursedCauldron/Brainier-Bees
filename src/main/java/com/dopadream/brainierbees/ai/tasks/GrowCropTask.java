package com.dopadream.brainierbees.ai.tasks;

import com.dopadream.brainierbees.mixin.BeeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Map;

public class GrowCropTask extends Behavior<Bee> {

    public GrowCropTask() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    public boolean canBeeUse(ServerLevel level, Bee bee) {
        if (((BeeAccessor) bee).invokeGetCropsGrownSincePollination() >= 10) {
            return false;
        } else if (level.getRandom().nextFloat() < 0.3F) {
            return false;
        } else {
            return bee.hasNectar();
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Bee livingEntity) {
        return canBeeUse(serverLevel, livingEntity);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Bee livingEntity, long l) {
        return canBeeUse(serverLevel, livingEntity);
    }

    @Override
    protected void tick(ServerLevel level, Bee bee, long l) {
        if (bee.getRandom().nextInt(1, 31) == 1) {
            for (int i = 1; i <= 2; ++i) {
                BlockPos blockPos = bee.blockPosition().below(i);
                BlockState blockState = level.getBlockState(blockPos);
                Block block = blockState.getBlock();
                boolean bl = false;
                IntegerProperty integerProperty = null;
                if (blockState.is(BlockTags.BEE_GROWABLES)) {
                    if (block instanceof CropBlock cropBlock) {
                        if (!cropBlock.isMaxAge(blockState)) {
                            bl = true;
                            integerProperty = cropBlock.getAgeProperty();
                        }
                    } else if (block instanceof StemBlock) {
                        int j = blockState.getValue(StemBlock.AGE);
                        if (j < 7) {
                            bl = true;
                            integerProperty = StemBlock.AGE;
                        }
                    } else if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
                        int j = blockState.getValue(SweetBerryBushBlock.AGE);
                        if (j < 3) {
                            bl = true;
                            integerProperty = SweetBerryBushBlock.AGE;
                        }
                    } else if (blockState.is(Blocks.CAVE_VINES) || blockState.is(Blocks.CAVE_VINES_PLANT)) {
                        ((BonemealableBlock) blockState.getBlock()).performBonemeal(level, bee.getRandom(), blockPos, blockState);
                    }

                    if (bl) {
                        level.levelEvent(2005, blockPos, 0);
                        level.setBlockAndUpdate(blockPos, blockState.setValue(integerProperty, Integer.valueOf(blockState.getValue(integerProperty) + 1)));
                        ((BeeAccessor)bee).invokeIncrementNumCropsGrownSincePollination();
                    }
                }
            }
        }
    }
}
