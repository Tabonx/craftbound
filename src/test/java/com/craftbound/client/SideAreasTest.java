package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.craftbound.client.SideAreas.Area;

class SideAreasTest
{
    private static final int LEFT = 200;
    private static final int TOP = 50;
    private static final int HEIGHT = 200;

    private static SideAreas of(Area... areas)
    {
        return SideAreas.of(List.of(areas), LEFT, TOP, HEIGHT);
    }

    @Test
    void nothingReportedLeavesTheScreenAsItsOwnImage()
    {
        assertEquals(SideAreas.NONE, of());
    }

    // AE2's toolbar overlaps the screen's edge, Refined Storage's buttons stop just short of it.
    @Test
    void elementsOnTheLeftEdgeReachAsFarAsTheWidest()
    {
        assertEquals(new SideAreas(30, Integer.MAX_VALUE),
                of(new Area(LEFT - 17, TOP + 1, 20, 150), new Area(LEFT - 30, TOP, 28, 18)));
    }

    @Test
    void areasElsewhereAreNotTheScreens()
    {
        assertEquals(SideAreas.NONE, of(
                new Area(LEFT + 20, TOP, 30, 30),                          // inside the screen
                new Area(10, TOP, 30, 30),                                 // far left, a floating window
                new Area(LEFT - 30, TOP - 40, 28, 34),                     // above the screen's rows
                new Area(LEFT + 300, 0, 160, 32)));                        // a toast, top right
    }

    // Create's stock keeper claims everything left of itself around its keeper, and draws the keeper taller.
    @Test
    void anAreaFromTheWindowEdgeToTheScreenClaimsThoseRows()
    {
        assertEquals(new SideAreas(0, 140), of(new Area(0, TOP + 170, LEFT, 300)));
        assertEquals(SideAreas.NONE, of(new Area(0, TOP - 10, LEFT, 300)));
        assertEquals(new SideAreas(0, 140), of(new Area(0, TOP + 170, LEFT - 3, 300)));
    }
}
