package cursedcauldron.brainierbees.mixin;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import static cursedcauldron.brainierbees.ai.ModMemoryTypes.HIVE_POS;

@Mixin(Bee.class)
public interface BeeAccessor {


    @Accessor
    int getStayOutOfHiveCountdown();

    @Accessor("hivePos")
    public void setHivePos(BlockPos pos);

    @Accessor("remainingCooldownBeforeLocatingNewFlower")
    public void setRemainingCooldownBeforeLocatingNewFlower(int integer);

    @Invoker("getCropsGrownSincePollination")
    public int invokeGetCropsGrownSincePollination();

    @Invoker("incrementNumCropsGrownSincePollination")
    public void invokeIncrementNumCropsGrownSincePollination();

    @Invoker("setHasNectar")
    public void invokeSetHasNectar(boolean bool);
}
