package com.craftbound.client;

// Pure layout math for docking the book left of the inventory. When the book is open we reserve a
// fixed column on the left — sized for the widened recipe panel, not the narrow browse panel — and
// shift the inventory right by that constant, keeping the book+inventory pair centered. Because the
// reservation is constant the inventory never moves between browse and recipe states, nor per
// recipe; it only moves when the book opens or closes. Kept free of Minecraft types so it can be
// unit-tested.
public final class RecipeBookLayout
{
    public static final int BOOK_WIDTH = 147;
    // The width the book occupies in the recipe state; the reserved column is sized for this so the
    // panel can widen to it without shifting the inventory. Browsing, the book stays BOOK_WIDTH and
    // simply sits right-anchored in the same reserved column.
    public static final int RECIPE_WIDTH = 200;
    public static final int GAP = 8;

    private RecipeBookLayout()
    {
    }

    // The inventory's left edge (leftPos). Centered normally; when the book is open it is shifted
    // right by the fixed reserved column so the widened recipe panel always fits beside it.
    public static int inventoryLeftPos(int screenWidth, int imageWidth, boolean bookOpen)
    {
        if (!bookOpen)
            return (screenWidth - imageWidth) / 2;

        int reserved = RECIPE_WIDTH + GAP;
        int clusterLeft = Math.max(0, (screenWidth - (reserved + imageWidth)) / 2);
        return clusterLeft + reserved;
    }

    // Right edge that book content is aligned to: just left of the inventory, across the gap.
    public static int bookRight(int inventoryLeftPos)
    {
        return inventoryLeftPos - GAP;
    }
}
