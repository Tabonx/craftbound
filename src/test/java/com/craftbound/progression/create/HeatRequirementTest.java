package com.craftbound.progression.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import com.craftbound.progression.InputSlot;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class HeatRequirementTest
{
    private static final ResourceLocation BLAZE_BURNER = ResourceLocation.parse("create:blaze_burner");
    private static final ResourceLocation BLAZE_CAKE = ResourceLocation.parse("create:blaze_cake");

    @Test
    void unheatedRecipesRequireNothingExtra()
    {
        assertEquals(List.of(), HeatRequirement.slotsFor(HeatRequirement.NONE));
    }

    @Test
    void heatedRecipesRequireABlazeBurner()
    {
        assertEquals(List.of(InputSlot.ofItems(Set.of(BLAZE_BURNER))),
                HeatRequirement.slotsFor(HeatRequirement.HEATED));
    }

    // Two slots, not one containing both: every slot must be satisfied, and a burner alone only
    // ever reaches "heated".
    @Test
    void superheatedRecipesRequireBurnerAndCakeSeparately()
    {
        assertEquals(List.of(InputSlot.ofItems(Set.of(BLAZE_BURNER)),
                        InputSlot.ofItems(Set.of(BLAZE_CAKE))),
                HeatRequirement.slotsFor(HeatRequirement.SUPERHEATED));
    }

    // Create is free to add heat levels; an unknown one must not lock every recipe in the game.
    @Test
    void anUnrecognisedHeatConditionRequiresNothing()
    {
        assertTrue(HeatRequirement.slotsFor("VOLCANIC").isEmpty());
    }
}
