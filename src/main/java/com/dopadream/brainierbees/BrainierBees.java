package com.dopadream.brainierbees;

import com.dopadream.brainierbees.ai.ModMemoryTypes;
import com.dopadream.brainierbees.util.SimpleConfig;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrainierBees implements ModInitializer {

    public static String MOD_ID = "brainierbees";
    private static final Logger LOGGER = LogManager.getLogger(BrainierBees.MOD_ID);

    public SimpleConfig CONFIG = SimpleConfig.of( MOD_ID ).provider( this::provider ).request();

    public String provider(String filename) {
        return """
                #Config for Brainier Bees
                #Setting any of these values too high may cause instability and performance issues!
                
                #Determines the maximum radius in blocks that bees will wander from their hive.
                #The amount of ticks bees are allowed to spend pathing home scales with this number.
                # (Default/Vanilla: 22)
                maxWanderRadius=22
                
                #Determines the radius in blocks that bees can target flowers from.
                # (Default: 8/Vanilla: 5)
                flowerLocateRange=8
                """;
    }

    public final int MAX_WANDER_RADIUS = CONFIG.getOrDefault( "maxWanderRadius", 22 );
    public final int FLOWER_LOCATE_RANGE = CONFIG.getOrDefault( "flowerLocateRange", 8 );


    @Override
    public void onInitialize() {
        ModMemoryTypes.MEMORY_MODULES.register();
    }
}
