package com.craftbound.client;

import java.util.List;
import java.util.Optional;

import com.craftbound.progression.InputSlot;

import net.minecraft.world.inventory.AbstractContainerMenu;

// What another mod adds to the book beyond what it already tells JEI. Integrations lists them. Each
// is handed its own IntegrationGuard, and wraps its calls into the other mod's classes in it.
public interface BookIntegration
{
    // Requirements a recipe carries that its JEI category never puts in a slot, so the generic slot
    // walk cannot see them.
    default List<InputSlot> hiddenRequirements(Object recipe)
    {
        return List.of();
    }

    // How the book places recipes into, and judges what is craftable from, a screen this mod owns.
    // transfer is what the book would use otherwise.
    default Optional<MenuBinding> bind(AbstractContainerMenu menu, RecipePlacer transfer)
    {
        return Optional.empty();
    }
}
