package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RecipeBookLayoutTest
{
    private static final int INVENTORY_WIDTH = 176;

    @Test
    void closedBookCentersInventory()
    {
        assertEquals((640 - INVENTORY_WIDTH) / 2,
                RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, false));
    }

    @Test
    void openBookShiftsInventoryRightByReservedColumn()
    {
        int closed = RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, false);
        int open = RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, true);
        assertTrue(open > closed, "open book should push the inventory to the right");
    }

    @Test
    void openBookLeavesRoomForTheBookLeftOfTheInventory()
    {
        int leftPos = RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, true);
        int bookRight = RecipeBookLayout.bookRight(leftPos);
        assertTrue(bookRight - RecipeBookLayout.BOOK_WIDTH >= 0,
                "the reserved book column should fit on screen");
        assertTrue(bookRight <= leftPos, "book must sit left of the inventory");
    }

    @Test
    void narrowWindowDoesNotProduceNegativeLeftPos()
    {
        int leftPos = RecipeBookLayout.inventoryLeftPos(200, INVENTORY_WIDTH, true);
        assertTrue(leftPos >= RecipeBookLayout.BOOK_WIDTH + RecipeBookLayout.GAP,
                "inventory still clears the reserved column even when the window is too small");
    }

    @Test
    void openReservesTheWidenedRecipeColumnAndCentersTheCluster()
    {
        int reserved = RecipeBookLayout.RECIPE_WIDTH + RecipeBookLayout.GAP;
        int clusterLeft = (640 - (reserved + INVENTORY_WIDTH)) / 2;

        int leftPos = RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, true);
        assertEquals(clusterLeft + reserved, leftPos, "inventory sits just right of the reserved column");
        assertTrue(leftPos + INVENTORY_WIDTH <= 640, "inventory must stay fully on screen");

        int recipeLeft = RecipeBookLayout.bookRight(leftPos) - RecipeBookLayout.RECIPE_WIDTH;
        assertTrue(recipeLeft >= 0, "the widened recipe panel fits in the reserved column");
    }

    @Test
    void openReservationDoesNotDependOnRecipeState()
    {
        int leftPos = RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, true);
        assertEquals(RecipeBookLayout.RECIPE_WIDTH + RecipeBookLayout.GAP,
                leftPos - (640 - (RecipeBookLayout.RECIPE_WIDTH + RecipeBookLayout.GAP + INVENTORY_WIDTH)) / 2,
                "the reserved column is a fixed constant, independent of the shown recipe");
    }

    @Test
    void narrowWindowClampsToTheReservedColumn()
    {
        int reserved = RecipeBookLayout.RECIPE_WIDTH + RecipeBookLayout.GAP;
        assertEquals(reserved, RecipeBookLayout.inventoryLeftPos(200, INVENTORY_WIDTH, true),
                "a window with no spare margin still clears the reserved column, without going off-screen");
    }
}
