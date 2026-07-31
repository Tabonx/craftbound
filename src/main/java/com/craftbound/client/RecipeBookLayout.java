package com.craftbound.client;

// Pure layout math for docking the book left of the inventory. When the book is open we reserve
// a column on the left and shift the inventory right, keeping the book+inventory pair centered —
// the same idea as vanilla shifting its GUI to make room. Kept free of Minecraft types so it can
// be unit-tested.
public final class RecipeBookLayout
{
    public static final int BOOK_WIDTH = 147;
    public static final int GAP = 8;

    // In the recipe state the book grows left; bias the open layout right (into otherwise-empty
    // right margin) so it has up to this much extra room before running off the left edge. Kept
    // moderate, and never at the cost of the inventory's minimum right margin.
    public static final int EXTRA_LEFT_ROOM = 90;
    public static final int MIN_RIGHT_MARGIN = 8;

    private RecipeBookLayout()
    {
    }

    // The inventory's left edge (leftPos). Centered normally; when the book is open it is shifted
    // right by the reserved book column plus an extra bias that gives the book room to widen.
    public static int inventoryLeftPos(int screenWidth, int imageWidth, boolean bookOpen)
    {
        if (!bookOpen)
            return (screenWidth - imageWidth) / 2;

        int reserved = BOOK_WIDTH + GAP;
        int clusterLeft = Math.max(0, (screenWidth - (reserved + imageWidth)) / 2);
        int spareRight = Math.max(0, clusterLeft - MIN_RIGHT_MARGIN);
        int bias = Math.min(EXTRA_LEFT_ROOM, spareRight);
        return clusterLeft + reserved + bias;
    }

    // Right edge that book content is aligned to: just left of the inventory, across the gap.
    public static int bookRight(int inventoryLeftPos)
    {
        return inventoryLeftPos - GAP;
    }
}
