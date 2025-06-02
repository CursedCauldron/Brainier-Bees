package io.github.dopadream;

import io.github.dopadream.config.BrainierBeesConfig;
import io.github.dopadream.registry.BrainierBeesRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BrainierBees {
    public static final String MOD_ID = "brainier_bees";
    public static final Logger LOGGER = LogManager.getLogger(BrainierBees.MOD_ID);

    public static void init() {
        BrainierBeesRegistries.init();
        BrainierBeesConfig.init();
        // Write common init code here.
    }
}
