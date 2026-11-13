package com.craftbound.placement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlacementAccessTest
{
    @Test
    void knownRecipesArePlaced()
    {
        assertEquals(PlacementAccess.PLACE, PlacementAccess.of(false, true));
        assertEquals(PlacementAccess.PLACE, PlacementAccess.of(true, true));
    }

    @Test
    void unknownRecipesAreOnlyLentForThePlacement()
    {
        assertEquals(PlacementAccess.PLACE_WITH_TEMPORARY_GRANT, PlacementAccess.of(false, false));
    }

    @Test
    void limitedCraftingRefusesUnknownRecipes()
    {
        assertEquals(PlacementAccess.REFUSE, PlacementAccess.of(true, false));
    }
}
