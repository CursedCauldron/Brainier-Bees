package cursedcauldron.brainierbees.ai;

import com.mojang.serialization.Codec;
import cursedcauldron.brainierbees.BrainierBees;
import cursedcauldron.brainierbees.mixin.MemoryModuleAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.pathfinder.Path;

import java.util.List;
import java.util.Optional;

public class ModMemoryTypes {
    public static final CoreRegistry<MemoryModuleType<?>> MEMORY_MODULES = CoreRegistry.create(Registry.MEMORY_MODULE_TYPE_REGISTRY, BrainierBees.MOD_ID);

    public static final MemoryModuleType<BlockPos> FLOWER_POS = register("flower_pos");
    public static final MemoryModuleType<BlockPos> HIVE_POS = register("hive_pos");
    public static final MemoryModuleType<Path> LAST_PATH = register("last_path");

    public static final MemoryModuleType<List<BlockPos>> HIVE_BLACKLIST = register("hive_blacklist");

    public static final MemoryModuleType<Integer> POLLINATING_COOLDOWN = register("pollinating_cooldown");
    public static final MemoryModuleType<Integer> POLLINATING_TICKS = register("pollinating_ticks");
    public static final MemoryModuleType<Integer> SUCCESSFUL_POLLINATING_TICKS = register("successful_pollinating_ticks");
    public static final MemoryModuleType<Integer> COOLDOWN_LOCATE_HIVE = register("cooldown_locate_hive");
    public static final MemoryModuleType<Integer> TRAVELLING_TICKS = register("travelling_ticks");
    public static final MemoryModuleType<Integer> STUCK_TICKS = register("stuck_ticks");

    public static final MemoryModuleType<Boolean> WANTS_HIVE = register("wants_hive");




    public static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
        return MEMORY_MODULES.register(id, MemoryModuleAccessor.createMemoryModuleType(Optional.of(codec)));
    }

    public static <U> MemoryModuleType<U> register(String id) {
        return MEMORY_MODULES.register(id, MemoryModuleAccessor.createMemoryModuleType(Optional.empty()));
    }
}
