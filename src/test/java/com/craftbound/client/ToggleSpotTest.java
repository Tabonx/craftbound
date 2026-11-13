package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ToggleSpotTest
{
    private static final String VANILLA_SCREEN = "net.minecraft.client.gui.screens.inventory.BrewingStandScreen";
    private static final int HEIGHT = 166;
    // The inside spot on a 166 high screen: x 5 to 25, y 37 to 55.
    private static final int TOP = 37;

    @Test
    void theScreensOwnSpotWinsWhileTheScreenShows()
    {
        assertEquals(ToggleSpot.VANILLA, ToggleSpot.of(true, () -> true, false, true));
    }

    @Test
    void aFreeCornerTakesTheToggleWhenTheScreenHasNoSpot()
    {
        assertEquals(ToggleSpot.INSIDE, ToggleSpot.of(false, () -> true, false, false));
    }

    @Test
    void theOpenBookCarriesTheToggleOverAHiddenScreen()
    {
        assertEquals(ToggleSpot.CARRIED, ToggleSpot.of(true, () -> true, true, true));
    }

    @Test
    void withoutASpotTheOpenBookCarriesItAndTheClosedOneLeavesItOutside()
    {
        assertEquals(ToggleSpot.CARRIED, ToggleSpot.of(false, () -> false, false, true));
        assertEquals(ToggleSpot.OUTSIDE, ToggleSpot.of(false, () -> false, false, false));
    }

    @Test
    void theInsideSpotClearsTheAnvilsHammer()
    {
        assertEquals(TOP, ToggleSpot.insideY(HEIGHT));
    }

    @Test
    void aCornerWithoutSlotsIsFree()
    {
        assertTrue(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(80, 80))));
    }

    @Test
    void aModdedScreensCornerIsNeverFree()
    {
        assertFalse(ToggleSpot.insideFree("com.example.MachineScreen", HEIGHT, List.of()));
    }

    // A slot's frame reaches a pixel past its item, so a slot one pixel clear still touches the toggle.
    @Test
    void aSlotFrameTouchingTheToggleTakesTheCorner()
    {
        assertFalse(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(25, TOP))));
        assertFalse(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(5, TOP + 18))));
        assertFalse(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(-11, TOP - 16))));
    }

    @Test
    void aSlotWhoseFrameEndsAtTheToggleLeavesTheCornerFree()
    {
        assertTrue(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(26, TOP))));
        assertTrue(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(5, TOP + 19))));
        assertTrue(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(-12, TOP))));
        assertTrue(ToggleSpot.insideFree(VANILLA_SCREEN, HEIGHT, List.of(ToggleSpot.slot(5, TOP - 17))));
    }
}
