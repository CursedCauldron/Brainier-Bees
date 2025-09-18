package com.dopadream.brainierbees.registry;

import com.dopadream.brainierbees.BrainierBees;
import com.dopadream.brainierbees.ai.BeeAi;
import com.dopadream.brainierbees.config.BrainierBeesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModSensorTypes {

    public static final SensorType<TemptingSensor> BEE_TEMPTATIONS = registerSensorType("bee_temptations", () -> new TemptingSensor(BeeAi.getTemptations()));

    @NotNull
    private static SensorType<TemptingSensor> registerSensorType(String name, Supplier<TemptingSensor> supplier) {
        return Registry.register(BuiltInRegistries.SENSOR_TYPE, ResourceLocation.fromNamespaceAndPath(BrainierBees.MOD_ID, name), new SensorType<>(supplier));
    }

    public static void init() {
    }
}
