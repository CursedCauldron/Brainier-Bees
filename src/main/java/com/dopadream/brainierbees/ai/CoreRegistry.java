package com.dopadream.brainierbees.ai;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class CoreRegistry<T> {
    protected final Registry<T> registry;
    protected final String modId;
    protected boolean isPresent;

    protected CoreRegistry(Registry<T> registry, String modId) {
        this.registry = registry;
        this.modId = modId;
        this.isPresent = false;
    }

    public static <T> CoreRegistry<T> create(Registry<T> registry, String modId) {
        return new CoreRegistry<>(registry, modId);
    }

    public <E extends T> E register(String key, E entry) {
        return Registry.register(this.registry, ResourceLocation.fromNamespaceAndPath(this.modId, key), entry);
    }

    public void register() {
        if (this.isPresent) throw new IllegalArgumentException("Duplication of Registry: " + this.registry);
        this.isPresent = true;
    }
}