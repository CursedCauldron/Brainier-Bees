package com.dopadream.brainierbees.config;


import com.dopadream.brainierbees.BrainierBees;

public class BrainierBeesConfig {

    public static SimpleConfig CONFIG = SimpleConfig.of(BrainierBees.MOD_ID).provider(BrainierBeesConfig::provider).request();

    public static String provider(String filename) {
        return """
                #Config for Brainier Bees
                #Setting any of these values too high may cause instability and performance issues!
                
                #Determines the maximum radius in blocks that bees will wander from their hive.
                #The amount of ticks bees are allowed to spend pathing home scales with this number.
                # (Default: 25/Vanilla: 22)
                maxWanderRadius=25
                
                #Determines the radius in blocks that bees can target flowers from.
                # (Default: 8/Vanilla: 5)
                flowerLocateRange=8
                """;
    }

    public static final int MAX_WANDER_RADIUS = CONFIG.getOrDefault("maxWanderRadius", 25);
    public static final int FLOWER_LOCATE_RANGE = CONFIG.getOrDefault("flowerLocateRange", 8);

    public static void init(){
        BrainierBees.LOGGER.info("Config initialized for Brainier Bees");
    }
}
