package com.craftbound.client.integration;

import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.craftbound.client.placement.RecipePlacer;

import net.minecraft.world.item.Item;

// A screen with no grid of its own, like a storage terminal, leaves craftable null.
public record MenuBinding(RecipePlacer placer, Supplier<Set<Item>> craftable, IntSupplier version)
{
    public static MenuBinding placing(RecipePlacer placer)
    {
        return new MenuBinding(placer, null, () -> 0);
    }
}
