package io.github.dopadream.fabric;

import net.fabricmc.api.ModInitializer;

import io.github.dopadream.BrainierBees;

public final class BrainierBeesFabric implements ModInitializer {
    @Override
    public void onInitialize() {

        BrainierBees.init();
    }
}
