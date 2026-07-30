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
}
