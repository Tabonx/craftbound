package com.craftbound.client;

import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;

// The book's hookup to an open screen: where a placed recipe goes, and which items are craftable
// from what the screen holds. The version changes whenever that answer might have.
public record MenuBinding(RecipePlacer placer, Supplier<Set<Item>> craftable, IntSupplier version)
{
    MenuBinding guardedBy(IntegrationGuard guard, RecipePlacer fallback)
    {
        return new MenuBinding(new GuardedPlacer(guard, placer, fallback),
                () -> guard.call(craftable, Set::of),
                () -> guard.call(version::getAsInt, () -> 0));
    }
}
