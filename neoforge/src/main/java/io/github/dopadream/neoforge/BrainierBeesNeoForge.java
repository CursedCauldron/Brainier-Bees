package io.github.dopadream.neoforge;

import io.github.dopadream.registry.BrainierBeesRegistries;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

import io.github.dopadream.BrainierBees;

import static io.github.dopadream.BrainierBees.MOD_ID;

@Mod(MOD_ID)
public final class BrainierBeesNeoForge {
    public BrainierBeesNeoForge(IEventBus modBus) {
        // Run our common setup.
        BrainierBees.init();
    }
}
