package com.dopadream.brainierbees.ai;

import com.dopadream.brainierbees.BrainierBees;
import com.dopadream.brainierbees.mixin.MemoryModuleAccessor;
import com.dopadream.brainierbees.mixin.SensorTypeAccessor;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public class ModSensorTypes {

    public static final SensorType<TemptingSensor> BEE_TEMPTATIONS = registerSensorType("bee_temptations", () -> new TemptingSensor(BeeBrain.getTemptations()));

    @NotNull
    private static SensorType<TemptingSensor> registerSensorType(String name, Supplier<TemptingSensor> supplier) {
        return Registry.register(BuiltInRegistries.SENSOR_TYPE, ResourceLocation.fromNamespaceAndPath(BrainierBees.MOD_ID, name), new SensorType<>(supplier));
    }
}
