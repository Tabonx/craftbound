package com.craftbound.client.integration;

import java.util.List;
import java.util.Optional;

import com.craftbound.client.placement.RecipePlacer;
import com.craftbound.progression.InputSlot;

import net.minecraft.world.inventory.AbstractContainerMenu;

// Each integration wraps its calls into the other mod's classes in its own IntegrationGuard.
public interface BookIntegration
{
    // Requirements a JEI category never puts in a slot.
    default List<InputSlot> hiddenRequirements(Object recipe)
    {
        return List.of();
    }

    // transfer is what the book would use otherwise.
    default Optional<MenuBinding> bind(AbstractContainerMenu menu, RecipePlacer transfer)
    {
        return Optional.empty();
    }
}
