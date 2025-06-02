package io.github.dopadream;

import dev.architectury.registry.registries.DeferredRegister;
import io.github.dopadream.config.BrainierBeesConfig;
import io.github.dopadream.registry.BrainierBeesRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BrainierBees {
    public static final String MOD_ID = "brainier_bees";
    public static final Logger LOGGER = LogManager.getLogger(BrainierBees.MOD_ID);

    // Core Registries
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(MOD_ID, Registries.MEMORY_MODULE_TYPE);
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(MOD_ID, Registries.SENSOR_TYPE);


    public static void init() {


        BrainierBeesRegistries.init();
        BrainierBeesConfig.init();
    }
}
