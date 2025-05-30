package io.github.dopadream.registry;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.dopadream.BrainierBees;
import io.github.dopadream.ai.BeeBrain;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.level.pathfinder.Path;

import java.util.List;
import java.util.Optional;

import static io.github.dopadream.BrainierBees.MOD_ID;

public class BrainierBeesRegistries {

    // Core Registries
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(MOD_ID, Registries.MEMORY_MODULE_TYPE);
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(MOD_ID, Registries.SENSOR_TYPE);

    // Sensor Types

    public static final RegistrySupplier<SensorType<TemptingSensor>> BEE_TEMPTATIONS =
            SENSOR_TYPES.register("bee_temptations",
                    () -> new SensorType<>(() -> new TemptingSensor(BeeBrain.getTemptations())));


    // Memory Modules
    public static final RegistrySupplier<MemoryModuleType<GlobalPos>> FLOWER_POS = MEMORY_MODULES.register("flower_pos", () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));
    public static final RegistrySupplier<MemoryModuleType<GlobalPos>> HIVE_POS = MEMORY_MODULES.register("hive_pos", () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

    public static final RegistrySupplier<MemoryModuleType<List<GlobalPos>>> HIVE_BLACKLIST = MEMORY_MODULES.register("hive_pos", () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistrySupplier<MemoryModuleType<Integer>> POLLINATING_COOLDOWN = MEMORY_MODULES.register("pollinating_cooldown", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistrySupplier<MemoryModuleType<Integer>> POLLINATING_TICKS = MEMORY_MODULES.register("pollinating_ticks", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistrySupplier<MemoryModuleType<Integer>> SUCCESSFUL_POLLINATING_TICKS = MEMORY_MODULES.register("successful_pollinating_ticks", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistrySupplier<MemoryModuleType<Integer>> LOCATE_HIVE_COOLDOWN = MEMORY_MODULES.register("locate_hive_cooldown", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistrySupplier<MemoryModuleType<Integer>> TRAVELLING_TICKS = MEMORY_MODULES.register("travelling_ticks", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistrySupplier<MemoryModuleType<Integer>> STUCK_TICKS = MEMORY_MODULES.register("stuck_ticks", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final RegistrySupplier<MemoryModuleType<Boolean>> WANTS_HIVE = MEMORY_MODULES.register("wants_hive", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistrySupplier<MemoryModuleType<Path>> LAST_PATH = MEMORY_MODULES.register("last_path", () -> new MemoryModuleType<>(Optional.empty()));

    public static void init(){
        BrainierBees.LOGGER.debug("Registries initialized for Brainier Bees");
    }
}
