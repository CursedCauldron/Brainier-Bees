package cursedcauldron.brainierbees.ai;

import cursedcauldron.brainierbees.BrainierBees;
import cursedcauldron.brainierbees.mixin.SensorTypeAccessor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModSensorTypes {
    public static final SensorType<TemptingSensor> BEE_TEMPTATIONS = registerSensorType("bee_temptations", () -> new TemptingSensor(BeeBrain.getTemptations()));

    @NotNull
    private static SensorType<TemptingSensor> registerSensorType(String name, Supplier<TemptingSensor> supplier) {
        return SensorTypeAccessor.callRegister(BrainierBees.MOD_ID + ":" + name, supplier);
    }

}
