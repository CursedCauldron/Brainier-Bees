package io.github.dopadream.neoforge;

import net.neoforged.fml.common.Mod;

import io.github.dopadream.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
