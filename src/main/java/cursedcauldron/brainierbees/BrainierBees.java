package cursedcauldron.brainierbees;

import cursedcauldron.brainierbees.ai.CoreRegistry;
import cursedcauldron.brainierbees.ai.ModMemoryTypes;
import net.fabricmc.api.ModInitializer;

public class BrainierBees implements ModInitializer {

    public static String MOD_ID = "brainierbees";

    @Override
    public void onInitialize() {
        ModMemoryTypes.MEMORY_MODULES.register();

    }
}
