package io.github.dopadream.neoforge;

import net.neoforged.fml.common.Mod;

import io.github.dopadream.BrainierBees;

@Mod(BrainierBees.MOD_ID)
public final class BrainierBeesNeoForge {
    public BrainierBeesNeoForge() {
        // Run our common setup.
        BrainierBees.init();
    }
}
