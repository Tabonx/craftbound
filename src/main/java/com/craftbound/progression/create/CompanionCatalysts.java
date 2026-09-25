package com.craftbound.progression.create;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

// Create lists the block a machine works on as a catalyst beside the machine: a basin under the
// mixer and press categories, a depot and a belt under the deployer. Catalysts are alternatives, so
// a basin alone would open mixing, packing and brewing before the player has either machine. These
// companions are dropped, so a category is gated on the machine that does the work.
public final class CompanionCatalysts
{
    private static final Set<ResourceLocation> COMPANIONS = Set.of(
            ResourceLocation.fromNamespaceAndPath("create", "basin"),
            ResourceLocation.fromNamespaceAndPath("create", "depot"),
            ResourceLocation.fromNamespaceAndPath("create", "belt_connector"));

    // A category whose only catalysts are companions keeps them, since nothing else could open it.
    public static Set<ResourceLocation> machinesOf(Set<ResourceLocation> catalysts)
    {
        Set<ResourceLocation> machines = new HashSet<>(catalysts);
        machines.removeAll(COMPANIONS);
        return machines.isEmpty() ? catalysts : Set.copyOf(machines);
    }

    private CompanionCatalysts() {}
}
